package com.biblioteca.cliente;

import java.net.URI;
import java.net.http.*;
import java.util.Scanner;

public class ClienteBibliotecario  {

    public static final Scanner scanner = new Scanner(System.in);
    public static String token;

    public static void main(String[] args) {
        if (!autenticar()) {
            return;
        }

        int opcao=0;

        while(opcao!=5){
            System.out.println("biblioteca");
            System.out.println("1 cadastrar livro");
            System.out.println("2 consultar livros");
            System.out.println("3 atualizar livro");
            System.out.println("4 deletar livro");
            System.out.println("5 sair");
            System.out.println("escolha uma opcao");

            if(!scanner.hasNextInt()){
                System.out.println("numero invalido \n Digite novamente");
                scanner.next();
                continue;

            }

            opcao=scanner.nextInt();
            scanner.nextLine();
            switch (opcao){
                case 1:
                    cadastrarLivro();
                    break;
                case 2:
                    consultarLivros();
                    break;
                case 3:
                    atualizarLivros();
                    break;
                case 4:
                    deletarLivros();
                    break;
                case 5:
                    System.out.println("Saindo..");
                    break;
                default:
                    System.out.println("opcao invalida");
            }

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

    public static void consultarLivros() {
        try {
            HttpClient cliente = HttpClient.newHttpClient();

            HttpRequest requisicao = comToken("http://localhost:8080/livros").GET().build();

            HttpResponse<String> resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());

            System.out.println("Livros cadastrados");
            System.out.println(resposta.body());

        } catch (Exception e) {
            System.out.println("Erro na consulta: " + e.getMessage());
        }
    }


    public static void cadastrarLivro() {

        System.out.println("Digite o nome do livro: ");
        String titulo = scanner.nextLine();

        System.out.println("Digite o autor do livro: ");
        String autor = scanner.nextLine();

        String json = String.format("{\"titulo\":\"%s\",\"autor\":\"%s\"}", titulo,autor);
        try {
            HttpClient cliente = HttpClient.newHttpClient();

            HttpRequest requisicao = comToken("http://localhost:8080/livros")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json)).build();

            HttpResponse<String> resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());

            if(resposta.statusCode() == 200 || resposta.statusCode() == 201) {
                System.out.println("Livro cadastrado com sucesso");
                System.out.println(resposta.body());

            }else  {
                System.out.println("Erro na consulta: " + resposta.statusCode());
            }
        } catch (Exception e) {
            System.out.println("Erro no cadastro de livro : " + e.getMessage());
        }
    }

    public static void  deletarLivros() {

        try {
            // dentro do try caso seja um input incorreto nao quebra sistema
            System.out.println("Digite o Id do livro: ");
            int id = scanner.nextInt();

            HttpClient cliente = HttpClient.newHttpClient();

            HttpRequest requisicao = comToken("http://localhost:8080/livros/" + id)
                    .DELETE().build();

            HttpResponse<String> resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());
            System.out.println(resposta.body());
        } catch (Exception e) {
            System.out.println("Erro ao deletar livro " + e.getMessage());
        }

    }

    public static void atualizarLivros() {

        try {
            // trazendo leitura no try para evitar quebra de sistema
            System.out.println("digite o id de livro");
            int id  = scanner.nextInt();
            scanner.nextLine(); // para limpar o buffer e o titulo do livro nao ficar vazio
            System.out.println("Digite o nome do livro: ");
            String titulo = scanner.nextLine();
            System.out.println("Digite o autor do livro: ");
            String autor = scanner.nextLine();

            String json = String.format("{\"titulo\":\"%s\",\"autor\":\"%s\"}", titulo,autor);

            HttpClient cliente = HttpClient.newHttpClient();

            HttpRequest requisicao = comToken("http://localhost:8080/livros/" + id)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json)).build();

            HttpResponse<String> resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());
            System.out.println(resposta.body());
        } catch (Exception e) {
            System.out.println("Erro ao atualizar livro " + e.getMessage());
        }
    }
}