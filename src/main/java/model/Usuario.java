package model;

import java.time.LocalDateTime;

public class Usuario {
    private int id;
    private String nome;
    private String matricula;
    private String email;
    private String senha;
    private String generoFav;
    private String fotoUrl;
    private String cargo;            // usuario | administrador
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public Usuario() {}

    public Usuario(String nome, String matricula, String email, String senha,
                   String generoFav, String fotoUrl, String cargo) {
        this.nome      = nome;
        this.matricula = matricula;
        this.email     = email;
        this.senha     = senha;
        this.generoFav = generoFav;
        this.fotoUrl   = fotoUrl;
        this.cargo     = cargo;
    }

    // ── Getters & Setters ───────────────────────────────────────
    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }

    public String getNome()                 { return nome; }
    public void setNome(String nome)        { this.nome = nome; }

    public String getMatricula()            { return matricula; }
    public void setMatricula(String m)      { this.matricula = m; }

    public String getEmail()                { return email; }
    public void setEmail(String email)      { this.email = email; }

    public String getSenha()                { return senha; }
    public void setSenha(String senha)      { this.senha = senha; }

    public String getGeneroFav()            { return generoFav; }
    public void setGeneroFav(String g)      { this.generoFav = g; }

    public String getFotoUrl()              { return fotoUrl; }
    public void setFotoUrl(String f)        { this.fotoUrl = f; }

    public String getCargo()                { return cargo; }
    public void setCargo(String cargo)      { this.cargo = cargo; }


    public LocalDateTime getCriadoEm()                      { return criadoEm; }
    public void setCriadoEm(LocalDateTime c)                { this.criadoEm = c; }

    public LocalDateTime getAtualizadoEm()                  { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime a)            { this.atualizadoEm = a; }

    // ── Regras de permissão ──────────────────────────────────────
    public boolean isAdministrador()    { return "administrador".equals(cargo); }
    public boolean isUsuarioComum()     { return "usuario".equals(cargo); }

    /**
     * Regra geral do sistema: administrador tem CRUD completo sobre tudo;
     * um usuário comum só pode editar/apagar o que ele mesmo criou
     * (comparando o id do usuário logado com o criado_por_id do registro).
     */
    public boolean podeGerenciar(Integer criadoPorId) {
        if (isAdministrador()) return true;
        return criadoPorId != null && criadoPorId == this.id;
    }

    @Override
    public String toString() {
        return String.format("Usuario{id=%d, nome='%s', matricula='%s', email='%s', cargo='%s'}",
                id, nome, matricula, email, cargo);
    }
}
