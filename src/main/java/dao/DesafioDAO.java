package dao;

import model.Desafio;
import model.Livro;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsável pelo CRUD da tabela DESAFIO.
 * Relacionamento: um desafio possui 1 ou mais livros (tabela desafio_livro, N:N).
 */
public class DesafioDAO {

    // ── CREATE ─────────────────────────────────────────────────
    public Desafio inserir(Desafio d) throws SQLException {
        String sql = "INSERT INTO desafio (nome, descricao, data_inicio, data_fim, criado_por_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, d.getNome());
            ps.setString(2, d.getDescricao());
            ps.setDate  (3, Date.valueOf(d.getDataInicio()));
            ps.setDate  (4, Date.valueOf(d.getDataFim()));
            if (d.getCriadoPorId() != null) ps.setInt(5, d.getCriadoPorId());
            else ps.setNull(5, Types.INTEGER);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) d.setId(rs.getInt(1));
            }
            System.out.println("[DesafioDAO] Inserido: " + d);
            return d;
        }
    }

    // ── VÍNCULO COM LIVROS (N:N) ─────────────────────────────────
    public void vincularLivros(int desafioId, List<Integer> livroIds) throws SQLException {
        String delete = "DELETE FROM desafio_livro WHERE desafio_id = ?";
        String insert = "INSERT INTO desafio_livro (desafio_id, livro_id) VALUES (?, ?)";

        try (Connection conn = ConexaoDB.getConexao()) {
            try (PreparedStatement psDel = conn.prepareStatement(delete)) {
                psDel.setInt(1, desafioId);
                psDel.executeUpdate();
            }
            try (PreparedStatement psIns = conn.prepareStatement(insert)) {
                for (Integer livroId : livroIds) {
                    psIns.setInt(1, desafioId);
                    psIns.setInt(2, livroId);
                    psIns.addBatch();
                }
                if (!livroIds.isEmpty()) psIns.executeBatch();
            }
        }
    }

    private List<Livro> listarLivrosDoDesafio(Connection conn, int desafioId) throws SQLException {
        String sql = """
            SELECT l.* FROM livro l
            JOIN desafio_livro dl ON dl.livro_id = l.id
            WHERE dl.desafio_id = ?
            ORDER BY l.nome
            """;
        List<Livro> livros = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, desafioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Livro l = new Livro();
                    l.setId(rs.getInt("id"));
                    l.setNome(rs.getString("nome"));
                    l.setAutor(rs.getString("autor"));
                    l.setGenero(rs.getString("genero"));
                    l.setAnoPublicacao(rs.getInt("ano_publicacao"));
                    l.setCapaUrl(rs.getString("capa_url"));
                    livros.add(l);
                }
            }
        }
        return livros;
    }

    // ── Usadas na remoção de um livro (LivroDAO.remover) ─────────
    // Retorna os ids dos desafios que contêm o livro informado.
    public List<Integer> listarIdsDesafiosDoLivro(Connection conn, int livroId) throws SQLException {
        String sql = "SELECT desafio_id FROM desafio_livro WHERE livro_id = ?";
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, livroId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt("desafio_id"));
            }
        }
        return ids;
    }

    // Remove o vínculo do livro com todos os desafios (não apaga os desafios).
    public void desvincularLivroDeTodosDesafios(Connection conn, int livroId) throws SQLException {
        String sql = "DELETE FROM desafio_livro WHERE livro_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, livroId);
            ps.executeUpdate();
        }
    }

    // Conta quantos livros um desafio ainda possui.
    public int contarLivrosDoDesafio(Connection conn, int desafioId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM desafio_livro WHERE desafio_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, desafioId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // Remove definitivamente um desafio e suas participações (usado quando o desafio fica sem nenhum livro).
    public void removerComDependencias(Connection conn, int desafioId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM usuario_desafio WHERE desafio_id = ?")) {
            ps.setInt(1, desafioId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM desafio WHERE id = ?")) {
            ps.setInt(1, desafioId);
            ps.executeUpdate();
        }
        System.out.println("[DesafioDAO] Desafio id=" + desafioId + " removido por ter ficado sem nenhum livro.");
    }

    private int contarParticipantes(Connection conn, int desafioId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuario_desafio WHERE desafio_id = ? AND desistiu = 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, desafioId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // ── READ (por id) ───────────────────────────────────────────
    public Desafio buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM desafio WHERE id = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Desafio d = mapear(rs);
                    d.setLivros(listarLivrosDoDesafio(conn, id));
                    d.setTotalParticipantes(contarParticipantes(conn, id));
                    return d;
                }
            }
        }
        return null;
    }

    // ── READ (todos) ─────────────────────────────────────────────
    public List<Desafio> listarTodos() throws SQLException {
        String sql = "SELECT * FROM desafio ORDER BY data_inicio DESC";
        return listarComFiltro(sql, null);
    }

    // ── READ (busca por nome/descrição) ─────────────────────────
    public List<Desafio> buscar(String termo) throws SQLException {
        String sql = """
            SELECT * FROM desafio
             WHERE nome LIKE ? OR descricao LIKE ?
             ORDER BY data_inicio DESC
            """;
        return listarComFiltro(sql, termo);
    }

    private List<Desafio> listarComFiltro(String sql, String termo) throws SQLException {
        List<Desafio> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (termo != null) {
                String like = "%" + termo + "%";
                ps.setString(1, like);
                ps.setString(2, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Desafio d = mapear(rs);
                    d.setLivros(listarLivrosDoDesafio(conn, d.getId()));
                    d.setTotalParticipantes(contarParticipantes(conn, d.getId()));
                    lista.add(d);
                }
            }
        }
        return lista;
    }

    // ── UPDATE ─────────────────────────────────────────────────
    public boolean atualizar(Desafio d) throws SQLException {
        String sql = "UPDATE desafio SET nome=?, descricao=?, data_inicio=?, data_fim=? WHERE id=?";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, d.getNome());
            ps.setString(2, d.getDescricao());
            ps.setDate  (3, Date.valueOf(d.getDataInicio()));
            ps.setDate  (4, Date.valueOf(d.getDataFim()));
            ps.setInt   (5, d.getId());

            boolean ok = ps.executeUpdate() > 0;
            System.out.println("[DesafioDAO] Atualizado (id=" + d.getId() + "): " + ok);
            return ok;
        }
    }

    // ── DELETE (definitivo) ───────────────────────────────────────
    public boolean remover(int id) throws SQLException {
        String sql = "DELETE FROM desafio WHERE id = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            boolean ok = ps.executeUpdate() > 0;
            System.out.println("[DesafioDAO] Removido (delete definitivo) id=" + id + ": " + ok);
            return ok;
        }
    }

    // ── MAPEAMENTO ──────────────────────────────────────────────
    private Desafio mapear(ResultSet rs) throws SQLException {
        Desafio d = new Desafio();
        d.setId         (rs.getInt  ("id"));
        d.setNome       (rs.getString("nome"));
        d.setDescricao  (rs.getString("descricao"));
        d.setDataInicio (rs.getDate ("data_inicio").toLocalDate());
        d.setDataFim    (rs.getDate ("data_fim").toLocalDate());
        int criadoPor = rs.getInt("criado_por_id");
        d.setCriadoPorId(rs.wasNull() ? null : criadoPor);
        Timestamp criadoEm = rs.getTimestamp("criado_em");
        if (criadoEm != null) d.setCriadoEm(criadoEm.toLocalDateTime());
        Timestamp atualizadoEm = rs.getTimestamp("atualizado_em");
        if (atualizadoEm != null) d.setAtualizadoEm(atualizadoEm.toLocalDateTime());
        return d;
    }
}
