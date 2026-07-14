package dao;

import model.Avaliacao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsável pelo CRUD da tabela AVALIACAO.
 * Regra de negócio: um usuário só pode ter 1 avaliação por livro.
 * Exclusão é um DELETE definitivo (sem soft delete).
 */
public class AvaliacaoDAO {

    private static final String SELECT_BASE = """
        SELECT a.*, u.nome AS nome_usuario, l.nome AS nome_livro
          FROM avaliacao a
          JOIN usuario u ON u.id = a.usuario_id
          JOIN livro   l ON l.id = a.livro_id
        """;

    // ── CREATE ─────────────────────────────────────────────────
    public Avaliacao inserir(Avaliacao a) throws SQLException {
        String sql = "INSERT INTO avaliacao (usuario_id, livro_id, nota, comentario) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt   (1, a.getUsuarioId());
            ps.setInt   (2, a.getLivroId());
            ps.setInt   (3, a.getNota());
            ps.setString(4, a.getComentario());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) a.setId(rs.getInt(1));
            }
            System.out.println("[AvaliacaoDAO] Inserida: " + a);
            return a;
        }
    }

    // ── READ (por id) ───────────────────────────────────────────
    public Avaliacao buscarPorId(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE a.id = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public Avaliacao buscarPorUsuarioELivro(int usuarioId, int livroId) throws SQLException {
        String sql = SELECT_BASE + " WHERE a.usuario_id = ? AND a.livro_id = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setInt(2, livroId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    // ── READ (todas) ──────────────────────────────────────────────
    public List<Avaliacao> listarTodas() throws SQLException {
        String sql = SELECT_BASE + " ORDER BY a.criado_em DESC";
        List<Avaliacao> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // ── READ (busca por usuário/livro/comentário) ────────────────
    public List<Avaliacao> buscar(String termo) throws SQLException {
        String sql = SELECT_BASE + """
             WHERE u.nome LIKE ? OR l.nome LIKE ? OR a.comentario LIKE ?
             ORDER BY a.criado_em DESC
            """;
        List<Avaliacao> lista = new ArrayList<>();
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

    // ── READ (avaliações de um usuário) ──────────────────────────
    public List<Avaliacao> listarPorUsuario(int usuarioId) throws SQLException {
        String sql = SELECT_BASE + " WHERE a.usuario_id = ? ORDER BY a.criado_em DESC";
        List<Avaliacao> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    // ── UPDATE ─────────────────────────────────────────────────
    public boolean atualizar(Avaliacao a) throws SQLException {
        String sql = "UPDATE avaliacao SET nota=?, comentario=? WHERE id=?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt   (1, a.getNota());
            ps.setString(2, a.getComentario());
            ps.setInt   (3, a.getId());

            boolean ok = ps.executeUpdate() > 0;
            System.out.println("[AvaliacaoDAO] Atualizada (id=" + a.getId() + "): " + ok);
            return ok;
        }
    }

    // ── DELETE (definitivo) ───────────────────────────────────────
    public boolean remover(int id) throws SQLException {
        String sql = "DELETE FROM avaliacao WHERE id = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            boolean ok = ps.executeUpdate() > 0;
            System.out.println("[AvaliacaoDAO] Removida (delete definitivo) id=" + id + ": " + ok);
            return ok;
        }
    }

    // ── MAPEAMENTO ──────────────────────────────────────────────
    private Avaliacao mapear(ResultSet rs) throws SQLException {
        Avaliacao a = new Avaliacao();
        a.setId         (rs.getInt   ("id"));
        a.setUsuarioId  (rs.getInt   ("usuario_id"));
        a.setLivroId    (rs.getInt   ("livro_id"));
        a.setNota       (rs.getInt   ("nota"));
        a.setComentario (rs.getString("comentario"));
        a.setNomeUsuario(rs.getString("nome_usuario"));
        a.setNomeLivro  (rs.getString("nome_livro"));
        Timestamp criadoEm = rs.getTimestamp("criado_em");
        if (criadoEm != null) a.setCriadoEm(criadoEm.toLocalDateTime());
        Timestamp atualizadoEm = rs.getTimestamp("atualizado_em");
        if (atualizadoEm != null) a.setAtualizadoEm(atualizadoEm.toLocalDateTime());
        return a;
    }
}
