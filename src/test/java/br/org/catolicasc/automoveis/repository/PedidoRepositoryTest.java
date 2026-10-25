package br.org.catolicasc.automoveis.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import br.org.catolicasc.automoveis.model.Automovel;
import br.org.catolicasc.automoveis.model.Cliente;
import br.org.catolicasc.automoveis.model.FormaPagamento;
import br.org.catolicasc.automoveis.model.Marca;
import br.org.catolicasc.automoveis.model.Modelo;
import br.org.catolicasc.automoveis.model.Pagamento;
import br.org.catolicasc.automoveis.model.Pedido;
import jakarta.persistence.EntityManager;

@DataJpaTest
class PedidoRepositoryTest {

	@Autowired
	private PedidoRepository pedidoRepository;

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private MarcaRepository marcaRepository;

	@Autowired
	private AutomovelRepository automovelRepository;

	@Autowired
	private EntityManager entityManager;

	private Cliente maria;
	private Cliente joao;
	private Automovel corola;
	private Automovel bross;

	@BeforeEach
	void prepararDados() {
		maria = clienteRepository.save(new Cliente("Maria", "maria@gmail.com", "47 99999-9999"));
		joao = clienteRepository.save(new Cliente("Joao", "joao@gmail.com", "47 98888-8888"));

		Marca toyota = new Marca("TOYOTA");
		Modelo modeloCorola = new Modelo("COROLA");
		toyota.adicionarModelo(modeloCorola);
		Marca honda = new Marca("HONDA");
		Modelo modeloBross = new Modelo("BROSS");
		honda.adicionarModelo(modeloBross);
		marcaRepository.save(toyota);
		marcaRepository.save(honda);

		corola = automovelRepository.save(new Automovel(modeloCorola, 2004, "MGJ-6840", new BigDecimal("41900.00")));
		bross = automovelRepository.save(new Automovel(modeloBross, 2022, "RDW-8H97", new BigDecimal("32500.00")));
	}

	private void sincronizarComOBanco() {
		entityManager.flush();
		entityManager.clear();
	}

	@Test
	void salvaPedidoDeUmClienteComItens() {
		Pedido pedido = new Pedido(maria, LocalDate.of(2026, 9, 21));
		pedido.adicionarItem(corola);
		pedido.adicionarItem(bross);

		Pedido salvo = pedidoRepository.save(pedido);
		sincronizarComOBanco();

		Pedido carregado = pedidoRepository.findById(salvo.getId()).orElseThrow();
		assertNotNull(carregado.getId());
		assertEquals("Maria", carregado.getCliente().getNome());
		assertEquals(LocalDate.of(2026, 9, 21), carregado.getDataPedido());
		assertEquals(2, carregado.getItens().size());
		carregado.getItens().forEach(item -> assertNotNull(item.getId()));
	}

	@Test
	void calculaTotalDoPedido() {
		Pedido pedido = new Pedido(maria, LocalDate.of(2026, 9, 21));
		pedido.adicionarItem(corola); // 41900.00
		pedido.adicionarItem(bross); // 32500.00

		Pedido salvo = pedidoRepository.save(pedido);
		sincronizarComOBanco();

		Pedido carregado = pedidoRepository.findById(salvo.getId()).orElseThrow();
		assertEquals(0, new BigDecimal("74400.00").compareTo(carregado.getTotal()));
	}

	@Test
	void listaSomenteOsPedidosDeCadaCliente() {
		Pedido p1 = new Pedido(maria, LocalDate.of(2026, 9, 1));
		p1.adicionarItem(corola);
		Pedido p2 = new Pedido(maria, LocalDate.of(2026, 9, 10));
		p2.adicionarItem(bross);
		Pedido p3 = new Pedido(joao, LocalDate.of(2026, 9, 15));
		p3.adicionarItem(automovelRepository.save(
				new Automovel(corola.getModelo(), 2010, "ABC-1234", new BigDecimal("39000.00"))));
		pedidoRepository.save(p1);
		pedidoRepository.save(p2);
		pedidoRepository.save(p3);
		sincronizarComOBanco();

		List<Pedido> pedidosDaMaria = pedidoRepository.findByCliente(maria);
		List<Pedido> pedidosDoJoao = pedidoRepository.findByCliente(joao);

		assertEquals(2, pedidosDaMaria.size());
		assertEquals(1, pedidosDoJoao.size());
		assertEquals(3, pedidoRepository.count());
	}

	@Test
	void precoDoItemNaoMudaQuandoOPrecoDoAutomovelMuda() {
		Pedido pedido = new Pedido(maria, LocalDate.of(2026, 9, 21));
		pedido.adicionarItem(corola);
		Pedido salvo = pedidoRepository.save(pedido);
		sincronizarComOBanco();

		Automovel automovel = automovelRepository.findById(corola.getId()).orElseThrow();
		automovel.setPreco(new BigDecimal("99999.99"));
		automovelRepository.save(automovel);
		sincronizarComOBanco();

		Pedido carregado = pedidoRepository.findById(salvo.getId()).orElseThrow();
		assertEquals(0, new BigDecimal("41900.00").compareTo(carregado.getTotal()));
	}

