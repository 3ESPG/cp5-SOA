package br.com.fiap3espg.autoescola3espg.integration.cep;

public class CepNaoEncontradoException extends RuntimeException {
    public CepNaoEncontradoException(String mensagem) { super(mensagem); }
}
