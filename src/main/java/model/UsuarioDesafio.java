package model;

import java.time.LocalDateTime;

/** Representa o vínculo N:N entre usuário e desafio (tabela usuario_desafio). */
public class UsuarioDesafio {
    private int usuarioId;
    private int desafioId;
    private boolean concluido;
    private boolean desistiu;
    private LocalDateTime dataParticipacao;

    // preenchidos via join, apenas para exibição
    private String nomeUsuario;
    private Desafio desafio;

    public UsuarioDesafio() {}

    public UsuarioDesafio(int usuarioId, int desafioId) {
        this.usuarioId = usuarioId;
        this.desafioId = desafioId;
    }

    public int getUsuarioId()                       { return usuarioId; }
    public void setUsuarioId(int usuarioId)         { this.usuarioId = usuarioId; }

    public int getDesafioId()                       { return desafioId; }
    public void setDesafioId(int desafioId)         { this.desafioId = desafioId; }

    public boolean isConcluido()                    { return concluido; }
    public void setConcluido(boolean concluido)     { this.concluido = concluido; }

    public boolean isDesistiu()                     { return desistiu; }
    public void setDesistiu(boolean desistiu)       { this.desistiu = desistiu; }

    public LocalDateTime getDataParticipacao()               { return dataParticipacao; }
    public void setDataParticipacao(LocalDateTime d)         { this.dataParticipacao = d; }

    public String getNomeUsuario()                  { return nomeUsuario; }
    public void setNomeUsuario(String nomeUsuario)  { this.nomeUsuario = nomeUsuario; }

    public Desafio getDesafio()                     { return desafio; }
    public void setDesafio(Desafio desafio)         { this.desafio = desafio; }
}
