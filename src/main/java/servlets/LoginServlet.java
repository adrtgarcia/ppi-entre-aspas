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
 * POST /login → autentica usando UsuarioDAO e encaminha o fluxo de acordo
 * com o resultado:
 *   - sucesso → RequestDispatcher.forward para /home (mesma requisição,
 *               a URL na barra do navegador continua sendo /login — é assim
 *               que forward funciona, o navegador nunca vê essa troca)
 *   - erro    → response.sendRedirect para /login/erro (URL mapeada via
 *               <jsp-file> no web.xml para login-erro.jsp). Redirect gera
 *               uma NOVA requisição no navegador, então a URL muda de
 *               verdade. Como é uma requisição nova, os atributos de erro
 *               não podem ir em request.setAttribute (eles se perderiam) —
 *               por isso usamos a sessão como um "flash message": grava o
 *               erro, a página de erro lê e imediatamente apaga da sessão.
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
                irParaPaginaDeErro(request, response, "E-mail ou senha inválidos.", email);
            }
        } catch (SQLException e) {
            irParaPaginaDeErro(request, response, "Erro ao acessar o banco de dados: " + e.getMessage(), email);
        }
    }

    /** Grava o erro na sessão (flash message) e redireciona para /login/erro. */
    private void irParaPaginaDeErro(HttpServletRequest request, HttpServletResponse response,
                                     String mensagem, String emailDigitado) throws IOException {
        HttpSession session = request.getSession();
        session.setAttribute("erroLogin", mensagem);
        session.setAttribute("emailDigitadoLogin", emailDigitado);
        response.sendRedirect(request.getContextPath() + "/login/erro");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }
}
