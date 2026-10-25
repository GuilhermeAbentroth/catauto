package br.org.catolicasc.automoveis.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.org.catolicasc.automoveis.model.Automovel;

@Repository
public interface AutomovelRepository extends JpaRepository<Automovel, Long> {

    Optional<Automovel> findByPlaca(String placa);

    /** Navega pela tabela automovel_opcional (N:M). */
    List<Automovel> findByOpcionaisNome(String nomeDoOpcional);

    /** Estoque: automoveis ainda nao vendidos. */
    List<Automovel> findByVendidoFalse();

    /** Pedidos feitos antes do controle de estoque: marca como vendido quem ja esta em algum pedido. */
    @Modifying
    @Transactional
    @Query("update Automovel a set a.vendido = true "
            + "where a.vendido = false and exists (select i from ItemPedido i where i.automovel = a)")
    int marcarVendidosEmPedidos();
}
