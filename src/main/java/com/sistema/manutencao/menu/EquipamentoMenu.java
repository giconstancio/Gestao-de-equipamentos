package com.sistema.manutencao.menu;

import com.sistema.manutencao.dao.EquipamentoDAO;
import com.sistema.manutencao.model.Equipamento;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class EquipamentoMenu {
    private final EquipamentoDAO equipamentoDAO = new EquipamentoDAO();
    private final Scanner scanner;
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public EquipamentoMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    public void exibirMenu() {
        boolean continuar = true;

        while (continuar) {
            System.out.println("\n===== MENU DE EQUIPAMENTOS =====");
            System.out.println("1 - Cadastrar equipamento");
            System.out.println("2 - Listar equipamentos");
            System.out.println("3 - Visualizar equipamento por ID");
            System.out.println("0 - Voltar");
            System.out.print("Escolha uma opção: ");

            String opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1" -> cadastrarEquipamento();
                case "2" -> listarEquipamentos();
                case "3" -> visualizarEquipamento();
                case "0" -> continuar = false;
                default -> System.out.println(">> Opção inválida. Tente novamente.");
            }
        }
    }

    private void cadastrarEquipamento() {
        System.out.println("\n--- Cadastro de Equipamento ---");
        try {
            System.out.print("Código de patrimônio: ");
            String codigoPatrimonio = scanner.nextLine().trim();

            System.out.print("Nome: ");
            String nome = scanner.nextLine().trim();

            System.out.print("Descrição (opcional): ");
            String descricao = scanner.nextLine().trim();

            System.out.print("Localização: ");
            String localizacao = scanner.nextLine().trim();

            if (codigoPatrimonio.isBlank() || nome.isBlank() || localizacao.isBlank()) {
                System.out.println(">> Código de patrimônio, nome e localização são obrigatórios.");
                return;
            }

            if (equipamentoDAO.buscarEquipamentoPorCodigo(codigoPatrimonio) != null) {
                System.out.println(">> Já existe um equipamento cadastrado com este código de patrimônio.");
                return;
            }

            Equipamento equipamento = new Equipamento();
            equipamento.setCodigoPatrimonio(codigoPatrimonio);
            equipamento.setNome(nome);
            equipamento.setDescricao(descricao.isBlank() ? null : descricao);
            equipamento.setLocalizacao(localizacao);
            equipamento.setAtivo(true);

            equipamento = equipamentoDAO.cadastrarEquipamento(equipamento);

            System.out.println(">> Equipamento cadastrado com sucesso! ID gerado: " + equipamento.getIdEquipamento());
        } catch (Exception e) {
            System.out.println(">> Erro ao cadastrar equipamento: " + e.getMessage());
        }
    }

    private void listarEquipamentos() {
        System.out.println("\n--- Lista de Equipamentos ---");
        try {
            List<Equipamento> equipamentos = equipamentoDAO.listarEquipamentos();

            if (equipamentos.isEmpty()) {
                System.out.println(">> Nenhum equipamento cadastrado.");
                return;
            }

            System.out.printf("%-5s %-15s %-25s %-20s %-8s%n", "ID", "PATRIMÔNIO", "NOME", "LOCALIZAÇÃO", "ATIVO");
            System.out.println("-".repeat(80));
            for (Equipamento e : equipamentos) {
                System.out.printf("%-5d %-15s %-25s %-20s %-8s%n",
                        e.getIdEquipamento(),
                        e.getCodigoPatrimonio(),
                        e.getNome(),
                        e.getLocalizacao(),
                        e.isAtivo() ? "Sim" : "Não");
            }
        } catch (Exception e) {
            System.out.println(">> Erro ao listar equipamentos: " + e.getMessage());
        }
    }

    private void visualizarEquipamento() {
        System.out.println("\n--- Visualizar Equipamento ---");
        try {
            System.out.print("Informe o ID do equipamento: ");
            int id = Integer.parseInt(scanner.nextLine().trim());

            Equipamento e = equipamentoDAO.buscarEquipamentoPorId(id);

            if (e == null) {
                System.out.println(">> Equipamento não encontrado.");
                return;
            }

            System.out.println("\nID: " + e.getIdEquipamento());
            System.out.println("Código de patrimônio: " + e.getCodigoPatrimonio());
            System.out.println("Nome: " + e.getNome());
            System.out.println("Descrição: " + (e.getDescricao() != null ? e.getDescricao() : "-"));
            System.out.println("Localização: " + e.getLocalizacao());
            System.out.println("Ativo: " + (e.isAtivo() ? "Sim" : "Não"));
            System.out.println("Cadastrado em: " + (e.getCriadoEm() != null ? e.getCriadoEm().format(FORMATO_DATA) : "-"));
        } catch (NumberFormatException e) {
            System.out.println(">> ID inválido. Informe um número inteiro.");
        } catch (Exception e) {
            System.out.println(">> Erro ao visualizar equipamento: " + e.getMessage());
        }
    }
}