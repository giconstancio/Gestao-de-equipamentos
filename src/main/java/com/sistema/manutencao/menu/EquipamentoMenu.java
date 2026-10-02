package com.sistema.manutencao.menu;

import com.sistema.manutencao.auth.SessaoAtual;
import com.sistema.manutencao.dao.EquipamentoDAO;
import com.sistema.manutencao.model.Equipamento;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/* Classe de Menu (CLI) de Equipamento
Responsável por exibir as opções do submenu de Equipamentos no terminal, ler e validar a entrada do usuário e
repassar os dados diretamente para a camada DAO, que realiza a persistência no banco
- Gestor: cadastra, consulta, edita e inativa equipamentos
- Técnico: apenas consulta equipamentos
*/
public class EquipamentoMenu {
    private final EquipamentoDAO equipamentoDAO = new EquipamentoDAO();
    private final Scanner scanner;
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public EquipamentoMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    // Método principal que exibe o submenu de Equipamentos em loop até o usuário optar por voltar
    // As opções de cadastro, edição e inativação só aparecem (e só funcionam) para o gestor
    public void exibirMenu() {
        boolean continuar = true;

        while (continuar) {
            System.out.println("\n===== MENU DE EQUIPAMENTOS =====");
            if (SessaoAtual.isGestor()) System.out.println("1 - Cadastrar equipamento");
            System.out.println("2 - Listar equipamentos");
            System.out.println("3 - Visualizar equipamento por ID");
            if (SessaoAtual.isGestor()) {
                System.out.println("4 - Editar equipamento");
                System.out.println("5 - Inativar equipamento");
            }
            System.out.println("0 - Voltar");
            System.out.print("Escolha uma opção: ");

            String opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1" -> { if (SessaoAtual.isGestor()) cadastrarEquipamento(); else opcaoInvalida(); }
                case "2" -> listarEquipamentos();
                case "3" -> visualizarEquipamento();
                case "4" -> { if (SessaoAtual.isGestor()) editarEquipamento(); else opcaoInvalida(); }
                case "5" -> { if (SessaoAtual.isGestor()) inativarEquipamento(); else opcaoInvalida(); }
                case "0" -> continuar = false;
                default -> opcaoInvalida();
            }
        }
    }

    private void opcaoInvalida() {
        System.out.println(">> Opção inválida. Tente novamente.");
    }

    // Lê os dados do novo equipamento pelo terminal, valida os campos obrigatórios e envia para a DAO cadastrar
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

            // Validação básica de campos obrigatórios
            if (codigoPatrimonio.isBlank() || nome.isBlank() || localizacao.isBlank()) {
                System.out.println(">> Código de patrimônio, nome e localização são obrigatórios.");
                return;
            }

            // Verifica se já existe um equipamento com o mesmo código de patrimônio
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

    // Busca e exibe a lista completa de equipamentos cadastrados em formato de tabela simples
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

    // Solicita o ID do equipamento e exibe todos os seus detalhes
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

    // Edita os dados de um equipamento; deixar o campo em branco (Enter) mantém o valor atual
    private void editarEquipamento() {
        System.out.println("\n--- Editar Equipamento ---");
        try {
            System.out.print("Informe o ID do equipamento: ");
            int id = Integer.parseInt(scanner.nextLine().trim());

            Equipamento e = equipamentoDAO.buscarEquipamentoPorId(id);
            if (e == null) {
                System.out.println(">> Equipamento não encontrado.");
                return;
            }

            System.out.print("Código de patrimônio [" + e.getCodigoPatrimonio() + "]: ");
            String codigo = scanner.nextLine().trim();
            if (!codigo.isBlank() && !codigo.equals(e.getCodigoPatrimonio())) {
                // O código de patrimônio é único: impede trocar para um código que já pertence a outro equipamento
                if (equipamentoDAO.buscarEquipamentoPorCodigo(codigo) != null) {
                    System.out.println(">> Já existe um equipamento cadastrado com este código de patrimônio.");
                    return;
                }
                e.setCodigoPatrimonio(codigo);
            }

            System.out.print("Nome [" + e.getNome() + "]: ");
            String nome = scanner.nextLine().trim();
            if (!nome.isBlank()) e.setNome(nome);

            System.out.print("Descrição [" + (e.getDescricao() != null ? e.getDescricao() : "-") + "]: ");
            String descricao = scanner.nextLine().trim();
            if (!descricao.isBlank()) e.setDescricao(descricao);

            System.out.print("Localização [" + e.getLocalizacao() + "]: ");
            String localizacao = scanner.nextLine().trim();
            if (!localizacao.isBlank()) e.setLocalizacao(localizacao);

            // Permite reativar um equipamento que foi inativado anteriormente
            if (!e.isAtivo()) {
                System.out.print("Equipamento inativo. Deseja reativá-lo? (S/N): ");
                if ("S".equalsIgnoreCase(scanner.nextLine().trim())) e.setAtivo(true);
            }

            equipamentoDAO.editarEquipamento(e);
            System.out.println(">> Equipamento atualizado com sucesso.");
        } catch (NumberFormatException e) {
            System.out.println(">> ID inválido. Informe um número inteiro.");
        } catch (Exception e) {
            System.out.println(">> Erro ao editar equipamento: " + e.getMessage());
        }
    }

    // Soft delete do equipamento (ativo = false), com confirmação antes de executar
    // Equipamentos inativos continuam no histórico, mas não aparecem para abertura de novas OS
    private void inativarEquipamento() {
        System.out.println("\n--- Inativar Equipamento ---");
        try {
            System.out.print("Informe o ID do equipamento: ");
            int id = Integer.parseInt(scanner.nextLine().trim());

            Equipamento e = equipamentoDAO.buscarEquipamentoPorId(id);
            if (e == null) {
                System.out.println(">> Equipamento não encontrado.");
                return;
            }
            if (!e.isAtivo()) {
                System.out.println(">> Este equipamento já está inativo.");
                return;
            }

            System.out.print("Confirmar inativação de \"" + e.getNome() + "\" (S/N): ");
            if ("S".equalsIgnoreCase(scanner.nextLine().trim())) {
                equipamentoDAO.inativarEquipamento(id);
                System.out.println(">> Equipamento inativado.");
            } else {
                System.out.println(">> Inativação cancelada.");
            }
        } catch (NumberFormatException e) {
            System.out.println(">> ID inválido. Informe um número inteiro.");
        } catch (Exception e) {
            System.out.println(">> Erro ao inativar equipamento: " + e.getMessage());
        }
    }
}
