package br.org.catolicasc.automoveis.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import br.org.catolicasc.automoveis.repository.MarcaRepository;
import br.org.catolicasc.automoveis.repository.ModeloRepository;
import jakarta.persistence.EntityManager;

@DataJpaTest
class ModeloTest {

	@Autowired
	private MarcaRepository marcaRepository;

	@Autowired
	private ModeloRepository modeloRepository;

	@Autowired
	private EntityManager entityManager;

	@BeforeEach
	void prepararMarcas() {
		Marca gm = new Marca("GM");
		gm.adicionarModelo(new Modelo("EQUINOX"));
		gm.adicionarModelo(new Modelo("OMEGA"));

		Marca honda = new Marca("HONDA");
		honda.adicionarModelo(new Modelo("BROSS"));

		marcaRepository.save(gm);
		marcaRepository.save(honda);
		entityManager.flush();
		entityManager.clear();
	}

	@Test
	void modeloSalvoConheceASuaMarca() {
		Modelo bross = modeloRepository.findByMarcaNome("HONDA").get(0);

		assertEquals("BROSS", bross.getNome());
		assertEquals("HONDA", bross.getMarca().getNome());
	}

	@Test
	void listaOsModelosDeCadaMarca() {
		List<Modelo> modelosGm = modeloRepository.findByMarcaNome("GM");
		List<Modelo> modelosHonda = modeloRepository.findByMarcaNome("HONDA");

		assertEquals(2, modelosGm.size());
		assertEquals(1, modelosHonda.size());
		assertTrue(modelosGm.stream().anyMatch(m -> m.getNome().equals("EQUINOX")));
		assertTrue(modelosGm.stream().anyMatch(m -> m.getNome().equals("OMEGA")));
	}

	@Test
	void excluirModeloNaoRemoveAMarca() {
		Modelo omega = modeloRepository.findByMarcaNome("GM").stream()
				.filter(m -> m.getNome().equals("OMEGA"))
				.findFirst().orElseThrow();

		modeloRepository.deleteById(omega.getId());
		entityManager.flush();
		entityManager.clear();

		assertEquals(3 - 1, modeloRepository.count());
		assertEquals(2, marcaRepository.count());
		assertEquals(1, modeloRepository.findByMarcaNome("GM").size());
	}
}
