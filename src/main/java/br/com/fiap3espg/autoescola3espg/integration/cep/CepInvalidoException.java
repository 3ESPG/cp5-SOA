package br.com.fiap3espg.autoescola3espg.integration.cep;

public class CepInvalidoException extends RuntimeException {
    public CepInvalidoException(String mensagem) { super(mensagem); }
}
