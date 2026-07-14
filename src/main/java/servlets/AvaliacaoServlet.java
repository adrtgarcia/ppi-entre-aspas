package servlets;

import dao.AvaliacaoDAO;
import dao.LivroDAO;
import model.Avaliacao;
import model.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Servlet responsável pela tela "Avaliações" (CRUD, busca e exclusão definitiva).
 * Um usuário comum só edita/remove suas próprias avaliações; administrador
 * gerencia todas. Toda operação termina com jsp:forward para avaliacoes.jsp.
 */
public class AvaliacaoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final AvaliacaoDAO avaliacaoDAO = new AvaliacaoDAO();
    private final LivroDAO livroDAO = new LivroDAO();

    // ── GET: lista ou busca ──────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String termo = request.getParameter("q");
        try {
            List<Avaliacao> avaliacoes = (termo != null && !termo.isBlank())
                    ? avaliacaoDAO.buscar(termo.trim())
                    : avaliacaoDAO.listarTodas();
            request.setAttribute("avaliacoes", avaliacoes);
            request.setAttribute("termoBusca", termo);
            request.setAttribute("todosLivros", livroDAO.listarTodos());
        } catch (SQLException e) {
            request.setAttribute("erro", "Erro ao consultar avaliações: " + e.getMessage());
        }

        request.getRequestDispatcher("/avaliacoes.jsp").forward(request, response);
    }

    // ── POST: cadastrar / alterar / remover ──────────────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Usuario logado = session != null ? (Usuario) session.getAttribute("usuarioLogado") : null;

        if (logado == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String acao = request.getParameter("acao");
        try {
            switch (acao != null ? acao : "") {
                case "cadastrar" -> cadastrar(request, logado);
                case "alterar"   -> alterar(request, logado);
                case "remover"   -> remover(request, logado);
                default -> request.setAttribute("erro", "Ação desconhecida: " + acao);
            }
        } catch (NumberFormatException e) {
            request.setAttribute("erro", "Dados inválidos: " + e.getMessage());
        } catch (SQLException e) {
            request.setAttribute("erro", "Erro ao processar solicitação: " + e.getMessage());
        }

        doGet(request, response);
    }

    private void cadastrar(HttpServletRequest request, Usuario logado) throws SQLException {
        int livroId = Integer.parseInt(request.getParameter("livroId"));
        int nota = Integer.parseInt(request.getParameter("nota"));
        String comentario = request.getParameter("comentario");

        if (avaliacaoDAO.buscarPorUsuarioELivro(logado.getId(), livroId) != null) {
            request.setAttribute("erro", "Você já avaliou este livro.");
            return;
        }

        Avaliacao avaliacao = new Avaliacao(logado.getId(), livroId, nota, comentario);
        avaliacaoDAO.inserir(avaliacao);
        request.setAttribute("sucesso", "Avaliação cadastrada com sucesso!");
    }

    private void alterar(HttpServletRequest request, Usuario logado) throws SQLException {
        int id = Integer.parseInt(request.getParameter("id"));
        Avaliacao avaliacao = avaliacaoDAO.buscarPorId(id);
        if (avaliacao == null) {
            request.setAttribute("erro", "Avaliação não encontrada (id=" + id + ")");
            return;
        }
        if (avaliacao.getUsuarioId() != logado.getId() && !logado.isAdministrador()) {
            request.setAttribute("erro", "Você só pode editar suas próprias avaliações.");
            return;
        }

        avaliacao.setNota(Integer.parseInt(request.getParameter("nota")));
        avaliacao.setComentario(request.getParameter("comentario"));
        avaliacaoDAO.atualizar(avaliacao);
        request.setAttribute("sucesso", "Avaliação atualizada com sucesso!");
    }

    private void remover(HttpServletRequest request, Usuario logado) throws SQLException {
        int id = Integer.parseInt(request.getParameter("id"));
        Avaliacao avaliacao = avaliacaoDAO.buscarPorId(id);
        if (avaliacao == null) {
            request.setAttribute("erro", "Avaliação não encontrada (id=" + id + ")");
            return;
        }
        if (avaliacao.getUsuarioId() != logado.getId() && !logado.isAdministrador()) {
            request.setAttribute("erro", "Você só pode remover suas próprias avaliações.");
            return;
        }

        boolean ok = avaliacaoDAO.remover(id);
        request.setAttribute(ok ? "sucesso" : "erro",
                ok ? "Avaliação removida com sucesso!" : "Erro ao remover avaliação.");
    }
}
