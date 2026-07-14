package dao;

import model.Desafio;
import model.UsuarioDesafio;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO da tabela associativa usuario_desafio (N:N).
 * Um usuário pode participar de vários desafios ao mesmo tempo.
 */
public class UsuarioDesafioDAO {

    // ── PARTICIPAR ───────────────────────────────────────────────
    public boolean participar(int usuarioId, int desafioId) throws SQLException {
        String sql = """
            INSERT INTO usuario_desafio (usuario_id, desafio_id)
            VALUES (?, ?)
            ON DUPLICATE KEY UPDATE desistiu = 0
            """;
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setInt(2, desafioId);
            boolean ok = ps.executeUpdate() > 0;
            System.out.println("[UsuarioDesafioDAO] usuario=" + usuarioId + " entrou no desafio=" + desafioId + ": " + ok);
            return ok;
        }
    }

    // ── CONCLUIR ─────────────────────────────────────────────────
    public boolean concluir(int usuarioId, int desafioId) throws SQLException {
        String sql = "UPDATE usuario_desafio SET concluido = 1 WHERE usuario_id = ? AND desafio_id = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setInt(2, desafioId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── DESISTIR ─────────────────────────────────────────────────
    public boolean desistir(int usuarioId, int desafioId) throws SQLException {
        String sql = "UPDATE usuario_desafio SET desistiu = 1 WHERE usuario_id = ? AND desafio_id = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setInt(2, desafioId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── LISTAR desafios EM ANDAMENTO de um usuário ───────────────
    public List<UsuarioDesafio> listarEmAndamentoPorUsuario(int usuarioId) throws SQLException {
        return listarPorUsuario(usuarioId, "AND ud.concluido = 0 AND ud.desistiu = 0");
    }

    // ── LISTAR desafios CONCLUÍDOS de um usuário ─────────────────
    public List<UsuarioDesafio> listarConcluidosPorUsuario(int usuarioId) throws SQLException {
        return listarPorUsuario(usuarioId, "AND ud.concluido = 1");
    }

    private List<UsuarioDesafio> listarPorUsuario(int usuarioId, String filtro) throws SQLException {
        String sql = """
            SELECT ud.*, d.nome AS d_nome, d.descricao AS d_descricao,
                   d.data_inicio AS d_inicio, d.data_fim AS d_fim
              FROM usuario_desafio ud
              JOIN desafio d ON d.id = ud.desafio_id
             WHERE ud.usuario_id = ? %s
             ORDER BY d.data_inicio DESC
            """.formatted(filtro);

        List<UsuarioDesafio> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    // ── LISTAR participantes de um desafio ───────────────────────
    public List<UsuarioDesafio> listarPorDesafio(int desafioId) throws SQLException {
        String sql = """
            SELECT ud.*, u.nome AS u_nome
              FROM usuario_desafio ud
              JOIN usuario u ON u.id = ud.usuario_id
             WHERE ud.desafio_id = ? AND ud.desistiu = 0
             ORDER BY u.nome
            """;
        List<UsuarioDesafio> lista = new ArrayList<>();
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, desafioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UsuarioDesafio ud = new UsuarioDesafio();
                    ud.setUsuarioId(rs.getInt("usuario_id"));
                    ud.setDesafioId(rs.getInt("desafio_id"));
                    ud.setConcluido(rs.getBoolean("concluido"));
                    ud.setNomeUsuario(rs.getString("u_nome"));
                    lista.add(ud);
                }
            }
        }
        return lista;
    }

    public boolean estaParticipando(int usuarioId, int desafioId) throws SQLException {
        String sql = "SELECT 1 FROM usuario_desafio WHERE usuario_id=? AND desafio_id=? AND desistiu=0";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setInt(2, desafioId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private UsuarioDesafio mapear(ResultSet rs) throws SQLException {
        UsuarioDesafio ud = new UsuarioDesafio();
        ud.setUsuarioId(rs.getInt("usuario_id"));
        ud.setDesafioId(rs.getInt("desafio_id"));
        ud.setConcluido(rs.getBoolean("concluido"));
        ud.setDesistiu(rs.getBoolean("desistiu"));
        Timestamp dp = rs.getTimestamp("data_participacao");
        if (dp != null) ud.setDataParticipacao(dp.toLocalDateTime());

        Desafio d = new Desafio();
        d.setId(rs.getInt("desafio_id"));
        d.setNome(rs.getString("d_nome"));
        d.setDescricao(rs.getString("d_descricao"));
        d.setDataInicio(rs.getDate("d_inicio").toLocalDate());
        d.setDataFim(rs.getDate("d_fim").toLocalDate());
        ud.setDesafio(d);

        return ud;
    }
}
