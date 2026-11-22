package com.biblioteca.cliente;

import java.net.URI;
import java.net.http.*;
import java.util.Scanner;

public class ClienteAluno {

    public static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        int opcao = 0;

        while (opcao != 5) {
            System.out.println("biblioteca - aluno");
            System.out.println("1 consultar livros");
            System.out.println("2 buscar livro por id");
            System.out.println("3 emprestar livro");
            System.out.println("4 devolver livro");
            System.out.println("5 sair");

            Integer escolha = lerInteiro("escolha uma opcao");
            if (escolha == null) {
                continue;
            }
            opcao = escolha;

            switch (opcao) {
                case 1:
                    consultarLivros();
                    break;
                case 2:
                    buscarLivroPorId();
                    break;
                case 3:
                    emprestarLivro();
                    break;
                case 4:
                    devolverLivro();
                    break;
                case 5:
                    System.out.println("Saindo..");
                    break;
                default:
                    System.out.println("opcao invalida");
            }
        }
    }

    // le uma linha e converte para numero, devolve null se nao for numero valido
    public static Integer lerInteiro(String mensagem) {
        System.out.println(mensagem);
        String linha = scanner.nextLine().trim();
        try {
            return Integer.parseInt(linha);
        } catch (NumberFormatException e) {
            System.out.println("numero invalido \nDigite novamente");
            return null;
        }
    }

    public static void consultarLivros() {
        try {
            HttpClient cliente = HttpClient.newHttpClient();

            HttpRequest requisicao = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/livros")).GET().build();

            HttpResponse<String> resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());

            System.out.println("Livros disponiveis");
            System.out.println(resposta.body());

        } catch (Exception e) {
            System.out.println("Erro na consulta: " + e.getMessage());
        }
    }

    public static void buscarLivroPorId() {
        Integer id = lerInteiro("Digite o Id do livro: ");
        if (id == null) {
            return;
        }

        try {
            HttpClient cliente = HttpClient.newHttpClient();

            HttpRequest requisicao = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/livros/" + id)).GET().build();

            HttpResponse<String> resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());

            if (resposta.statusCode() == 200) {
                System.out.println(resposta.body());
            } else {
                System.out.println("Livro nao encontrado. Codigo: " + resposta.statusCode());
            }
        } catch (Exception e) {
            System.out.println("Erro na busca: " + e.getMessage());
        }
    }

    public static void emprestarLivro() {
        Integer id = lerInteiro("Digite o Id do livro: ");
        if (id == null) {
            return;
        }

        try {
            HttpClient cliente = HttpClient.newHttpClient();

            HttpRequest requisicao = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/livros/" + id + "/emprestar"))
                    .PUT(HttpRequest.BodyPublishers.noBody()).build();

            HttpResponse<String> resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());

            if (resposta.statusCode() == 200) {
                System.out.println(resposta.body());
            } else {
                System.out.println("Erro ao emprestar. Codigo: " + resposta.statusCode());
            }
        } catch (Exception e) {
            System.out.println("Erro ao emprestar livro: " + e.getMessage());
        }
    }

    public static void devolverLivro() {
        Integer id = lerInteiro("Digite o Id do livro: ");
        if (id == null) {
            return;
        }

        try {
            HttpClient cliente = HttpClient.newHttpClient();

            HttpRequest requisicao = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/livros/" + id + "/devolver"))
                    .PUT(HttpRequest.BodyPublishers.noBody()).build();

            HttpResponse<String> resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());

            if (resposta.statusCode() == 200) {
                System.out.println(resposta.body());
            } else {
                System.out.println("Erro ao devolver. Codigo: " + resposta.statusCode());
            }
        } catch (Exception e) {
            System.out.println("Erro ao devolver livro: " + e.getMessage());
        }
    }
}
