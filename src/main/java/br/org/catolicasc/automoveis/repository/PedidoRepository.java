package br.org.catolicasc.automoveis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.org.catolicasc.automoveis.model.Cliente;
import br.org.catolicasc.automoveis.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByCliente(Cliente cliente);
}
