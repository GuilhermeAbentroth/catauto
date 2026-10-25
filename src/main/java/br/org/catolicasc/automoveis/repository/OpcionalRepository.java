package br.org.catolicasc.automoveis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.org.catolicasc.automoveis.model.Opcional;

@Repository
public interface OpcionalRepository extends JpaRepository<Opcional, Long> {

    Optional<Opcional> findByNome(String nome);
}
