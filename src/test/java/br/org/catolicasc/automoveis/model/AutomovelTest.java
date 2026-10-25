package br.org.catolicasc.automoveis.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import br.org.catolicasc.automoveis.repository.AutomovelRepository;
import br.org.catolicasc.automoveis.repository.MarcaRepository;
import jakarta.persistence.EntityManager;

@DataJpaTest
class AutomovelTest {

	@Autowired
	private AutomovelRepository automovelRepository;

	@Autowired
	private MarcaRepository marcaRepository;

	@Autowired
	private EntityManager entityManager;

	private Modelo uno;

	@BeforeEach
	void prepararMarcaEModelo() {
		Marca fiat = new Marca("FIAT");
		uno = new Modelo("UNO");
		fiat.adicionarModelo(uno);
		marcaRepository.save(fiat);
	}

	private void sincronizarComOBanco() {
		entityManager.flush();
		entityManager.clear();
	}

	@Test
	void construtorEGettersPreenchemOsCampos() {
		Automovel automovel = new Automovel(uno, 1995, "555JK");

		assertEquals(uno, automovel.getModelo());
		assertEquals(1995, automovel.getAno());
		assertEquals("555JK", automovel.getPlaca());
		assertNull(automovel.getId());
	}

	@Test
	void salvaAutomovelEGeraId() {
		Automovel salvo = automovelRepository.save(new Automovel(uno, 1995, "555JK"));

		assertNotNull(salvo.getId());
	}

	@Test
	void salvaAutomovelLigadoAoModeloEAMarca() {
		Automovel salvo = automovelRepository.save(new Automovel(uno, 1995, "555JK"));
		sincronizarComOBanco();

		Automovel carregado = automovelRepository.findById(salvo.getId()).orElseThrow();

		assertEquals(1995, carregado.getAno());
		assertEquals("555JK", carregado.getPlaca());
		assertEquals("UNO", carregado.getModelo().getNome());
		assertEquals("FIAT", carregado.getModelo().getMarca().getNome());
	}

	@Test
	void buscaAutomovelPorPlaca() {
		automovelRepository.save(new Automovel(uno, 1995, "555JK"));
		sincronizarComOBanco();

		assertTrue(automovelRepository.findByPlaca("555JK").isPresent());
		assertFalse(automovelRepository.findByPlaca("ZZZ-9999").isPresent());
	}

	@Test
	void listaTodosOsAutomoveis() {
		automovelRepository.save(new Automovel(uno, 1995, "AAA-1111"));
		automovelRepository.save(new Automovel(uno, 2001, "BBB-2222"));
		automovelRepository.save(new Automovel(uno, 2010, "CCC-3333"));

		List<Automovel> todos = new ArrayList<>();
		automovelRepository.findAll().forEach(todos::add);

		assertEquals(3, todos.size());
	}

	@Test
	void atualizaAnoDoAutomovel() {
		Automovel salvo = automovelRepository.save(new Automovel(uno, 1995, "555JK"));
		sincronizarComOBanco();

		Automovel carregado = automovelRepository.findById(salvo.getId()).orElseThrow();
		carregado.setAno(1996);
		automovelRepository.save(carregado);
		sincronizarComOBanco();

		assertEquals(1996, automovelRepository.findById(salvo.getId()).orElseThrow().getAno());
	}

	@Test
	void excluiAutomovelMasMantemModeloEMarca() {
		Automovel salvo = automovelRepository.save(new Automovel(uno, 1995, "555JK"));
		sincronizarComOBanco();

		automovelRepository.deleteById(salvo.getId());
		sincronizarComOBanco();

		assertEquals(0, automovelRepository.count());
		assertEquals(1, marcaRepository.count());
	}
}
