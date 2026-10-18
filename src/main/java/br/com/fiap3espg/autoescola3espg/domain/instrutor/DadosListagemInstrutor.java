package br.com.fiap3espg.autoescola3espg.domain.instrutor;

public record DadosListagemInstrutor(
        Long id,
        String nome,
        String email,
        Especialidade especialidade) {
    public DadosListagemInstrutor(Instrutor instrutor) {
        this(
                instrutor.getId(),
                instrutor.getNome(),
                instrutor.getEmail(),
                instrutor.getEspecialidade()
        );
    }
}