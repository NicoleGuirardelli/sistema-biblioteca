package com.biblioteca.servidor.controller;

import java.time.LocalDate;

public record EmprestimoResponse(
        Integer id,
        Integer usuarioId,
        String nome,
        String matricula,
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
                emprestimo.getUsuario().getNome(),
                emprestimo.getUsuario().getMatricula(),
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
}
