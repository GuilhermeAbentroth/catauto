package br.org.catolicasc.automoveis.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

/**
 * Pagamento de um pedido (1:1). O valor e o total do pedido no momento em que
 * o pagamento foi registrado.
 */
@Entity
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FormaPagamento forma;

    @Column(nullable = false)
    private Integer parcelas;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    public Pagamento() {
    }

    public Pagamento(Pedido pedido, FormaPagamento forma, int parcelas, BigDecimal valor) {
        if (forma == null) {
            throw new IllegalArgumentException("A forma de pagamento e obrigatoria");
        }
        if (parcelas < 1) {
            throw new IllegalArgumentException("O numero de parcelas deve ser no minimo 1");
        }
        if (forma == FormaPagamento.A_VISTA && parcelas != 1) {
            throw new IllegalArgumentException("Pagamento a vista deve ter 1 parcela");
        }
        this.pedido = pedido;
        this.forma = forma;
        this.parcelas = parcelas;
        this.valor = valor;
    }

    public BigDecimal getValorParcela() {
        return valor.divide(BigDecimal.valueOf(parcelas), 2, RoundingMode.HALF_EVEN);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public FormaPagamento getForma() {
        return forma;
    }

    public void setForma(FormaPagamento forma) {
        this.forma = forma;
    }

    public Integer getParcelas() {
        return parcelas;
    }

    public void setParcelas(Integer parcelas) {
        this.parcelas = parcelas;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}
