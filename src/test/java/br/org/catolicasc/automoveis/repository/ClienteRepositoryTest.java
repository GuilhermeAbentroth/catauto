package br.org.catolicasc.automoveis.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import br.org.catolicasc.automoveis.model.Cliente;

@DataJpaTest
class ClienteRepositoryTest {

	@Autowired
	private ClienteRepository clienteRepository;

	@Test
	void salvaClienteEGeraId() {
		Cliente salvo = clienteRepository.save(new Cliente("Maria", "maria@gmail.com", "47 99999-9999"));

		assertNotNull(salvo.getId());
	}

	@Test
	void buscaClientePorId() {
		Cliente salvo = clienteRepository.save(new Cliente("Joao", "joao@gmail.com", "47 98888-8888"));

		Cliente encontrado = clienteRepository.findById(salvo.getId()).orElseThrow();

		assertEquals("Joao", encontrado.getNome());
		assertEquals("joao@gmail.com", encontrado.getEmail());
		assertEquals("47 98888-8888", encontrado.getTelefone());
	}

	@Test
	void buscaClientePorEmail() {
		clienteRepository.save(new Cliente("Carlos", "carlos@gmail.com", "47 97777-7777"));

		Optional<Cliente> encontrado = clienteRepository.findByEmail("carlos@gmail.com");
		Optional<Cliente> inexistente = clienteRepository.findByEmail("ninguem@gmail.com");

		assertTrue(encontrado.isPresent());
		assertEquals("Carlos", encontrado.get().getNome());
		assertFalse(inexistente.isPresent());
	}

	@Test
	void listaTodosOsClientes() {
		clienteRepository.save(new Cliente("Maria", "maria@gmail.com", "1"));
		clienteRepository.save(new Cliente("Joao", "joao@gmail.com", "2"));
		clienteRepository.save(new Cliente("Carlos", "carlos@gmail.com", "3"));

		List<Cliente> todos = new ArrayList<>();
		clienteRepository.findAll().forEach(todos::add);

		assertEquals(3, todos.size());
	}

	@Test
	void atualizaCliente() {
		Cliente salvo = clienteRepository.save(new Cliente("Guilherme", "gui@gmail.com", "47 99212-9013"));

		salvo.setTelefone("47 90000-0000");
		clienteRepository.save(salvo);

		Cliente atualizado = clienteRepository.findById(salvo.getId()).orElseThrow();
		assertEquals("47 90000-0000", atualizado.getTelefone());
	}

	@Test
	void excluiCliente() {
		Cliente salvo = clienteRepository.save(new Cliente("Maria", "maria@gmail.com", "1"));

		clienteRepository.deleteById(salvo.getId());

		assertFalse(clienteRepository.findById(salvo.getId()).isPresent());
		assertEquals(0, clienteRepository.count());
	}
}
