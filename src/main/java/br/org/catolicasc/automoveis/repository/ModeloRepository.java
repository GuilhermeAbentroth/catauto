package br.org.catolicasc.automoveis.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.org.catolicasc.automoveis.model.Marca;
import br.org.catolicasc.automoveis.model.Modelo;

@Repository
public interface ModeloRepository extends JpaRepository<Modelo, Long> {

    List<Modelo> findByMarcaNome(String nomeDaMarca);

    Optional<Modelo> findByNomeAndMarca(String nome, Marca marca);
}
