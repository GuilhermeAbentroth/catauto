package br.org.catolicasc.automoveis.model;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderBy;

@Entity
public class Automovel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "modelo_id", nullable = false)
    private Modelo modelo;

    @Column(nullable = false)
    private Integer ano;

    @Column(nullable = false, unique = true, length = 8)
    private String placa;

    @Column(precision = 12, scale = 2)
    private BigDecimal preco;

    /** Vendido sai do estoque. O DEFAULT deixa o Hibernate criar a coluna em bancos que ja tem automoveis. */
    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean vendido;

    /** N:M - a tabela automovel_opcional guarda os pares (automovel, opcional). */
    @ManyToMany
    @JoinTable(name = "automovel_opcional",
            joinColumns = @JoinColumn(name = "automovel_id"),
            inverseJoinColumns = @JoinColumn(name = "opcional_id"))
    @OrderBy("nome")
    private Set<Opcional> opcionais = new LinkedHashSet<>();

    public Automovel() {
    }

    public Automovel(Modelo modelo, Integer ano, String placa) {
        this.modelo = modelo;
        this.ano = ano;
        this.placa = placa;
    }

    public Automovel(Modelo modelo, Integer ano, String placa, BigDecimal preco) {
        this(modelo, ano, placa);
        this.preco = preco;
    }

    public void adicionarOpcional(Opcional opcional) {
        opcionais.add(opcional);
    }

    public void removerOpcional(Opcional opcional) {
        opcionais.remove(opcional);
    }

    public void registrarVenda() {
        if (vendido) {
            throw new IllegalStateException("O automovel " + placa + " ja foi vendido");
        }
        vendido = true;
    }

    /** Usado quando o pedido e cancelado. */
    public void devolverAoEstoque() {
        vendido = false;
    }

    public boolean isVendido() {
        return vendido;
    }

    /** Ex.: "GM EQUINOX 2019". */
    public String getDescricao() {
        return modelo.getMarca().getNome() + " " + modelo.getNome() + " " + ano;
    }

    public Set<Opcional> getOpcionais() {
        return opcionais;
    }

    public void setOpcionais(Set<Opcional> opcionais) {
        this.opcionais = opcionais;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Modelo getModelo() {
        return modelo;
    }

    public void setModelo(Modelo modelo) {
        this.modelo = modelo;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }
}
