package com.sistema.manutencao.menu;

import com.sistema.manutencao.auth.SessaoAtual;
import com.sistema.manutencao.model.Usuario;
import com.sistema.manutencao.model.enums.PerfilUsuario;
import com.sistema.manutencao.model.enums.StatusUsuario;
import com.sistema.manutencao.service.UsuarioService;

import java.util.List;
import java.util.Scanner;

public class UsuarioMenu {
    private final Scanner scanner;
    private final UsuarioService service = new UsuarioService();

    public UsuarioMenu(Scanner scanner) { this.scanner = scanner; }

    public void exibirDisponibilidade() {
        System.out.println("\n===== DISPONIBILIDADE DOS TÉCNICOS =====");
        List<Usuario> tecnicos = service.listarTecnicos();
        if (tecnicos.isEmpty()) { System.out.println(">> Nenhum técnico cadastrado."); return; }
        System.out.printf("%-5s %-25s %-30s %-18s %-8s%n", "ID","NOME","E-MAIL","STATUS","ATIVO");
        System.out.println("-".repeat(90));
        for (Usuario t : tecnicos) {
            System.out.printf("%-5d %-25s %-30s %-18s %-8s%n",
                    t.getIdUsuario(), t.getNome(), t.getEmail(),
                    t.getStatus(), t.isAtivo() ? "Sim" : "Não");
        }
    }

    public void alterarMinhaDisponibilidade() {
        Usuario atual = SessaoAtual.getUsuarioLogado();
        System.out.println("\n===== MINHA DISPONIBILIDADE =====");
        System.out.println("1 - DISPONIVEL");
        System.out.println("2 - EM_ATENDIMENTO");
        System.out.println("3 - AUSENTE");
        System.out.print("Escolha: ");
        StatusUsuario status = statusPorOpcao(scanner.nextLine().trim());
        service.alterarDisponibilidade(atual.getIdUsuario(), status);
        atual.setStatus(status);
        System.out.println(">> Disponibilidade atualizada.");
    }

    public void exibirMenuGestor() {
        boolean cont = true;
        while (cont) {
            System.out.println("\n===== USUÁRIOS E PERFIS =====");
            System.out.println("1 - Cadastrar usuário");
            System.out.println("2 - Listar usuários");
            System.out.println("3 - Editar usuário");
            System.out.println("4 - Inativar usuário");
            System.out.println("5 - Alterar disponibilidade de técnico");
            System.out.println("0 - Voltar");
            System.out.print("Escolha: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> cadastrar();
                case "2" -> listar();
                case "3" -> editar();
                case "4" -> inativar();
                case "5" -> alterarDisponibilidadeGestor();
                case "0" -> cont = false;
                default -> System.out.println(">> Opção inválida.");
            }
        }
    }

    private void cadastrar() {
        try {
            System.out.println("\n--- Novo usuário ---");
            System.out.print("Nome: "); String nome = scanner.nextLine().trim();
            System.out.print("E-mail: "); String email = scanner.nextLine().trim();
            System.out.print("Senha: "); String senha = scanner.nextLine();
            PerfilUsuario perfil = lerPerfil();
            Usuario u = service.cadastrarUsuario(nome, email, senha, perfil);
            System.out.println(">> Usuário criado. ID: " + u.getIdUsuario());
        } catch (Exception e) { System.out.println(">> " + e.getMessage()); }
    }

    private void listar() {
        List<Usuario> lista = service.listarUsuarios();
        System.out.printf("%-5s %-25s %-30s %-10s %-18s %-8s%n",
                "ID","NOME","E-MAIL","PERFIL","STATUS","ATIVO");
        System.out.println("-".repeat(105));
        for (Usuario u : lista)
            System.out.printf("%-5d %-25s %-30s %-10s %-18s %-8s%n",
                    u.getIdUsuario(), u.getNome(), u.getEmail(), u.getPerfil(),
                    u.getStatus() == null ? "-" : u.getStatus(), u.isAtivo() ? "Sim" : "Não");
    }

    private void editar() {
        try {
            System.out.print("ID do usuário: ");
            Usuario u = service.buscarPorId(Integer.parseInt(scanner.nextLine().trim()));
            System.out.print("Nome [" + u.getNome() + "]: "); String v = scanner.nextLine().trim(); if (!v.isBlank()) u.setNome(v);
            System.out.print("E-mail [" + u.getEmail() + "]: "); v = scanner.nextLine().trim(); if (!v.isBlank()) u.setEmail(v);
            System.out.print("Senha (Enter mantém): "); v = scanner.nextLine(); if (!v.isBlank()) u.setSenha(v);
            System.out.println("Perfil atual: " + u.getPerfil() + " | 1-GESTOR 2-TECNICO 0-manter");
            v = scanner.nextLine().trim();
            if ("1".equals(v)) u.setPerfil(PerfilUsuario.GESTOR);
            else if ("2".equals(v)) u.setPerfil(PerfilUsuario.TECNICO);
            service.editarUsuario(u);
            System.out.println(">> Usuário atualizado.");
        } catch (Exception e) { System.out.println(">> " + e.getMessage()); }
    }

    private void inativar() {
        try {
            System.out.print("ID do usuário: ");
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Confirmar inativação (S/N): ");
            if ("S".equalsIgnoreCase(scanner.nextLine().trim())) {
                service.inativarUsuario(id);
                System.out.println(">> Usuário inativado.");
            }
        } catch (Exception e) { System.out.println(">> " + e.getMessage()); }
    }

    private void alterarDisponibilidadeGestor() {
        try {
            System.out.print("ID do técnico: ");
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.println("1 - DISPONIVEL\n2 - EM_ATENDIMENTO\n3 - AUSENTE");
            StatusUsuario s = statusPorOpcao(scanner.nextLine().trim());
            service.alterarDisponibilidade(id, s);
            System.out.println(">> Disponibilidade atualizada.");
        } catch (Exception e) { System.out.println(">> " + e.getMessage()); }
    }

    private PerfilUsuario lerPerfil() {
        System.out.print("Perfil (1-GESTOR / 2-TECNICO): ");
        return "1".equals(scanner.nextLine().trim()) ? PerfilUsuario.GESTOR : PerfilUsuario.TECNICO;
    }

    private StatusUsuario statusPorOpcao(String op) {
        return switch (op) {
            case "1" -> StatusUsuario.DISPONIVEL;
            case "2" -> StatusUsuario.EM_ATENDIMENTO;
            case "3" -> StatusUsuario.AUSENTE;
            default -> throw new IllegalArgumentException("Status inválido.");
        };
    }
}
