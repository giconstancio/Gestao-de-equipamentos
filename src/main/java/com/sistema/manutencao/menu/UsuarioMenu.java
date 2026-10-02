package com.sistema.manutencao.menu;

import com.sistema.manutencao.auth.SessaoAtual;
import com.sistema.manutencao.model.Usuario;
import com.sistema.manutencao.model.enums.PerfilUsuario;
import com.sistema.manutencao.model.enums.StatusUsuario;
import com.sistema.manutencao.service.UsuarioService;

import java.util.List;
import java.util.Scanner;

/* Classe de Menu (CLI) de Usuário
Responsável por exibir as opções de usuários e disponibilidade dos técnicos no terminal, ler a entrada do usuário e
repassar os dados para a camada Service, que aplica as regras de negócio antes de chamar a DAO
*/
public class UsuarioMenu {
    private final Scanner scanner;
    private final UsuarioService service = new UsuarioService();

    public UsuarioMenu(Scanner scanner) { this.scanner = scanner; }

    // Exibe a tabela de técnicos com a disponibilidade atual (opção acessível a gestores e técnicos)
    public void exibirDisponibilidade() {
        System.out.println("\n===== DISPONIBILIDADE DOS TÉCNICOS =====");
        List<Usuario> tecnicos = service.listarTecnicos();
        if (tecnicos.isEmpty()) { System.out.println(">> Nenhum técnico cadastrado."); return; }
        System.out.printf("%-5s %-25s %-30s %-18s %-8s%n", "ID","NOME","E-MAIL","STATUS","ATIVO");
        System.out.println("-".repeat(90));
        for (Usuario t : tecnicos) {
            System.out.printf("%-5d %-25s %-30s %-18s %-8s%n",
                    t.getIdUsuario(), t.getNome(), t.getEmail(),
                    t.getStatus() == null ? "-" : t.getStatus(), t.isAtivo() ? "Sim" : "Não");
        }
    }

    // Permite que o técnico logado altere a sua própria disponibilidade
    public void alterarMinhaDisponibilidade() {
        Usuario atual = SessaoAtual.getUsuarioLogado();
        System.out.println("\n===== MINHA DISPONIBILIDADE =====");
        System.out.println("Status atual: " + (atual.getStatus() == null ? "-" : atual.getStatus()));
        System.out.println("1 - DISPONIVEL");
        System.out.println("2 - EM_ATENDIMENTO");
        System.out.println("3 - AUSENTE");
        System.out.print("Escolha: ");
        StatusUsuario status = statusPorOpcao(scanner.nextLine().trim());
        service.alterarDisponibilidade(atual.getIdUsuario(), status);
        atual.setStatus(status); // Mantém o objeto da sessão sincronizado com o banco
        System.out.println(">> Disponibilidade atualizada.");
    }

