package servlets;

import dao.AvaliacaoDAO;
import dao.UsuarioDesafioDAO;
import model.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Servlet controlador da tela inicial (home).
 * Monta: desafios em andamento do usuário logado, desafios concluídos e
 * suas avaliações, e ENCAMINHA (jsp:forward) para home.jsp.
 *
 * Também processa as ações "concluir" e "desistir" de um desafio, disparadas
 * pela modal da lista de desafios em andamento.
 */
public class HomeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final UsuarioDesafioDAO usuarioDesafioDAO = new UsuarioDesafioDAO();
    private final AvaliacaoDAO avaliacaoDAO = new AvaliacaoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        montarEEncaminhar(request, response);
    }

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
        String desafioIdStr = request.getParameter("desafioId");

        // Só tenta processar quando ação e id do desafio realmente vieram na requisição.
        // Isso evita o NumberFormatException ("Cannot parse null string") quando o
        // formulário é reenviado sem esses parâmetros (ex.: resubmissão de POST pelo navegador).
        if (acao == null || acao.isBlank() || desafioIdStr == null || desafioIdStr.isBlank()) {
            montarEEncaminhar(request, response);
            return;
        }

        try {
            int desafioId = Integer.parseInt(desafioIdStr);
            if ("concluir".equals(acao)) {
                usuarioDesafioDAO.concluir(logado.getId(), desafioId);
                request.setAttribute("sucesso", "Desafio concluído! Parabéns.");
            } else if ("desistir".equals(acao)) {
                usuarioDesafioDAO.desistir(logado.getId(), desafioId);
                request.setAttribute("sucesso", "Você desistiu do desafio.");
            }
        } catch (NumberFormatException e) {
            request.setAttribute("erro", "Não foi possível processar o desafio: identificador inválido.");
        } catch (SQLException e) {
            request.setAttribute("erro", "Não foi possível processar o desafio: " + e.getMessage());
        }

        montarEEncaminhar(request, response);
    }

    private void montarEEncaminhar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Usuario logado = session != null ? (Usuario) session.getAttribute("usuarioLogado") : null;
        if (logado == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        try {
            request.setAttribute("emAndamento", usuarioDesafioDAO.listarEmAndamentoPorUsuario(logado.getId()));
            request.setAttribute("concluidos", usuarioDesafioDAO.listarConcluidosPorUsuario(logado.getId()));
            request.setAttribute("minhasAvaliacoes", avaliacaoDAO.listarPorUsuario(logado.getId()));
        } catch (SQLException e) {
            request.setAttribute("erro", "Erro ao carregar dados da home: " + e.getMessage());
        }

        request.getRequestDispatcher("/home.jsp").forward(request, response);
    }
}
