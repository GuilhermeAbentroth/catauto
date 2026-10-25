package br.org.catolicasc.automoveis.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import br.org.catolicasc.automoveis.repository.MarcaRepository;
import br.org.catolicasc.automoveis.repository.ModeloRepository;
import jakarta.persistence.EntityManager;

@DataJpaTest
class MarcaTest {

	@Autowired
	private MarcaRepository marcaRepository;

	@Autowired
	private ModeloRepository modeloRepository;

	@Autowired
	private EntityManager entityManager;

	private void sincronizarComOBanco() {
		entityManager.flush();
		entityManager.clear();
	}

	@Test
	void adicionarModeloLigaOsDoisLados() {
		Marca fiat = new Marca("FIAT");
		Modelo uno = new Modelo("UNO");

		fiat.adicionarModelo(uno);

		assertSame(fiat, uno.getMarca());
		assertEquals(1, fiat.getModelos().size());
	}

	@Test
	void salvaMarcaEGeraId() {
		Marca marca = marcaRepository.save(new Marca("FIAT"));

		assertNotNull(marca.getId());
	}

	@Test
	void salvaMarcaComSeusModelosEmCascata() {
		Marca fiat = new Marca("FIAT");
		fiat.adicionarModelo(new Modelo("UNO"));
		fiat.adicionarModelo(new Modelo("PALIO"));

		Marca salva = marcaRepository.save(fiat);
		sincronizarComOBanco();

		Marca carregada = marcaRepository.findById(salva.getId()).orElseThrow();
		assertEquals(2, carregada.getModelos().size());
		carregada.getModelos().forEach(modelo -> assertNotNull(modelo.getId()));
		assertEquals(2, modeloRepository.count());
	}

	@Test
	void buscaMarcaPorNome() {
		marcaRepository.save(new Marca("HONDA"));

		assertTrue(marcaRepository.findByNome("HONDA").isPresent());
		assertFalse(marcaRepository.findByNome("INEXISTENTE").isPresent());
	}

	@Test
	void excluirMarcaRemoveSeusModelos() {
		Marca fiat = new Marca("FIAT");
		fiat.adicionarModelo(new Modelo("UNO"));
		Marca salva = marcaRepository.save(fiat);
		sincronizarComOBanco();

		marcaRepository.deleteById(salva.getId());
		sincronizarComOBanco();

		assertEquals(0, marcaRepository.count());
		assertEquals(0, modeloRepository.count());
	}
}
