package br.com.fiap3espg.autoescola3espg.infra.exception;

import br.com.fiap3espg.autoescola3espg.domain.aluno.AlunoNotFoundException;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.InstrucaoNotFoundException;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.ValidacaoException;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.InstrutorNotFoundException;
import br.com.fiap3espg.autoescola3espg.domain.usuario.UsuarioNotFoundException;
import br.com.fiap3espg.autoescola3espg.integration.cep.CepIndisponivelException;
import br.com.fiap3espg.autoescola3espg.integration.cep.CepInvalidoException;
import br.com.fiap3espg.autoescola3espg.integration.cep.CepNaoEncontradoException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class TratadorGlobalErros {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Void> tratarNotFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<DadosBadRequest>> tratarBadRequest(MethodArgumentNotValidException ex) {
        List<DadosBadRequest> erros = ex.getFieldErrors().stream().map(DadosBadRequest::new).toList();
        return ResponseEntity.badRequest().body(erros);
    }

    @ExceptionHandler({InstrutorNotFoundException.class, AlunoNotFoundException.class,
            UsuarioNotFoundException.class, InstrucaoNotFoundException.class})
    public ResponseEntity<DadosMessage> tratarNaoEncontrado(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new DadosMessage(ex.getMessage()));
    }

    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<DadosMessage> tratarValidacao(ValidacaoException ex) {
        return ResponseEntity.badRequest().body(new DadosMessage(ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<DadosMessage> tratarIntegridade(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new DadosMessage("Violação de integridade: e-mail, CPF, CNH ou login já cadastrado, ou registro em uso."));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<DadosMessage> tratarAutenticacao(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new DadosMessage("Login ou senha inválidos"));
    }

    // ---- Integração ViaCEP ----
    @ExceptionHandler(CepInvalidoException.class)
    public ResponseEntity<DadosMessage> tratarCepInvalido(CepInvalidoException ex) {
        return ResponseEntity.badRequest().body(new DadosMessage(ex.getMessage()));
    }

    @ExceptionHandler(CepNaoEncontradoException.class)
    public ResponseEntity<DadosMessage> tratarCepNaoEncontrado(CepNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new DadosMessage(ex.getMessage()));
    }

    @ExceptionHandler(CepIndisponivelException.class)
    public ResponseEntity<DadosMessage> tratarCepIndisponivel(CepIndisponivelException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new DadosMessage(ex.getMessage()));
    }

    public record DadosBadRequest(String field, String message) {
        public DadosBadRequest(FieldError erro) {
            this(erro.getField(), erro.getDefaultMessage());
        }
    }

    public record DadosMessage(String message) {
    }
}
