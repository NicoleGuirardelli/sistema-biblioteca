package com.biblioteca.servidor.model;

public class Livro {
    private Integer id;
    private String titulo;
    private String autor;
    private String isbn;
    private String categoria;
    private int quantidadeExemplares;
    private int quantidadeDisponivel;
    private boolean disponivel;

    public Livro(){}

    public Livro(Integer id, String titulo, String autor) {
        this(id, titulo, autor, null, null, 1);
    }

    public Livro(Integer id, String titulo, String autor, String isbn, String categoria, int quantidadeExemplares) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.categoria = categoria;
        this.quantidadeExemplares = quantidadeExemplares;
        this.quantidadeDisponivel = quantidadeExemplares;
        this.disponivel = quantidadeExemplares > 0;
    }

    //métodos get e set
    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;

    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public int getQuantidadeExemplares() {
        return quantidadeExemplares;
    }

    public void setQuantidadeExemplares(int quantidadeExemplares) {
        this.quantidadeExemplares = quantidadeExemplares;
    }

    public int getQuantidadeDisponivel() {
        return quantidadeDisponivel;
    }

    public void setQuantidadeDisponivel(int quantidadeDisponivel) {
        this.quantidadeDisponivel = quantidadeDisponivel;
    }
}
