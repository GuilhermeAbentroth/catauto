package br.org.catolicasc.automoveis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import br.org.catolicasc.automoveis.config.DadosIniciais;
import br.org.catolicasc.automoveis.controller.AutomovelController;
import br.org.catolicasc.automoveis.model.Automovel;
import br.org.catolicasc.automoveis.repository.AutomovelRepository;
import br.org.catolicasc.automoveis.repository.MarcaRepository;
import br.org.catolicasc.automoveis.repository.ModeloRepository;
import br.org.catolicasc.automoveis.repository.OpcionalRepository;

@SpringBootTest
class AutomoveisApplicationTests {

	@Autowired
	private AutomovelController controller;

	@Autowired
	private DadosIniciais dadosIniciais;

	@Autowired
	private AutomovelRepository automovelRepository;

	@Autowired
	private MarcaRepository marcaRepository;

	@Autowired
	private ModeloRepository modeloRepository;

	@Autowired
	private OpcionalRepository opcionalRepository;

	@Test
	void contextLoads() {
		assertNotNull(controller);
	}

	@Test
	@SuppressWarnings("unchecked")
	void catalogoTrazTodosOsVeiculosComTodasAsInformacoes() {
		Model model = new ConcurrentModel();

		String view = controller.listar(model);
		List<Automovel> automoveis = new ArrayList<>();
		((Iterable<Automovel>) model.getAttribute("Automovel")).forEach(automoveis::add);

		assertEquals("lista", view);
		assertEquals(4, automoveis.size());

		verificar(automoveis.get(0), "GM", "EQUINOX", 2019, "AAA-0000");
		verificar(automoveis.get(1), "HONDA", "BROSS", 2022, "RDW-8H97");
		verificar(automoveis.get(2), "GM", "OMEGA", 1997, "MXH-6424");
		verificar(automoveis.get(3), "TOYOTA", "COROLA", 2004, "MGJ-6840");
	}

	@Test
	void cargaInicialNaoDuplicaOsDadosQuandoRodaDeNovo() {
		dadosIniciais.run();
		dadosIniciais.run();

		assertEquals(4, automovelRepository.count());
		assertEquals(3, marcaRepository.count());
		assertEquals(4, modeloRepository.count());
		assertEquals(5, opcionalRepository.count());
	}

	private void verificar(Automovel automovel, String marca, String modelo, Integer ano, String placa) {
		assertEquals(marca, automovel.getModelo().getMarca().getNome());
		assertEquals(modelo, automovel.getModelo().getNome());
		assertEquals(ano, automovel.getAno());
		assertEquals(placa, automovel.getPlaca());
	}

}
