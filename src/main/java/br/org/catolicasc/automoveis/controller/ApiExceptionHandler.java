package br.org.catolicasc.automoveis.controller;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Padroniza os erros da API no formato {"erro": "mensagem"}:
 * 404/400 lancados pelos controllers, 400 para regra de negocio violada,
 * 409 para estado invalido (automovel ja vendido) e para restricao do banco
 * (registro em uso, valor duplicado, campo obrigatorio).
 */
@RestControllerAdvice(basePackages = "br.org.catolicasc.automoveis.controller")
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> status(ResponseStatusException e) {
        String mensagem = e.getReason() == null ? "Requisicao invalida" : e.getReason();
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("erro", mensagem));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> regraDeNegocio(IllegalArgumentException e) {
        String mensagem = e.getMessage() == null ? "Requisicao invalida" : e.getMessage();
        return ResponseEntity.badRequest().body(Map.of("erro", mensagem));
    }

    /** Ex.: vender um automovel que ja foi vendido. */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> estadoInvalido(IllegalStateException e) {
        String mensagem = e.getMessage() == null ? "Operacao invalida" : e.getMessage();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", mensagem));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> conflito(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro",
                "Operacao viola a integridade dos dados (registro em uso, valor duplicado ou campo obrigatorio ausente)."));
    }
}