	@Test
	void excluirPedidoRemoveSeusItensMasMantemClienteEAutomovel() {
		Pedido pedido = new Pedido(maria, LocalDate.of(2026, 9, 21));
		pedido.adicionarItem(corola);
		pedido.adicionarItem(bross);
		Pedido salvo = pedidoRepository.save(pedido);
		sincronizarComOBanco();

		pedidoRepository.deleteById(salvo.getId());
		sincronizarComOBanco();

		assertEquals(0, pedidoRepository.count());
		Long itensRestantes = entityManager
				.createQuery("select count(i) from ItemPedido i", Long.class)
				.getSingleResult();
		assertEquals(0L, itensRestantes);
		assertEquals(2, clienteRepository.count());
		assertEquals(2, automovelRepository.count());
	}

	@Test
	void registraPagamentoUmParaUmComOTotalDoPedido() {
		Pedido pedido = new Pedido(maria, LocalDate.of(2026, 9, 21));
		pedido.adicionarItem(corola); // 41900.00
		pedido.adicionarItem(bross); // 32500.00
		pedido.registrarPagamento(FormaPagamento.FINANCIAMENTO, 12);

		Pedido salvo = pedidoRepository.save(pedido);
		sincronizarComOBanco();

		Pagamento pagamento = pedidoRepository.findById(salvo.getId()).orElseThrow().getPagamento();
		assertNotNull(pagamento.getId());
		assertEquals(salvo.getId(), pagamento.getPedido().getId());
		assertEquals(FormaPagamento.FINANCIAMENTO, pagamento.getForma());
		assertEquals(12, pagamento.getParcelas());
		assertEquals(0, new BigDecimal("74400.00").compareTo(pagamento.getValor()));
		assertEquals(0, new BigDecimal("6200.00").compareTo(pagamento.getValorParcela()));
	}

	@Test
	void excluirPedidoRemoveTambemOPagamento() {
		Pedido pedido = new Pedido(maria, LocalDate.of(2026, 9, 21));
		pedido.adicionarItem(corola);
		pedido.registrarPagamento(FormaPagamento.A_VISTA, 1);
		Pedido salvo = pedidoRepository.save(pedido);
		sincronizarComOBanco();

		pedidoRepository.deleteById(salvo.getId());
		sincronizarComOBanco();

		Long pagamentosRestantes = entityManager
				.createQuery("select count(p) from Pagamento p", Long.class)
				.getSingleResult();
		assertEquals(0L, pagamentosRestantes);
	}

	@Test
	void pagamentoValidaFormaEParcelas() {
		Pedido pedido = new Pedido(maria, LocalDate.of(2026, 9, 21));
		pedido.adicionarItem(corola);

		assertThrows(IllegalArgumentException.class, () -> pedido.registrarPagamento(FormaPagamento.A_VISTA, 3));
		assertThrows(IllegalArgumentException.class, () -> pedido.registrarPagamento(FormaPagamento.FINANCIAMENTO, 0));
		assertThrows(IllegalArgumentException.class, () -> pedido.registrarPagamento(null, 1));
		assertNull(pedido.getPagamento());
	}

	@Test
	void vendaTiraOAutomovelDoEstoque() {
		Pedido pedido = new Pedido(maria, LocalDate.of(2026, 9, 21));
		pedido.adicionarItem(corola);
		pedidoRepository.save(pedido);
		sincronizarComOBanco();

		List<Automovel> estoque = automovelRepository.findByVendidoFalse();
		assertEquals(1, estoque.size());
		assertEquals("RDW-8H97", estoque.get(0).getPlaca());
		assertTrue(automovelRepository.findByPlaca("MGJ-6840").orElseThrow().isVendido());
	}

	@Test
	void marcaComoVendidoQuemJaEstavaEmPedidoAntigo() {
		Pedido pedido = new Pedido(maria, LocalDate.of(2026, 9, 21));
		pedido.adicionarItem(corola);
		pedidoRepository.save(pedido);
		corola.devolverAoEstoque(); // simula um pedido gravado antes do controle de estoque
		sincronizarComOBanco();

		assertEquals(1, automovelRepository.marcarVendidosEmPedidos());
		sincronizarComOBanco();
		assertTrue(automovelRepository.findByPlaca("MGJ-6840").orElseThrow().isVendido());
		assertEquals(0, automovelRepository.marcarVendidosEmPedidos());
	}

	@Test
	void naoVendeOMesmoAutomovelDuasVezes() {
		new Pedido(maria, LocalDate.of(2026, 9, 21)).adicionarItem(corola);
		Pedido outro = new Pedido(joao, LocalDate.of(2026, 9, 22));

		assertThrows(IllegalStateException.class, () -> outro.adicionarItem(corola));
	}

	@Test
	void cancelarPedidoDevolveOsAutomoveisAoEstoque() {
		Pedido pedido = new Pedido(maria, LocalDate.of(2026, 9, 21));
		pedido.adicionarItem(corola);
		pedido.adicionarItem(bross);
		Pedido salvo = pedidoRepository.save(pedido);
		sincronizarComOBanco();

		Pedido carregado = pedidoRepository.findById(salvo.getId()).orElseThrow();
		carregado.cancelar();
		pedidoRepository.delete(carregado);
		sincronizarComOBanco();

		assertEquals(2, automovelRepository.findByVendidoFalse().size());
	}

	@Test
	void pedidoTemNoMaximoUmPagamento() {
		Pedido pedido = new Pedido(maria, LocalDate.of(2026, 9, 21));
		pedido.adicionarItem(corola);
		pedido.registrarPagamento(FormaPagamento.A_VISTA, 1);

		assertThrows(IllegalStateException.class, () -> pedido.registrarPagamento(FormaPagamento.CONSORCIO, 10));
	}
}