    // Submenu exclusivo do gestor para administração de usuários e perfis
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
            String op = scanner.nextLine().trim();
            // O try fica em volta de todas as opções para que um erro em uma ação
            // exiba a mensagem e mantenha o usuário dentro deste submenu
            try {
                switch (op) {
                    case "1" -> cadastrar();
                    case "2" -> listar();
                    case "3" -> editar();
                    case "4" -> inativar();
                    case "5" -> alterarDisponibilidadeGestor();
                    case "0" -> cont = false;
                    default -> System.out.println(">> Opção inválida.");
                }
            } catch (Exception e) { System.out.println(">> " + e.getMessage()); }
        }
    }

    // Lê os dados do novo usuário; as validações (campos obrigatórios e email único) ficam na Service
    private void cadastrar() {
        System.out.println("\n--- Novo usuário ---");
        System.out.print("Nome: "); String nome = scanner.nextLine().trim();
        System.out.print("E-mail: "); String email = scanner.nextLine().trim();
        System.out.print("Senha: "); String senha = scanner.nextLine();
        PerfilUsuario perfil = lerPerfil();
        Usuario u = service.cadastrarUsuario(nome, email, senha, perfil);
        System.out.println(">> Usuário criado. ID: " + u.getIdUsuario());
    }

    // Lista todos os usuários (ativos e inativos) em formato de tabela
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

    // Edita os dados do usuário; deixar o campo em branco (Enter) mantém o valor atual
    private void editar() {
        Usuario u = service.buscarPorId(lerInteiro("ID do usuário: "));
        System.out.print("Nome [" + u.getNome() + "]: "); String v = scanner.nextLine().trim(); if (!v.isBlank()) u.setNome(v);
        System.out.print("E-mail [" + u.getEmail() + "]: "); v = scanner.nextLine().trim(); if (!v.isBlank()) u.setEmail(v);
        System.out.print("Senha (Enter mantém): "); v = scanner.nextLine(); if (!v.isBlank()) u.setSenha(v);
        System.out.println("Perfil atual: " + u.getPerfil() + " | 1-GESTOR 2-TECNICO 0-manter");
        v = scanner.nextLine().trim();
        if ("1".equals(v)) u.setPerfil(PerfilUsuario.GESTOR);
        else if ("2".equals(v)) u.setPerfil(PerfilUsuario.TECNICO);
        service.editarUsuario(u);

        // Se o gestor editou a própria conta, atualiza a sessão para refletir os novos dados
        if (u.getIdUsuario() == SessaoAtual.getUsuarioLogado().getIdUsuario()) {
            SessaoAtual.login(u);
        }
        System.out.println(">> Usuário atualizado.");
    }

    // Soft delete do usuário, com confirmação antes de executar
    private void inativar() {
        int id = lerInteiro("ID do usuário: ");
        // Impede que o gestor inative a própria conta e perca o acesso ao sistema durante a sessão
        if (id == SessaoAtual.getUsuarioLogado().getIdUsuario()) {
            throw new IllegalArgumentException("Você não pode inativar o seu próprio usuário.");
        }
        System.out.print("Confirmar inativação (S/N): ");
        if ("S".equalsIgnoreCase(scanner.nextLine().trim())) {
            service.inativarUsuario(id);
            System.out.println(">> Usuário inativado.");
        } else {
            System.out.println(">> Inativação cancelada.");
        }
    }

    // Permite que o gestor altere a disponibilidade de qualquer técnico
    private void alterarDisponibilidadeGestor() {
        int id = lerInteiro("ID do técnico: ");
        System.out.println("1 - DISPONIVEL\n2 - EM_ATENDIMENTO\n3 - AUSENTE");
        System.out.print("Escolha: ");
        StatusUsuario s = statusPorOpcao(scanner.nextLine().trim());
        service.alterarDisponibilidade(id, s);
        System.out.println(">> Disponibilidade atualizada.");
    }

    // Lê o perfil do novo usuário; qualquer opção diferente de 1 ou 2 é rejeitada
    private PerfilUsuario lerPerfil() {
        System.out.print("Perfil (1-GESTOR / 2-TECNICO): ");
        return switch (scanner.nextLine().trim()) {
            case "1" -> PerfilUsuario.GESTOR;
            case "2" -> PerfilUsuario.TECNICO;
            default -> throw new IllegalArgumentException("Perfil inválido.");
        };
    }

    // Converte a opção digitada no menu para o Status do técnico correspondente
    private StatusUsuario statusPorOpcao(String op) {
        return switch (op) {
            case "1" -> StatusUsuario.DISPONIVEL;
            case "2" -> StatusUsuario.EM_ATENDIMENTO;
            case "3" -> StatusUsuario.AUSENTE;
            default -> throw new IllegalArgumentException("Status inválido.");
        };
    }

    // Lê um número inteiro do terminal, exibindo uma mensagem amigável caso o valor digitado não seja numérico
    private int lerInteiro(String prompt) {
        System.out.print(prompt);
        String valor = scanner.nextLine().trim();
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor inválido: \"" + valor + "\". Informe um número inteiro.");
        }
    }
}
