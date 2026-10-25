package br.org.catolicasc.automoveis.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.org.catolicasc.automoveis.model.Cliente;
import br.org.catolicasc.automoveis.repository.ClienteRepository;

@RestController
@RequestMapping("/api/clientes")
public class ClienteApiController {

    public record ClienteDto(Long id, String nome, String email, String telefone) {
        static ClienteDto de(Cliente c) {
            return new ClienteDto(c.getId(), c.getNome(), c.getEmail(), c.getTelefone());
        }
    }

    private final ClienteRepository repository;

    public ClienteApiController(ClienteRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<ClienteDto> listar() {
        return repository.findAll().stream().map(ClienteDto::de).toList();
    }

    @GetMapping("/{id}")
    public ClienteDto buscar(@PathVariable Long id) {
        return ClienteDto.de(obter(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteDto criar(@RequestBody ClienteDto dto) {
        return ClienteDto.de(repository.save(new Cliente(dto.nome(), dto.email(), dto.telefone())));
    }

    @PutMapping("/{id}")
    public ClienteDto atualizar(@PathVariable Long id, @RequestBody ClienteDto dto) {
        Cliente cliente = obter(id);
        cliente.setNome(dto.nome());
        cliente.setEmail(dto.email());
        cliente.setTelefone(dto.telefone());
        return ClienteDto.de(repository.save(cliente));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        repository.delete(obter(id));
    }

    private Cliente obter(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente nao encontrado"));
    }
}
