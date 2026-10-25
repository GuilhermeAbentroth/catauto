package br.org.catolicasc.automoveis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.org.catolicasc.automoveis.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByEmail(String email);
}
