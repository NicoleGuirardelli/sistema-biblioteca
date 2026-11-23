package com.biblioteca.cliente;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ClienteAluno {

    public static final Scanner scanner = new Scanner(System.in);
    public static String token;

    public static void main(String[] args) {
        if (!autenticar()) {
            return;
        }

        int opcao = 0;

        while (opcao != 5) {
            System.out.println("biblioteca - aluno");
            System.out.println("1 consultar livros");
            System.out.println("2 buscar livro por id");
            System.out.println("3 emprestar livro");
            System.out.println("4 devolver livro");
            System.out.println("5 sair");
            System.out.println("6 meus emprestimos");

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
                case 6:
                    meusEmprestimos();
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

    public static boolean autenticar() {
        System.out.println("Digite a matricula: ");
        String matricula = scanner.nextLine().trim();
        String json = String.format("{\"matricula\":\"%s\"}", matricula);
        try {
            HttpClient cliente = HttpClient.newHttpClient();
            HttpRequest requisicao = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());
            if (resposta.statusCode() != 200) {
                System.out.println(resposta.body());
                return false;
            }
            String corpo = resposta.body();
            int inicio = corpo.indexOf("\"token\":\"") + "\"token\":\"".length();
            token = corpo.substring(inicio, corpo.indexOf('"', inicio));
            System.out.println("Login realizado");
            return true;
        } catch (Exception e) {
            System.out.println("Erro no login: " + e.getMessage());
            return false;
        }
    }

    public static HttpRequest.Builder comToken(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token);
    }

    public static void meusEmprestimos() {
        try {
            HttpClient cliente = HttpClient.newHttpClient();
            HttpRequest requisicao = comToken("http://localhost:8080/emprestimos/meus").GET().build();
            HttpResponse<String> resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());
            System.out.println(resposta.body());
        } catch (Exception e) {
            System.out.println("Erro ao consultar emprestimos: " + e.getMessage());
        }
    }

    public static void consultarLivros() {
        try {
            System.out.println("Digite titulo ou categoria, ou enter para listar todos: ");
            String termo = scanner.nextLine().trim();
            String url = termo.isEmpty()
                    ? "http://localhost:8080/livros"
                    : "http://localhost:8080/livros?q=" + URLEncoder.encode(termo, StandardCharsets.UTF_8);

            HttpClient cliente = HttpClient.newHttpClient();
            HttpRequest requisicao = comToken(url).GET().build();

            HttpResponse<String> resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());

            if (resposta.body() != null && resposta.body().trim().equals("[]")) {
                System.out.println("Nenhum resultado");
            } else {
                System.out.println("Livros disponiveis");
                System.out.println(resposta.body());
            }

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

            HttpRequest requisicao = comToken("http://localhost:8080/livros/" + id).GET().build();

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

            String json = "{\"livroId\":" + id + "}";
            HttpRequest requisicao = comToken("http://localhost:8080/emprestimos")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json)).build();

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
        Integer id = lerInteiro("Digite o Id do emprestimo: ");
        if (id == null) {
            return;
        }

        try {
            HttpClient cliente = HttpClient.newHttpClient();

            HttpRequest requisicao = comToken("http://localhost:8080/emprestimos/" + id + "/devolucao")
                    .POST(HttpRequest.BodyPublishers.noBody()).build();

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
