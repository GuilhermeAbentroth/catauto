package br.org.catolicasc.automoveis.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate dataPedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    /** 1:1 - a chave estrangeira fica em pagamento.pedido_id. */
    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private Pagamento pagamento;

    public Pedido() {
    }

    public Pedido(Cliente cliente, LocalDate dataPedido) {
        this.cliente = cliente;
        this.dataPedido = dataPedido;
    }

    /** O automovel vendido sai do estoque; vender um automovel ja vendido lanca IllegalStateException. */
    public void adicionarItem(Automovel automovel) {
        automovel.registrarVenda();
        itens.add(new ItemPedido(this, automovel));
    }

    /** Antes de remover o pedido: os automoveis voltam ao estoque. */
    public void cancelar() {
        itens.forEach(item -> item.getAutomovel().devolverAoEstoque());
    }

    public BigDecimal getTotal() {
        return itens.stream()
                .map(ItemPedido::getPrecoUnitario)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Registra o pagamento pelo total atual; chamar depois de adicionar os automoveis. */
    public Pagamento registrarPagamento(FormaPagamento forma, int parcelas) {
        if (pagamento != null) {
            throw new IllegalStateException("O pedido ja possui pagamento");
        }
        pagamento = new Pagamento(this, forma, parcelas, getTotal());
        return pagamento;
    }

    public Pagamento getPagamento() {
        return pagamento;
    }

    public void setPagamento(Pagamento pagamento) {
        this.pagamento = pagamento;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDataPedido() {
        return dataPedido;
    }

    public void setDataPedido(LocalDate dataPedido) {
        this.dataPedido = dataPedido;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
    }
}
