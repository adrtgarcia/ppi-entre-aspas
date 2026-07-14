package dao;

import model.Livro;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsável pelo CRUD da tabela LIVRO.
 * Exclusão é um DELETE definitivo (sem soft delete).
 */
public class LivroDAO {

    // ── CREATE ─────────────────────────────────────────────────
    public Livro inserir(Livro livro) throws SQLException {
        String sql = "INSERT INTO livro (nome, autor, genero, ano_publicacao, sinopse, capa_url, criado_por_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, livro.getNome());
            ps.setString(2, livro.getAutor());
            ps.setString(3, livro.getGenero());
            ps.setInt   (4, livro.getAnoPublicacao());
            ps.setString(5, livro.getSinopse());
            ps.setString(6, livro.getCapaUrl());
            if (livro.getCriadoPorId() != null) ps.setInt(7, livro.getCriadoPorId());
            else ps.setNull(7, Types.INTEGER);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) livro.setId(rs.getInt(1));
            }
            System.out.println("[LivroDAO] Inserido: " + livro);
            return livro;
        }
    }

    // ── READ (por id) ───────────────────────────────────────────
    public Livro buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM livro WHERE id = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    // ── READ (todos) ─────────────────────────────────────────────
    public List<Livro> listarTodos() throws SQLException {
        String sql = "SELECT * FROM livro ORDER BY nome";
        List<Livro> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // ── READ (busca por nome/autor/gênero) ──────────────────────
    public List<Livro> buscar(String termo) throws SQLException {
        String sql = """
            SELECT * FROM livro
             WHERE nome LIKE ? OR autor LIKE ? OR genero LIKE ?
             ORDER BY nome
            """;
        List<Livro> lista = new ArrayList<>();
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
    public boolean atualizar(Livro livro) throws SQLException {
        String sql = "UPDATE livro SET nome=?, autor=?, genero=?, ano_publicacao=?, sinopse=?, capa_url=? WHERE id=?";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, livro.getNome());
            ps.setString(2, livro.getAutor());
            ps.setString(3, livro.getGenero());
            ps.setInt   (4, livro.getAnoPublicacao());
            ps.setString(5, livro.getSinopse());
            ps.setString(6, livro.getCapaUrl());
            ps.setInt   (7, livro.getId());

            boolean ok = ps.executeUpdate() > 0;
            System.out.println("[LivroDAO] Atualizado (id=" + livro.getId() + "): " + ok);
            return ok;
        }
    }

    // ── DELETE (definitivo) ───────────────────────────────────────
    // Regra de negócio: ao apagar um livro, ele é removido de todos os desafios
    // em que aparece. Se, após a remoção, algum desafio ficar sem nenhum livro
    // vinculado, esse desafio é removido junto (não faz sentido um desafio "vazio").
    // Se o desafio ainda tiver outros livros, ele continua existindo normalmente.
    public boolean remover(int id) throws SQLException {
        try (Connection conn = ConexaoDB.getConexao()) {
            conn.setAutoCommit(false);
            try {
                DesafioDAO desafioDAO = new DesafioDAO();

                List<Integer> desafiosDoLivro = desafioDAO.listarIdsDesafiosDoLivro(conn, id);
                desafioDAO.desvincularLivroDeTodosDesafios(conn, id);

                for (int desafioId : desafiosDoLivro) {
                    if (desafioDAO.contarLivrosDoDesafio(conn, desafioId) == 0) {
                        desafioDAO.removerComDependencias(conn, desafioId);
                    }
                }

                boolean ok;
                String sql = "DELETE FROM livro WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    ok = ps.executeUpdate() > 0;
                }

                conn.commit();
                System.out.println("[LivroDAO] Removido (delete definitivo) id=" + id + ": " + ok);
                return ok;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // ── MAPEAMENTO ResultSet → Livro ────────────────────────────
    private Livro mapear(ResultSet rs) throws SQLException {
        Livro l = new Livro();
        l.setId            (rs.getInt      ("id"));
        l.setNome          (rs.getString   ("nome"));
        l.setAutor         (rs.getString   ("autor"));
        l.setGenero        (rs.getString   ("genero"));
        l.setAnoPublicacao (rs.getInt      ("ano_publicacao"));
        l.setSinopse       (rs.getString   ("sinopse"));
        l.setCapaUrl       (rs.getString   ("capa_url"));
        int criadoPor = rs.getInt("criado_por_id");
        l.setCriadoPorId(rs.wasNull() ? null : criadoPor);
        Timestamp criadoEm = rs.getTimestamp("criado_em");
        if (criadoEm != null) l.setCriadoEm(criadoEm.toLocalDateTime());
        Timestamp atualizadoEm = rs.getTimestamp("atualizado_em");
        if (atualizadoEm != null) l.setAtualizadoEm(atualizadoEm.toLocalDateTime());
        return l;
    }
}
