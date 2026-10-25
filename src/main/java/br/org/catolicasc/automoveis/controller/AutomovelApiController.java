package br.org.catolicasc.automoveis.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.org.catolicasc.automoveis.model.Automovel;
import br.org.catolicasc.automoveis.model.Marca;
import br.org.catolicasc.automoveis.model.Modelo;
import br.org.catolicasc.automoveis.model.Opcional;
import br.org.catolicasc.automoveis.repository.AutomovelRepository;
import br.org.catolicasc.automoveis.repository.MarcaRepository;
import br.org.catolicasc.automoveis.repository.ModeloRepository;
import br.org.catolicasc.automoveis.repository.OpcionalRepository;

@RestController
@RequestMapping("/api/automoveis")
public class AutomovelApiController {

    public record AutomovelDto(Long id, Long modeloId, String modelo, String marca, Integer ano, String placa,
            BigDecimal preco, List<Long> opcionaisIds, List<String> opcionais, Boolean vendido) {
        static AutomovelDto de(Automovel a) {
            Modelo m = a.getModelo();
            return new AutomovelDto(a.getId(),
                    m == null ? null : m.getId(),
                    m == null ? null : m.getNome(),
                    m == null || m.getMarca() == null ? null : m.getMarca().getNome(),
                    a.getAno(), a.getPlaca(), a.getPreco(),
                    a.getOpcionais().stream().map(Opcional::getId).toList(),
                    a.getOpcionais().stream().map(Opcional::getNome).toList(),
                    a.isVendido());
        }
    }

    private final AutomovelRepository repository;
    private final ModeloRepository modeloRepository;
    private final MarcaRepository marcaRepository;
    private final OpcionalRepository opcionalRepository;

    public AutomovelApiController(AutomovelRepository repository, ModeloRepository modeloRepository,
            MarcaRepository marcaRepository, OpcionalRepository opcionalRepository) {
        this.repository = repository;
        this.modeloRepository = modeloRepository;
        this.marcaRepository = marcaRepository;
        this.opcionalRepository = opcionalRepository;
    }

    /**
     * Lista o estoque (automoveis nao vendidos); ?incluirVendidos=true lista todos.
     * Com ?opcional=Airbag lista so os automoveis que tem aquele opcional.
     */
    @GetMapping
    @Transactional(readOnly = true)
    public List<AutomovelDto> listar(@RequestParam(required = false) String opcional,
            @RequestParam(defaultValue = "false") boolean incluirVendidos) {
        List<Automovel> automoveis = opcional != null
                ? repository.findByOpcionaisNome(opcional)
                : incluirVendidos ? repository.findAll() : repository.findByVendidoFalse();
        return automoveis.stream()
                .filter(a -> incluirVendidos || !a.isVendido())
                .map(AutomovelDto::de).toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public AutomovelDto buscar(@PathVariable Long id) {
        return AutomovelDto.de(obter(id));
    }

    @GetMapping("/placa/{placa}")
    @Transactional(readOnly = true)
    public AutomovelDto buscarPorPlaca(@PathVariable String placa) {
        return repository.findByPlaca(placa).map(AutomovelDto::de)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Automovel nao encontrado"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public AutomovelDto criar(@RequestBody AutomovelDto dto) {
        Automovel automovel = new Automovel(obterModelo(dto), dto.ano(), dto.placa(), dto.preco());
        obterOpcionais(dto.opcionaisIds()).forEach(automovel::adicionarOpcional);
        return AutomovelDto.de(repository.save(automovel));
    }

    /** PUT substitui o recurso inteiro, inclusive a lista de opcionais. */
    @PutMapping("/{id}")
    @Transactional
    public AutomovelDto atualizar(@PathVariable Long id, @RequestBody AutomovelDto dto) {
        Automovel automovel = obter(id);
        automovel.setModelo(obterModelo(dto));
        automovel.setAno(dto.ano());
        automovel.setPlaca(dto.placa());
        automovel.setPreco(dto.preco());
        automovel.getOpcionais().clear();
        obterOpcionais(dto.opcionaisIds()).forEach(automovel::adicionarOpcional);
        return AutomovelDto.de(repository.saveAndFlush(automovel));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        repository.delete(obter(id));
    }

    private Automovel obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Automovel nao encontrado"));
    }

    /**
     * Usa o modeloId quando informado; senao procura a marca e o modelo pelo nome
     * (em maiusculas) e cadastra o que ainda nao existir.
     */
    private Modelo obterModelo(AutomovelDto dto) {
        if (dto.modeloId() != null) {
            return modeloRepository.findById(dto.modeloId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Modelo inexistente"));
        }
        String nomeMarca = normalizar(dto.marca());
        String nomeModelo = normalizar(dto.modelo());
        if (nomeMarca == null || nomeModelo == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a marca e o modelo");
        }
        Marca marca = marcaRepository.findByNome(nomeMarca)
                .orElseGet(() -> marcaRepository.save(new Marca(nomeMarca)));
        return modeloRepository.findByNomeAndMarca(nomeModelo, marca).orElseGet(() -> {
            Modelo modelo = new Modelo(nomeModelo);
            marca.adicionarModelo(modelo);
            return modeloRepository.save(modelo);
        });
    }

    private static String normalizar(String nome) {
        return nome == null || nome.isBlank() ? null : nome.trim().toUpperCase();
    }

    /** Sem opcionaisIds = automovel sem opcionais; id que nao existe = 400. */
    private List<Opcional> obterOpcionais(List<Long> ids) {
        if (ids == null) {
            return List.of();
        }
        return ids.stream()
                .map(id -> opcionalRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Opcional inexistente")))
                .toList();
    }
}
