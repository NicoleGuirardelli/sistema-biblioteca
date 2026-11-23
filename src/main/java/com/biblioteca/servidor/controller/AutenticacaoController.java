package com.biblioteca.servidor.controller;

import com.biblioteca.servidor.model.Usuario;
import com.biblioteca.servidor.repository.UsuarioRepository;
import com.biblioteca.servidor.service.ApiException;
import com.biblioteca.servidor.service.TokenService;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AutenticacaoController {
    private final UsuarioRepository usuarioRepository;
    private final TokenService tokenService;

    public AutenticacaoController(UsuarioRepository usuarioRepository, TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.tokenService = tokenService;
    }

    @PostMapping("/auth/login")
    public Map<String, Object> login(@RequestBody Map<String, String> corpo) {
        if (corpo == null || corpo.get("matricula") == null || corpo.get("matricula").isBlank()) {
            throw new ApiException(400, "Informe a matrícula");
        }
        Usuario usuario = usuarioRepository.findByMatricula(corpo.get("matricula").trim())
                .orElseThrow(() -> new ApiException(401, "não autenticado"));
        return Map.of(
                "token", tokenService.gerar(usuario),
                "usuarioId", usuario.getId(),
                "nome", usuario.getNome(),
                "perfil", usuario.getPerfil().name()
        );
    }
}
