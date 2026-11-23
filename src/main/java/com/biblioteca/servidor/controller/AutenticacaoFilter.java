package com.biblioteca.servidor.controller;

import com.biblioteca.servidor.model.Usuario;
import com.biblioteca.servidor.service.TokenService;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AutenticacaoFilter extends OncePerRequestFilter {
    private final TokenService tokenService;

    public AutenticacaoFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (request.getDispatcherType() != DispatcherType.REQUEST) {
            return true;
        }
        String caminho = request.getRequestURI();
        return caminho.equals("/")
                || caminho.equals("/index.html")
                || caminho.equals("/biblioteca")
                || caminho.startsWith("/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String autorizacao = request.getHeader("Authorization");
        if (autorizacao == null || !autorizacao.startsWith("Bearer ")) {
            recusar(response);
            return;
        }
        Optional<Usuario> usuario = tokenService.usuarioDoToken(autorizacao.substring("Bearer ".length()).trim());
        if (usuario.isEmpty()) {
            recusar(response);
            return;
        }
        request.setAttribute(EmprestimoController.USUARIO_LOGADO, usuario.get());
        filterChain.doFilter(request, response);
    }

    private void recusar(HttpServletResponse response) throws IOException {
        response.setStatus(401);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"mensagem\":\"não autenticado\"}");
    }
}
