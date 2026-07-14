package servlets;

import dao.LivroDAO;
import model.Livro;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Gera a ficha de leitura (impressão) de um livro, com espaço para anotações
 * e datas de início/fim de leitura.
 * GET /ficha-leitura?id={livroId} → encaminha para ficha-leitura.jsp
 */
public class FichaLeituraServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final LivroDAO livroDAO = new LivroDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Livro livro = livroDAO.buscarPorId(id);

            if (livro == null) {
                request.setAttribute("erro", "Livro não encontrado.");
            } else {
                request.setAttribute("livro", livro);
            }
        } catch (NumberFormatException | SQLException e) {
            request.setAttribute("erro", "Não foi possível carregar a ficha de leitura: " + e.getMessage());
        }

        request.getRequestDispatcher("/ficha-leitura.jsp").forward(request, response);
    }
}
