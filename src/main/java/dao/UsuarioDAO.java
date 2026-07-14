package dao;

import model.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsável pelo CRUD da tabela USUARIO.
 * Exclusão é um DELETE definitivo (sem soft delete).
 */
public class UsuarioDAO {

    // ── CREATE ─────────────────────────────────────────────────
    public Usuario inserir(Usuario u) throws SQLException {
        String sql = """
            INSERT INTO usuario (nome, matricula, email, senha, genero_fav, foto_url, cargo)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, u.getNome());
            ps.setString(2, u.getMatricula());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getSenha());
            ps.setString(5, u.getGeneroFav());
            ps.setString(6, u.getFotoUrl());
            ps.setString(7, u.getCargo() != null ? u.getCargo() : "usuario");
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) u.setId(rs.getInt(1));
            }
            System.out.println("[UsuarioDAO] Inserido: " + u);
            return u;
        }
    }

    // ── READ (por id) ───────────────────────────────────────────
    public Usuario buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE id = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    // ── READ (login) ────────────────────────────────────────────
    public Usuario autenticar(String email, String senha) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE email = ? AND senha = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, senha);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public boolean emailJaCadastrado(String email) throws SQLException {
        String sql = "SELECT id FROM usuario WHERE email = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean matriculaJaCadastrada(String matricula) throws SQLException {
        String sql = "SELECT id FROM usuario WHERE matricula = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ── READ (todos) ─────────────────────────────────────────────
    public List<Usuario> listarTodos() throws SQLException {
        String sql = "SELECT * FROM usuario ORDER BY nome";
        List<Usuario> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // ── READ (apenas administradores) ────────────────────────────
    public List<Usuario> listarAdministradores() throws SQLException {
        String sql = "SELECT * FROM usuario WHERE cargo = 'administrador' ORDER BY nome";
        List<Usuario> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // ── READ (busca por nome/email/matrícula) ───────────────────
    public List<Usuario> buscar(String termo) throws SQLException {
        String sql = """
            SELECT * FROM usuario
             WHERE nome LIKE ? OR email LIKE ? OR matricula LIKE ?
             ORDER BY nome
            """;
        List<Usuario> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + termo + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    // ── UPDATE ─────────────────────────────────────────────────
    public boolean atualizar(Usuario u) throws SQLException {
        String sql = """
            UPDATE usuario
               SET nome=?, matricula=?, email=?, senha=?, genero_fav=?, foto_url=?, cargo=?
             WHERE id=?
            """;
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getNome());
            ps.setString(2, u.getMatricula());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getSenha());
            ps.setString(5, u.getGeneroFav());
            ps.setString(6, u.getFotoUrl());
            ps.setString(7, u.getCargo());
            ps.setInt(8, u.getId());

            boolean ok = ps.executeUpdate() > 0;
            System.out.println("[UsuarioDAO] Atualizado (id=" + u.getId() + "): " + ok);
            return ok;
        }
    }

    // ── DELETE (definitivo) ───────────────────────────────────────
    public boolean remover(int id) throws SQLException {
        String sql = "DELETE FROM usuario WHERE id = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            boolean ok = ps.executeUpdate() > 0;
            System.out.println("[UsuarioDAO] Removido (delete definitivo) id=" + id + ": " + ok);
            return ok;
        }
    }

    // ── MAPEAMENTO ResultSet → Usuario ──────────────────────────
    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId       (rs.getInt     ("id"));
        u.setNome     (rs.getString  ("nome"));
        u.setMatricula(rs.getString  ("matricula"));
        u.setEmail    (rs.getString  ("email"));
        u.setSenha    (rs.getString  ("senha"));
        u.setGeneroFav(rs.getString  ("genero_fav"));
        u.setFotoUrl  (rs.getString  ("foto_url"));
        u.setCargo    (rs.getString  ("cargo"));

        Timestamp criadoEm = rs.getTimestamp("criado_em");
        if (criadoEm != null) u.setCriadoEm(criadoEm.toLocalDateTime());
        Timestamp atualizadoEm = rs.getTimestamp("atualizado_em");
        if (atualizadoEm != null) u.setAtualizadoEm(atualizadoEm.toLocalDateTime());

        return u;
    }
}
