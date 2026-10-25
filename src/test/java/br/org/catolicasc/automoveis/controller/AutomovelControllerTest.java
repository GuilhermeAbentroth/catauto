package br.org.catolicasc.automoveis.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import br.org.catolicasc.automoveis.model.Automovel;

@SpringBootTest
class AutomovelControllerTest {

	@Autowired
	private AutomovelController controller;

	@Test
	void listarRetornaAViewListaComOsAutomoveis() {
		Model model = new ConcurrentModel();

		String view = controller.listar(model);

		assertEquals("lista", view);
		assertNotNull(model.getAttribute("Automovel"));
	}

	@Test
	@SuppressWarnings("unchecked")
	void listarTrazOsQuatroAutomoveisIniciais() {
		Model model = new ConcurrentModel();

		controller.listar(model);
		List<Automovel> automoveis = new ArrayList<>();
		((Iterable<Automovel>) model.getAttribute("Automovel")).forEach(automoveis::add);

		assertEquals(4, automoveis.size());
		assertEquals("GM", automoveis.get(0).getModelo().getMarca().getNome());
		assertEquals("EQUINOX", automoveis.get(0).getModelo().getNome());
		assertEquals(2019, automoveis.get(0).getAno());
		assertEquals("AAA-0000", automoveis.get(0).getPlaca());
	}

	@Test
	void rotaRaizResponde200ComAViewLista() throws Exception {
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(view().name("lista"))
				.andExpect(model().attributeExists("Automovel"));
	}
}
