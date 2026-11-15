package com.biblioteca.servidor.model;

public class Livro {
    private Integer id;
    private String titulo;
    private String autor;
    private boolean disponivel;

    public Livro(){}

    public Livro(Integer id, String titulo, String autor) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.disponivel = true;

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
}
