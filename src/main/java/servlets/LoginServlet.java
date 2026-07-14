package servlets;

import dao.UsuarioDAO;
import model.Usuario;
import util.SenhaUtil;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Servlet responsável pelo login do sistema.
 *
 * POST /login → autentica usando UsuarioDAO e ENCAMINHA (RequestDispatcher.forward,
 * equivalente Java do <jsp:forward>) para uma página JSP distinta de acordo
 * com o resultado:
 *   - sucesso → /home  (que monta os dados e encaminha para home.jsp)
 *   - erro    → /login.jsp (com mensagem de erro)
 */
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String senha = request.getParameter("senha");

        try {
            Usuario usuario = usuarioDAO.autenticar(email, SenhaUtil.codificar(senha));

            if (usuario != null) {
                HttpSession session = request.getSession();
                session.setAttribute("usuarioLogado", usuario);

                // sucesso → encaminha para o fluxo autenticado (HomeServlet monta os dados e forward para home.jsp)
                RequestDispatcher rd = request.getRequestDispatcher("/home");
                rd.forward(request, response);
            } else {
                request.setAttribute("erro", "E-mail ou senha inválidos.");
                request.setAttribute("emailDigitado", email);

                // erro → encaminha de volta para a tela de login com a mensagem
                RequestDispatcher rd = request.getRequestDispatcher("/login.jsp");
                rd.forward(request, response);
            }
        } catch (SQLException e) {
            request.setAttribute("erro", "Erro ao acessar o banco de dados: " + e.getMessage());
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }
}
