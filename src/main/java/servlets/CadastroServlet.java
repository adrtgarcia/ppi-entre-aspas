package servlets;

import dao.UsuarioDAO;
import model.Usuario;
import util.SenhaUtil;
import util.UploadUtil;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Servlet responsável pelo autocadastro de novos usuários (sempre com cargo "usuario").
 * Recebe o upload da FOTO DE PERFIL (arquivo 1 de 2 exigidos pelo laboratório).
 *
 * POST /cadastro → processa o cadastro e ENCAMINHA (jsp:forward) para:
 *   - sucesso → /login.jsp  (com mensagem de sucesso, pronto para logar)
 *   - erro    → /cadastro.jsp (com mensagem de erro e dados já digitados)
 */
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 10 * 1024 * 1024)
public class CadastroServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String nome      = request.getParameter("nome");
        String matricula = request.getParameter("matricula");
        String email     = request.getParameter("email");
        String senha     = request.getParameter("senha");
        String generoFav = request.getParameter("generoFav");

        try {
            if (usuarioDAO.emailJaCadastrado(email)) {
                erroCadastro(request, response, "Este e-mail já está cadastrado.");
                return;
            }
            if (usuarioDAO.matriculaJaCadastrada(matricula)) {
                erroCadastro(request, response, "Esta matrícula já está cadastrada.");
                return;
            }

            Part fotoPart = request.getPart("foto");
            String fotoUrl = UploadUtil.salvar(fotoPart, "fotos", getServletContext());

            Usuario usuario = new Usuario(nome, matricula, email, SenhaUtil.codificar(senha), generoFav, fotoUrl, "usuario");
            usuarioDAO.inserir(usuario);

            request.setAttribute("sucesso", "Cadastro realizado com sucesso! Faça login para continuar.");
            RequestDispatcher rd = request.getRequestDispatcher("/login.jsp");
            rd.forward(request, response);

        } catch (SQLException e) {
            erroCadastro(request, response, "Erro ao cadastrar: " + e.getMessage());
        } catch (IOException e) {
            erroCadastro(request, response, "Erro no upload da foto: " + e.getMessage());
        }
    }

    private void erroCadastro(HttpServletRequest request, HttpServletResponse response, String mensagem)
            throws ServletException, IOException {
        request.setAttribute("erro", mensagem);
        request.setAttribute("nomeDigitado", request.getParameter("nome"));
        request.setAttribute("matriculaDigitada", request.getParameter("matricula"));
        request.setAttribute("emailDigitado", request.getParameter("email"));
        request.setAttribute("generoDigitado", request.getParameter("generoFav"));
        RequestDispatcher rd = request.getRequestDispatcher("/cadastro.jsp");
        rd.forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/cadastro.jsp");
    }
}
