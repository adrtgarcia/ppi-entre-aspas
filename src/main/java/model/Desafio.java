package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Desafio {
    private int id;
    private String nome;
    private String descricao;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private Integer criadoPorId;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    // preenchidos pelo DAO via join (não são colunas da tabela desafio)
    private List<Livro> livros = new ArrayList<>();
    private int totalParticipantes;

    public Desafio() {}

    public Desafio(String nome, String descricao, LocalDate dataInicio, LocalDate dataFim) {
        this.nome       = nome;
        this.descricao  = descricao;
        this.dataInicio = dataInicio;
        this.dataFim    = dataFim;
    }

    // ── Getters & Setters ───────────────────────────────────────
    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public String getNome()                     { return nome; }
    public void setNome(String nome)            { this.nome = nome; }

    public String getDescricao()                { return descricao; }
    public void setDescricao(String d)          { this.descricao = d; }

    public LocalDate getDataInicio()            { return dataInicio; }
    public void setDataInicio(LocalDate d)      { this.dataInicio = d; }

    public LocalDate getDataFim()               { return dataFim; }
    public void setDataFim(LocalDate d)         { this.dataFim = d; }

    public Integer getCriadoPorId()                 { return criadoPorId; }
    public void setCriadoPorId(Integer criadoPorId) { this.criadoPorId = criadoPorId; }

    public LocalDateTime getCriadoEm()                      { return criadoEm; }
    public void setCriadoEm(LocalDateTime c)                { this.criadoEm = c; }

    public LocalDateTime getAtualizadoEm()                  { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime a)            { this.atualizadoEm = a; }

    public List<Livro> getLivros()              { return livros; }
    public void setLivros(List<Livro> livros)   { this.livros = livros; }

    public int getTotalParticipantes()                  { return totalParticipantes; }
    public void setTotalParticipantes(int totalParticipantes) { this.totalParticipantes = totalParticipantes; }

    /** true se hoje está dentro do período do desafio */
    public boolean isEmAndamento() {
        LocalDate hoje = LocalDate.now();
        return !hoje.isBefore(dataInicio) && !hoje.isAfter(dataFim);
    }

    /** true se a data final do desafio já passou */
    public boolean isFinalizado() {
        return LocalDate.now().isAfter(dataFim);
    }

    @Override
    public String toString() {
        return String.format("Desafio{id=%d, nome='%s', inicio=%s, fim=%s}",
                id, nome, dataInicio, dataFim);
    }
}
