package br.com.fiap3espg.autoescola3espg.integration.cep;

public class CepIndisponivelException extends RuntimeException {
    public CepIndisponivelException(String mensagem, Throwable causa) { super(mensagem, causa); }
}
