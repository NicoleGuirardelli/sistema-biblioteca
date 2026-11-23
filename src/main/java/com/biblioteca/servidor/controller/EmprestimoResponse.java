package com.biblioteca.servidor.controller;

import com.biblioteca.servidor.model.Usuario;
import java.time.LocalDate;

public record EmprestimoResponse(
        Integer id,
        Integer usuarioId,
        Integer livroId,
        String titulo,
        LocalDate dataEmprestimo,
        LocalDate dataPrevista,
        LocalDate dataDevolucao,
        String status,
        boolean emAtraso,
        String mensagem
) {
    public static EmprestimoResponse deEmprestimo(com.biblioteca.servidor.model.Emprestimo emprestimo, String mensagem) {
        return new EmprestimoResponse(
                emprestimo.getId(),
                emprestimo.getUsuario().getId(),
                emprestimo.getLivro().getId(),
                emprestimo.getLivro().getTitulo(),
                emprestimo.getDataEmprestimo(),
                emprestimo.getDataPrevista(),
                emprestimo.getDataDevolucao(),
                emprestimo.getStatus().name(),
                emprestimo.isEmAtraso(),
                mensagem
        );
    }

    public static EmprestimoResponse deReferencias(com.biblioteca.servidor.model.Emprestimo emprestimo,
                                                    Usuario usuario,
                                                    com.biblioteca.servidor.model.Livro livro,
                                                    String mensagem) {
        return new EmprestimoResponse(
                emprestimo.getId(),
                usuario.getId(),
                livro.getId(),
                livro.getTitulo(),
                emprestimo.getDataEmprestimo(),
                emprestimo.getDataPrevista(),
                emprestimo.getDataDevolucao(),
                emprestimo.getStatus().name(),
                emprestimo.isEmAtraso(),
                mensagem
        );
    }
}
