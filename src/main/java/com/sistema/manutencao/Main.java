package com.sistema.manutencao;

import com.sistema.manutencao.auth.SessaoAtual;
import com.sistema.manutencao.config.DatabaseConnection;
import com.sistema.manutencao.menu.EquipamentoMenu;
import com.sistema.manutencao.menu.OrdemServicoMenu;
import com.sistema.manutencao.menu.UsuarioMenu;
import com.sistema.manutencao.service.UsuarioService;

import java.sql.Connection;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final UsuarioService usuarioService = new UsuarioService();

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("   SISTEMA DE GESTÃO DE MANUTENÇÃO");
        System.out.println("==============================================");

        System.out.print("Usuário do MySQL: ");
        String dbUser = scanner.nextLine().trim();

        System.out.print("Senha do MySQL: ");
        String dbPassword = scanner.nextLine();

        DatabaseConnection.configurar(dbUser, dbPassword);

        try (Connection ignored = DatabaseConnection.getConnection()) {
            System.out.println(">> Conexão com o MySQL realizada com sucesso.");
        } catch (Exception e) {
            System.err.println(">> Não foi possível conectar ao banco: " + e.getMessage());
            System.err.println(">> Verifique o usuário, senha, MySQL e banco de dados.");
            return;
        }

        loginLoop();
        scanner.close();
    }

    private static void loginLoop() {
        while (true) {
            System.out.println("\n===== LOGIN =====");
            System.out.println("1 - Entrar");
            System.out.println("0 - Sair");
            System.out.print("Escolha: ");
            String op = scanner.nextLine().trim();

            if ("0".equals(op)) return;
            if (!"1".equals(op)) {
                System.out.println(">> Opção inválida.");
                continue;
            }

            try {
                System.out.print("E-mail: ");
                String email = scanner.nextLine().trim();
                System.out.print("Senha: ");
                String senha = scanner.nextLine();

                SessaoAtual.login(usuarioService.autenticar(email, senha));
                System.out.println("\n>> Login realizado. Olá, " + SessaoAtual.getUsuarioLogado().getNome() + "!");
                menuPrincipal();
                SessaoAtual.logout();
            } catch (Exception e) {
                System.out.println(">> " + mensagem(e));
            }
        }
    }

    private static void menuPrincipal() {
        EquipamentoMenu equipamentoMenu = new EquipamentoMenu(scanner);
        UsuarioMenu usuarioMenu = new UsuarioMenu(scanner);
        OrdemServicoMenu osMenu = new OrdemServicoMenu(scanner);

        boolean continuar = true;
        while (continuar && SessaoAtual.getUsuarioLogado() != null) {
            System.out.println("\n===== MENU PRINCIPAL =====");
            System.out.println("Usuário: " + SessaoAtual.getUsuarioLogado().getNome()
                    + " | Perfil: " + SessaoAtual.getUsuarioLogado().getPerfil());

            System.out.println("1 - Equipamentos");
            System.out.println("2 - Ordens de Serviço");
            System.out.println("3 - Disponibilidade dos técnicos");
            if (SessaoAtual.isGestor()) {
                System.out.println("4 - Usuários e perfis");
            } else {
                System.out.println("4 - Alterar minha disponibilidade");
            }
            System.out.println("0 - Logout");
            System.out.print("Escolha: ");

            String op = scanner.nextLine().trim();
            try {
                switch (op) {
                    case "1" -> equipamentoMenu.exibirMenu();
                    case "2" -> osMenu.exibirMenu();
                    case "3" -> usuarioMenu.exibirDisponibilidade();
                    case "4" -> {
                        if (SessaoAtual.isGestor()) usuarioMenu.exibirMenuGestor();
                        else usuarioMenu.alterarMinhaDisponibilidade();
                    }
                    case "0" -> continuar = false;
                    default -> System.out.println(">> Opção inválida.");
                }
            } catch (Exception e) {
                System.out.println(">> " + mensagem(e));
            }
        }
    }

    private static String mensagem(Exception e) {
        Throwable t = e;
        while (t.getCause() != null && (t.getMessage() == null || t.getMessage().startsWith("java."))) {
            t = t.getCause();
        }
        return t.getMessage() == null ? t.toString() : t.getMessage();
    }
}
