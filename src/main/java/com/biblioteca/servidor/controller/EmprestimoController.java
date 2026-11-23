package com.biblioteca.servidor.controller;

import com.biblioteca.servidor.model.Emprestimo;
import com.biblioteca.servidor.model.Usuario;
import com.biblioteca.servidor.service.EmprestimoService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmprestimoController {
    public static final String USUARIO_LOGADO = "usuarioLogado";

    private final EmprestimoService emprestimoService;

    public EmprestimoController(EmprestimoService emprestimoService) {
        this.emprestimoService = emprestimoService;
    }

    @PostMapping("/emprestimos")
    public EmprestimoResponse realizar(@RequestBody Map<String, Object> corpo, HttpServletRequest request) {
        Usuario logado = (Usuario) request.getAttribute(USUARIO_LOGADO);
        Integer livroId = numero(corpo, "livroId");
        String matriculaAluno = corpo.get("matriculaAluno") == null ? null : corpo.get("matriculaAluno").toString();

        Emprestimo emprestimo = emprestimoService.realizar(logado, livroId, matriculaAluno);
        return EmprestimoResponse.deEmprestimo(
                emprestimo,
                "Empréstimo realizado. Devolver até " + emprestimo.getDataPrevista()
        );
    }

    @PostMapping("/emprestimos/{id}/devolucao")
    public EmprestimoResponse devolver(@PathVariable int id, HttpServletRequest request) {
        Usuario logado = (Usuario) request.getAttribute(USUARIO_LOGADO);
        Emprestimo emprestimo = emprestimoService.devolver(logado, id);
        String mensagem = emprestimo.isEmAtraso()
                ? "Devolução realizada com atraso"
                : "Devolução realizada";
        return EmprestimoResponse.deEmprestimo(emprestimo, mensagem);
    }

    private Integer numero(Map<String, Object> corpo, String campo) {
        Object valor = corpo == null ? null : corpo.get(campo);
        if (valor == null) {
            return null;
        }
        if (valor instanceof Number numero) {
            return numero.intValue();
        }
        return Integer.valueOf(valor.toString());
    }
}
