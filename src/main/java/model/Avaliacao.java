package model;

import java.time.LocalDateTime;

public class Avaliacao {
    private int id;
    private int usuarioId;          // FK → usuario
    private int livroId;            // FK → livro
    private int nota;                // 1–5
    private String comentario;      // opcional
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    // preenchidos pelo DAO via join, apenas para exibição
    private String nomeUsuario;
    private String nomeLivro;

    public Avaliacao() {}

    public Avaliacao(int usuarioId, int livroId, int nota, String comentario) {
        this.usuarioId  = usuarioId;
        this.livroId    = livroId;
        this.nota       = nota;
        this.comentario = comentario;
    }

    // ── Getters & Setters ───────────────────────────────────────
    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }

    public int getUsuarioId()               { return usuarioId; }
    public void setUsuarioId(int u)         { this.usuarioId = u; }

    public int getLivroId()                 { return livroId; }
    public void setLivroId(int l)           { this.livroId = l; }

    public int getNota()                    { return nota; }
    public void setNota(int nota)           { this.nota = nota; }

    public String getComentario()           { return comentario; }
    public void setComentario(String c)     { this.comentario = c; }


    public LocalDateTime getCriadoEm()                      { return criadoEm; }
    public void setCriadoEm(LocalDateTime c)                { this.criadoEm = c; }

    public LocalDateTime getAtualizadoEm()                  { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime a)            { this.atualizadoEm = a; }

    public String getNomeUsuario()                  { return nomeUsuario; }
    public void setNomeUsuario(String nomeUsuario)  { this.nomeUsuario = nomeUsuario; }

    public String getNomeLivro()                    { return nomeLivro; }
    public void setNomeLivro(String nomeLivro)      { this.nomeLivro = nomeLivro; }

    @Override
    public String toString() {
        return String.format("Avaliacao{id=%d, usuarioId=%d, livroId=%d, nota=%d}",
                id, usuarioId, livroId, nota);
    }
}
