package br.org.catolicasc.automoveis.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.org.catolicasc.automoveis.model.Automovel;
import br.org.catolicasc.automoveis.model.Cliente;
import br.org.catolicasc.automoveis.model.FormaPagamento;
import br.org.catolicasc.automoveis.model.ItemPedido;
import br.org.catolicasc.automoveis.model.Pagamento;
import br.org.catolicasc.automoveis.model.Pedido;
import br.org.catolicasc.automoveis.repository.AutomovelRepository;
import br.org.catolicasc.automoveis.repository.ClienteRepository;
import br.org.catolicasc.automoveis.repository.PedidoRepository;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoApiController {

    /** Corpo do POST: o cliente, os ids dos automoveis comprados e o pagamento (padrao: a vista, 1 parcela). */
    public record PedidoRequest(Long clienteId, List<Long> automoveisIds, FormaPagamento formaPagamento,
            Integer parcelas) {
    }

    public record ItemDto(Long automovelId, String automovel, String placa, BigDecimal preco) {
        static ItemDto de(ItemPedido i) {
            Automovel a = i.getAutomovel();
            return new ItemDto(a.getId(), a.getDescricao(), a.getPlaca(), i.getPrecoUnitario());
        }
    }

    public record PagamentoDto(FormaPagamento forma, Integer parcelas, BigDecimal valor, BigDecimal valorParcela) {
        static PagamentoDto de(Pagamento p) {
            return p == null ? null
                    : new PagamentoDto(p.getForma(), p.getParcelas(), p.getValor(), p.getValorParcela());
        }
    }

    public record PedidoDto(Long id, LocalDate dataPedido, Long clienteId, String cliente, List<ItemDto> itens,
            BigDecimal total, PagamentoDto pagamento) {
        static PedidoDto de(Pedido p) {
            return new PedidoDto(p.getId(), p.getDataPedido(), p.getCliente().getId(), p.getCliente().getNome(),
                    p.getItens().stream().map(ItemDto::de).toList(), p.getTotal(),
                    PagamentoDto.de(p.getPagamento()));
        }
    }

    private final PedidoRepository repository;
    private final ClienteRepository clienteRepository;
    private final AutomovelRepository automovelRepository;

    public PedidoApiController(PedidoRepository repository, ClienteRepository clienteRepository,
            AutomovelRepository automovelRepository) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
        this.automovelRepository = automovelRepository;
    }

    /** Sem parametro lista todos; com ?clienteId=1 lista so os pedidos daquele cliente. */
    @GetMapping
    @Transactional(readOnly = true)
    public List<PedidoDto> listar(@RequestParam(required = false) Long clienteId) {
        List<Pedido> pedidos = clienteId == null
                ? repository.findAll()
                : repository.findByCliente(obterCliente(clienteId));
        return pedidos.stream().map(PedidoDto::de).toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public PedidoDto buscar(@PathVariable Long id) {
        return PedidoDto.de(obter(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public PedidoDto criar(@RequestBody PedidoRequest req) {
        if (req.automoveisIds() == null || req.automoveisIds().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O pedido precisa de ao menos um automovel");
        }
        Pedido pedido = new Pedido(obterCliente(req.clienteId()), LocalDate.now());
        for (Long id : req.automoveisIds()) {
            Automovel automovel = automovelRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Automovel inexistente"));
            pedido.adicionarItem(automovel);
        }
        FormaPagamento forma = req.formaPagamento() == null ? FormaPagamento.A_VISTA : req.formaPagamento();
        pedido.registrarPagamento(forma, req.parcelas() == null ? 1 : req.parcelas());
        return PedidoDto.de(repository.save(pedido));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void remover(@PathVariable Long id) {
        Pedido pedido = obter(id);
        pedido.cancelar();
        repository.delete(pedido);
    }

    private Pedido obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido nao encontrado"));
    }

    private Cliente obterCliente(Long clienteId) {
        if (clienteId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clienteId e obrigatorio");
        }
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente inexistente"));
    }
}
