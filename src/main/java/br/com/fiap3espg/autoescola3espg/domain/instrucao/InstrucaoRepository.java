package br.com.fiap3espg.autoescola3espg.domain.instrucao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface InstrucaoRepository extends JpaRepository<Instrucao, Long> {
    boolean existsByInstrutorIdAndDataHora(Long idInstrutor, LocalDateTime dataHora);

    boolean existsByAlunoIdAndDataHoraBetween(Long idAluno, LocalDateTime inicioExpediente, LocalDateTime fimExpediente);
}
