package br.org.catolicasc.automoveis.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import br.org.catolicasc.automoveis.model.Automovel;
import br.org.catolicasc.automoveis.model.Cliente;
import br.org.catolicasc.automoveis.repository.AutomovelRepository;
import br.org.catolicasc.automoveis.repository.ClienteRepository;

/**
 * Testa a API REST com os dados iniciais. @Transactional desfaz o que cada
 * teste grava, para nao alterar o banco usado pelos outros testes.
 */
@SpringBootTest
@Transactional
class ApiRestTest {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private AutomovelRepository automovelRepository;

	private MockMvc mockMvc;

	@BeforeEach
	void prepararMockMvc() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	private Long idDaPlaca(String placa) {
		return automovelRepository.findByPlaca(placa).orElseThrow().getId();
	}

	private Long novoCliente() {
		return clienteRepository.save(new Cliente("Ana", "ana@teste.com", "47 99999-0000")).getId();
	}

	@Test
	void criaPedidoFinanciadoComSeuPagamento() throws Exception {
		String corpo = """
				{"clienteId": %d, "automoveisIds": [%d, %d], "formaPagamento": "FINANCIAMENTO", "parcelas": 10}
				""".formatted(novoCliente(), idDaPlaca("AAA-0000"), idDaPlaca("RDW-8H97"));

		mockMvc.perform(post("/api/pedidos").contentType(MediaType.APPLICATION_JSON).content(corpo))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.itens.length()").value(2))
				.andExpect(jsonPath("$.total").value(222400.0))
				.andExpect(jsonPath("$.pagamento.forma").value("FINANCIAMENTO"))
				.andExpect(jsonPath("$.pagamento.parcelas").value(10))
				.andExpect(jsonPath("$.pagamento.valorParcela").value(22240.0));
	}

	@Test
	void pedidoSemFormaDePagamentoEAVista() throws Exception {
		String corpo = """
				{"clienteId": %d, "automoveisIds": [%d]}
				""".formatted(novoCliente(), idDaPlaca("MGJ-6840"));

		mockMvc.perform(post("/api/pedidos").contentType(MediaType.APPLICATION_JSON).content(corpo))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.pagamento.forma").value("A_VISTA"))
				.andExpect(jsonPath("$.pagamento.parcelas").value(1));
	}

	@Test
	void pagamentoAVistaParceladoRetorna400() throws Exception {
		String corpo = """
				{"clienteId": %d, "automoveisIds": [%d], "formaPagamento": "A_VISTA", "parcelas": 3}
				""".formatted(novoCliente(), idDaPlaca("MGJ-6840"));

		mockMvc.perform(post("/api/pedidos").contentType(MediaType.APPLICATION_JSON).content(corpo))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Pagamento a vista deve ter 1 parcela"));
	}

	@Test
	void automovelVendidoSaiDoEstoqueENaoPodeSerVendidoDeNovo() throws Exception {
		String corpo = """
				{"clienteId": %d, "automoveisIds": [%d]}
				""".formatted(novoCliente(), idDaPlaca("MGJ-6840"));

		mockMvc.perform(post("/api/pedidos").contentType(MediaType.APPLICATION_JSON).content(corpo))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/automoveis"))
				.andExpect(jsonPath("$.length()").value(3))
				.andExpect(jsonPath("$[?(@.placa == 'MGJ-6840')]").isEmpty());
		mockMvc.perform(get("/api/automoveis").param("incluirVendidos", "true"))
				.andExpect(jsonPath("$.length()").value(4))
				.andExpect(jsonPath("$[?(@.placa == 'MGJ-6840')].vendido").value(true));

		mockMvc.perform(post("/api/pedidos").contentType(MediaType.APPLICATION_JSON).content(corpo))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.erro").value("O automovel MGJ-6840 ja foi vendido"));
	}

	@Test
	void filtraAutomoveisPorOpcional() throws Exception {
		mockMvc.perform(get("/api/automoveis").param("opcional", "Airbag"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[*].placa", containsInAnyOrder("AAA-0000", "MGJ-6840")));
	}

	@Test
	void placaRepetidaRetorna409() throws Exception {
		Automovel existente = automovelRepository.findByPlaca("AAA-0000").orElseThrow();
		String corpo = """
				{"modeloId": %d, "ano": 2020, "placa": "AAA-0000", "preco": 1000}
				""".formatted(existente.getModelo().getId());

		mockMvc.perform(post("/api/automoveis").contentType(MediaType.APPLICATION_JSON).content(corpo))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.erro").exists());
	}

	@Test
	void cadastraAutomovelComMarcaEModeloNovos() throws Exception {
		String corpo = """
				{"marca": " fiat ", "modelo": "Uno", "ano": 2012, "placa": "QWE-1234", "preco": 25000}
				""";

		mockMvc.perform(post("/api/automoveis").contentType(MediaType.APPLICATION_JSON).content(corpo))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.marca").value("FIAT"))
				.andExpect(jsonPath("$.modelo").value("UNO"));
	}

	@Test
	void reaproveitaMarcaEModeloJaCadastrados() throws Exception {
		Long equinox = automovelRepository.findByPlaca("AAA-0000").orElseThrow().getModelo().getId();
		String corpo = """
				{"marca": "gm", "modelo": "equinox", "ano": 2021, "placa": "ZZZ-9999", "preco": 150000}
				""";

		mockMvc.perform(post("/api/automoveis").contentType(MediaType.APPLICATION_JSON).content(corpo))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.modeloId").value(equinox));
	}

	@Test
	void automovelSemMarcaEModeloRetorna400() throws Exception {
		mockMvc.perform(post("/api/automoveis").contentType(MediaType.APPLICATION_JSON)
				.content("{\"ano\": 2020, \"placa\": \"SEM-0001\", \"preco\": 1}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erro").value("Informe a marca e o modelo"));
	}

	@Test
	void recursoInexistenteRetorna404ComMensagem() throws Exception {
		mockMvc.perform(get("/api/clientes/999999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.erro").value("Cliente nao encontrado"));
	}
}
