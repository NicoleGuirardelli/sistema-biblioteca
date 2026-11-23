package com.biblioteca.servidor.model;

import java.time.LocalDate;

public class Emprestimo {
    public static final int PRAZO_DIAS = 14;

    private Integer id;
    private Aluno aluno;
    private Livro livro;
    private LocalDate dataEmprestimo;
    private LocalDate dataPrevistaDevolucao;
    private LocalDate dataDevolucao;
    private SituacaoEmprestimo situacao;

    public Emprestimo() {}

    public Emprestimo(Integer id, Aluno aluno, Livro livro, LocalDate dataEmprestimo) {
        this.id = id;
        this.aluno = aluno;
        this.livro = livro;
        this.dataEmprestimo = dataEmprestimo;
        this.dataPrevistaDevolucao = dataEmprestimo.plusDays(PRAZO_DIAS);
        this.situacao = SituacaoEmprestimo.ATIVO;
    }

    public void registrarDevolucao(LocalDate dataDevolucao) {
        this.dataDevolucao = dataDevolucao;
        this.situacao = SituacaoEmprestimo.ENCERRADO;
    }

    public boolean isAtrasado(LocalDate dataReferencia) {
        if (situacao == SituacaoEmprestimo.ENCERRADO) {
            return dataDevolucao != null && dataDevolucao.isAfter(dataPrevistaDevolucao);
        }
        return dataReferencia.isAfter(dataPrevistaDevolucao);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public Livro getLivro() {
        return livro;
    }

    public void setLivro(Livro livro) {
        this.livro = livro;
    }

    public LocalDate getDataEmprestimo() {
        return dataEmprestimo;
    }

    public void setDataEmprestimo(LocalDate dataEmprestimo) {
        this.dataEmprestimo = dataEmprestimo;
    }

    public LocalDate getDataPrevistaDevolucao() {
        return dataPrevistaDevolucao;
    }

    public void setDataPrevistaDevolucao(LocalDate dataPrevistaDevolucao) {
        this.dataPrevistaDevolucao = dataPrevistaDevolucao;
    }

    public LocalDate getDataDevolucao() {
        return dataDevolucao;
    }

    public void setDataDevolucao(LocalDate dataDevolucao) {
        this.dataDevolucao = dataDevolucao;
    }

    public SituacaoEmprestimo getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoEmprestimo situacao) {
        this.situacao = situacao;
    }
}
