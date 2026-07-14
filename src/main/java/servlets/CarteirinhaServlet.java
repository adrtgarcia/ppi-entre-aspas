package servlets;

import model.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Gera a carteirinha de membro (impressão) do usuário logado.
 * GET /carteirinha → encaminha para carteirinha.jsp com os dados do usuário.
 */
public class CarteirinhaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Usuario logado = session != null ? (Usuario) session.getAttribute("usuarioLogado") : null;

        if (logado == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        request.setAttribute("usuario", logado);
        request.getRequestDispatcher("/carteirinha.jsp").forward(request, response);
    }
}
