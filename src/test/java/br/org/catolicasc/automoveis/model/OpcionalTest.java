package br.org.catolicasc.automoveis.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import br.org.catolicasc.automoveis.repository.AutomovelRepository;
import br.org.catolicasc.automoveis.repository.MarcaRepository;
import br.org.catolicasc.automoveis.repository.OpcionalRepository;
import jakarta.persistence.EntityManager;

/** Relacionamento N:M entre Automovel e Opcional (tabela automovel_opcional). */
@DataJpaTest
class OpcionalTest {

	@Autowired
	private AutomovelRepository automovelRepository;

	@Autowired
	private MarcaRepository marcaRepository;

	@Autowired
	private OpcionalRepository opcionalRepository;

	@Autowired
	private EntityManager entityManager;

	private Modelo uno;
	private Opcional ar;
	private Opcional airbag;

	@BeforeEach
	void prepararDados() {
		Marca fiat = new Marca("FIAT");
		uno = new Modelo("UNO");
		fiat.adicionarModelo(uno);
		marcaRepository.save(fiat);

		ar = opcionalRepository.save(new Opcional("Ar-condicionado"));
		airbag = opcionalRepository.save(new Opcional("Airbag"));
	}

	private void sincronizarComOBanco() {
		entityManager.flush();
		entityManager.clear();
	}

	@Test
	void salvaAutomovelComSeusOpcionais() {
		Automovel automovel = new Automovel(uno, 2010, "AAA-1111");
		automovel.adicionarOpcional(ar);
		automovel.adicionarOpcional(airbag);
		Automovel salvo = automovelRepository.save(automovel);
		sincronizarComOBanco();

		Automovel carregado = automovelRepository.findById(salvo.getId()).orElseThrow();
		assertEquals(2, carregado.getOpcionais().size());
	}

	@Test
	void opcionaisVoltamEmOrdemAlfabetica() {
		Automovel automovel = new Automovel(uno, 2010, "AAA-1111");
		automovel.adicionarOpcional(ar);
		automovel.adicionarOpcional(airbag);
		Automovel salvo = automovelRepository.save(automovel);
		sincronizarComOBanco();

		List<String> nomes = automovelRepository.findById(salvo.getId()).orElseThrow()
				.getOpcionais().stream().map(Opcional::getNome).toList();
		assertEquals(List.of("Airbag", "Ar-condicionado"), nomes);
	}

	@Test
	void mesmoOpcionalPodeEstarEmVariosAutomoveis() {
		Automovel primeiro = new Automovel(uno, 2010, "AAA-1111");
		primeiro.adicionarOpcional(ar);
		Automovel segundo = new Automovel(uno, 2015, "BBB-2222");
		segundo.adicionarOpcional(ar);
		segundo.adicionarOpcional(airbag);
		automovelRepository.save(primeiro);
		automovelRepository.save(segundo);
		sincronizarComOBanco();

		assertEquals(2, automovelRepository.findByOpcionaisNome("Ar-condicionado").size());
		assertEquals(1, automovelRepository.findByOpcionaisNome("Airbag").size());
		assertEquals(2, opcionalRepository.count());
	}

	@Test
	void removerOpcionalDoAutomovelNaoApagaOOpcional() {
		Automovel automovel = new Automovel(uno, 2010, "AAA-1111");
		automovel.adicionarOpcional(ar);
		automovel.adicionarOpcional(airbag);
		Automovel salvo = automovelRepository.save(automovel);
		sincronizarComOBanco();

		Automovel carregado = automovelRepository.findById(salvo.getId()).orElseThrow();
		Opcional airbagCarregado = carregado.getOpcionais().stream()
				.filter(o -> o.getNome().equals("Airbag"))
				.findFirst().orElseThrow();
		carregado.removerOpcional(airbagCarregado);
		automovelRepository.save(carregado);
		sincronizarComOBanco();

		assertEquals(1, automovelRepository.findById(salvo.getId()).orElseThrow().getOpcionais().size());
		assertTrue(opcionalRepository.findByNome("Airbag").isPresent());
	}

	@Test
	void nomeDoOpcionalNaoSeRepete() {
		assertThrows(DataIntegrityViolationException.class,
				() -> opcionalRepository.saveAndFlush(new Opcional("Airbag")));
	}
}
