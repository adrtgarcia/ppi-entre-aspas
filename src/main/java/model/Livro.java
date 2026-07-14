package model;

import java.time.LocalDateTime;

public class Livro {
    private int id;
    private String nome;
    private String autor;
    private String genero;
    private int anoPublicacao;
    private String sinopse;
    private String capaUrl;
    private Integer criadoPorId;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public Livro() {}

    public Livro(String nome, String autor, String genero, int anoPublicacao, String sinopse, String capaUrl) {
        this.nome          = nome;
        this.autor         = autor;
        this.genero        = genero;
        this.anoPublicacao = anoPublicacao;
        this.sinopse       = sinopse;
        this.capaUrl       = capaUrl;
    }

    // ── Getters & Setters ───────────────────────────────────────
    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }

    public String getNome()                 { return nome; }
    public void setNome(String nome)        { this.nome = nome; }

    public String getAutor()                { return autor; }
    public void setAutor(String autor)      { this.autor = autor; }

    public String getGenero()               { return genero; }
    public void setGenero(String genero)    { this.genero = genero; }

    public int getAnoPublicacao()                       { return anoPublicacao; }
    public void setAnoPublicacao(int anoPublicacao)     { this.anoPublicacao = anoPublicacao; }

    public String getSinopse()              { return sinopse; }
    public void setSinopse(String sinopse)  { this.sinopse = sinopse; }

    public String getCapaUrl()              { return capaUrl; }
    public void setCapaUrl(String capaUrl)  { this.capaUrl = capaUrl; }

    public Integer getCriadoPorId()                 { return criadoPorId; }
    public void setCriadoPorId(Integer criadoPorId) { this.criadoPorId = criadoPorId; }


    public LocalDateTime getCriadoEm()                      { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm)         { this.criadoEm = criadoEm; }

    public LocalDateTime getAtualizadoEm()                  { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }

    @Override
    public String toString() {
        return String.format("Livro{id=%d, nome='%s', autor='%s', genero='%s', ano=%d}",
                id, nome, autor, genero, anoPublicacao);
    }
}
