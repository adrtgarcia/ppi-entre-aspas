package filters;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Filtro que garante que apenas usuários autenticados acessem as páginas
 * internas do sistema. Páginas públicas: login, cadastro, css, js e uploads.
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    private static final String[] PUBLICO = {
        "/login.jsp", "/login", "/cadastro.jsp", "/cadastro",
        "/css/", "/js/", "/img/", "/uploads/"
    };

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String caminho = request.getRequestURI().substring(request.getContextPath().length());

        boolean ehPublico = caminho.equals("/") || caminho.isEmpty();
        for (String rota : PUBLICO) {
            if (caminho.startsWith(rota)) { ehPublico = true; break; }
        }

        HttpSession session = request.getSession(false);
        boolean logado = session != null && session.getAttribute("usuarioLogado") != null;

        if (ehPublico || logado) {
            chain.doFilter(req, res);
        } else {
            request.setAttribute("erro", "Faça login para continuar.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}
