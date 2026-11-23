package com.biblioteca.servidor.service;

import com.biblioteca.servidor.model.Usuario;
import com.biblioteca.servidor.repository.UsuarioRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    private static final long DURACAO_SEGUNDOS = 12 * 60 * 60;

    private final UsuarioRepository usuarioRepository;
    private final String segredo;

    public TokenService(UsuarioRepository usuarioRepository,
                        @Value("${biblioteca.token.segredo}") String segredo) {
        this.usuarioRepository = usuarioRepository;
        this.segredo = segredo;
    }

    public String gerar(Usuario usuario) {
        String cabecalho = codificar("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        long expiraEm = Instant.now().getEpochSecond() + DURACAO_SEGUNDOS;
        String carga = codificar("{\"sub\":" + usuario.getId() + ",\"perfil\":\"" + usuario.getPerfil() + "\",\"exp\":" + expiraEm + "}");
        String conteudo = cabecalho + "." + carga;
        return conteudo + "." + assinar(conteudo);
    }

    public Optional<Usuario> usuarioDoToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        String[] partes = token.split("\\.");
        if (partes.length != 3) {
            return Optional.empty();
        }
        String conteudo = partes[0] + "." + partes[1];
        if (!assinar(conteudo).equals(partes[2])) {
            return Optional.empty();
        }
        String json = new String(Base64.getUrlDecoder().decode(partes[1]), StandardCharsets.UTF_8);
        long expiraEm = lerNumero(json, "exp");
        if (expiraEm < Instant.now().getEpochSecond()) {
            return Optional.empty();
        }
        int usuarioId = (int) lerNumero(json, "sub");
        return usuarioRepository.findById(usuarioId);
    }

    private long lerNumero(String json, String campo) {
        String marca = "\"" + campo + "\":";
        int inicio = json.indexOf(marca);
        if (inicio < 0) {
            return -1;
        }
        int posicao = inicio + marca.length();
        int fim = posicao;
        while (fim < json.length() && Character.isDigit(json.charAt(fim))) {
            fim++;
        }
        return Long.parseLong(json.substring(posicao, fim));
    }

    private String codificar(String valor) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(valor.getBytes(StandardCharsets.UTF_8));
    }

    private String assinar(String conteudo) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(conteudo.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao assinar o token", e);
        }
    }
}
