package com.sistema.manutencao.menu;

import com.sistema.manutencao.auth.SessaoAtual;
import com.sistema.manutencao.dao.EquipamentoDAO;
import com.sistema.manutencao.dao.HistoricoDAO;
import com.sistema.manutencao.dao.OrdemServicoDAO;
import com.sistema.manutencao.model.Equipamento;
import com.sistema.manutencao.model.Historico;
import com.sistema.manutencao.model.OrdemServico;
import com.sistema.manutencao.model.Usuario;
import com.sistema.manutencao.model.enums.CriticidadeOS;
import com.sistema.manutencao.model.enums.PerfilUsuario;
import com.sistema.manutencao.model.enums.StatusOS;
import com.sistema.manutencao.model.enums.StatusUsuario;
import com.sistema.manutencao.service.OrdemServicoStatusService;
import com.sistema.manutencao.service.UsuarioService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/* Classe de Menu (CLI) de Ordem de Serviço
Responsável por exibir o submenu de Ordens de Serviço, que muda de acordo com o perfil logado:
- Gestor: atribui técnicos, encerra administrativamente e cancela OS
- Técnico: registra diagnóstico, aponta reparo, altera o status operacional e submete a conclusão técnica
Toda ação relevante é registrada no histórico da OS através da OrdemServicoStatusService
*/
public class OrdemServicoMenu {
    private final Scanner scanner;
    private final OrdemServicoDAO osDao = new OrdemServicoDAO();
    private final EquipamentoDAO equipamentoDao = new EquipamentoDAO();
    private final HistoricoDAO historicoDao = new HistoricoDAO();
    private final UsuarioService usuarioService = new UsuarioService();
    private final OrdemServicoStatusService statusService = new OrdemServicoStatusService();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public OrdemServicoMenu(Scanner scanner) { this.scanner = scanner; }

    // Exibe o submenu em loop; as opções 6 a 9 variam conforme o perfil do usuário logado
    public void exibirMenu() {
        boolean cont = true;
        while (cont) {
            System.out.println("\n===== ORDENS DE SERVIÇO =====");
            System.out.println("1 - Abrir chamado/OS");
            System.out.println("2 - Listar OS");
            System.out.println("3 - Visualizar OS e histórico");
            System.out.println("4 - Filtrar OS por status");
            System.out.println("5 - Ver minhas OS");
            if (SessaoAtual.isGestor()) {
                System.out.println("6 - Atribuir/reatribuir técnico");
                System.out.println("7 - Encerrar OS administrativamente");
                System.out.println("8 - Cancelar OS");
            } else {
                System.out.println("6 - Registrar diagnóstico");
                System.out.println("7 - Apontar reparo");
                System.out.println("8 - Atualizar status operacional");
                System.out.println("9 - Concluir tecnicamente (submeter)");
            }
            System.out.println("0 - Voltar");
            System.out.print("Escolha: ");

            String op = scanner.nextLine().trim();
            // Qualquer erro de validação lançado pelas ações é exibido aqui, mantendo o usuário no submenu
            try {
                switch (op) {
                    case "1" -> abrir();
                    case "2" -> listar(osDao.listarOrdensServico());
                    case "3" -> visualizar();
                    case "4" -> filtrar();
                    case "5" -> minhasOS();
                    case "6" -> { if (SessaoAtual.isGestor()) atribuir(); else diagnostico(); }
                    case "7" -> { if (SessaoAtual.isGestor()) encerrar(); else apontarReparo(); }
                    case "8" -> { if (SessaoAtual.isGestor()) cancelar(); else statusTecnico(); }
                    case "9" -> { if (SessaoAtual.isTecnico()) concluirTecnico(); else System.out.println(">> Opção inválida."); }
                    case "0" -> cont = false;
                    default -> System.out.println(">> Opção inválida.");
                }
            } catch (Exception e) { System.out.println(">> " + e.getMessage()); }
        }
    }

    // Abre um novo chamado (OS) para um equipamento ativo; o status inicial ABERTA é definido pelo banco
    private void abrir() {
        System.out.println("\n--- Abertura de chamado ---");
        listarEquipamentosAtivos();
        int equipamentoId = lerId("ID do equipamento: ");
        Equipamento equipamento = equipamentoDao.buscarEquipamentoPorId(equipamentoId);
        if (equipamento == null || !equipamento.isAtivo())
            throw new IllegalArgumentException("Equipamento inexistente ou inativo.");

        System.out.print("Descrição da falha: ");
        String falha = scanner.nextLine().trim();
        if (falha.isBlank()) throw new IllegalArgumentException("A descrição da falha é obrigatória.");

        CriticidadeOS criticidade = lerCriticidade();
        OrdemServico os = new OrdemServico();
        os.setEquipamentoId(equipamentoId);
        // O banco mantém o nome gestor_abertura_id. Como o requisito permite
        // abertura por gestor e técnico, este campo guarda o usuário que abriu o chamado.
        os.setGestorAberturaId(SessaoAtual.getUsuarioLogado().getIdUsuario());
        os.setDescricaoFalha(falha);
        os.setCriticidade(criticidade);
        osDao.abrirOrdemServico(os);

        // Primeiro registro do histórico: não existe status anterior, por isso é passado null
        statusService.registrarHistorico(os.getIdOrdemServico(),
                SessaoAtual.getUsuarioLogado().getIdUsuario(),
                "OS aberta pelo usuário " + SessaoAtual.getUsuarioLogado().getNome() + ".",
                null, StatusOS.ABERTA);

        System.out.println(">> OS aberta com sucesso. Número: " + os.getIdOrdemServico());
    }

    // Exibe os equipamentos ativos para ajudar o usuário a escolher o ID na abertura do chamado
    private void listarEquipamentosAtivos() {
        List<Equipamento> equipamentos = equipamentoDao.listarEquipamentosAtivos();
        if (equipamentos.isEmpty())
            throw new IllegalStateException("Nenhum equipamento ativo cadastrado. Cadastre um equipamento antes de abrir uma OS.");
        System.out.printf("%-5s %-18s %-30s %-25s%n", "ID","PATRIMÔNIO","NOME","LOCALIZAÇÃO");
        for (Equipamento e : equipamentos)
            System.out.printf("%-5d %-18s %-30s %-25s%n", e.getIdEquipamento(), e.getCodigoPatrimonio(), e.getNome(), e.getLocalizacao());
    }

    // Exibe uma lista de OS em formato de tabela
    private void listar(List<OrdemServico> lista) {
        if (lista.isEmpty()) { System.out.println(">> Nenhuma OS encontrada."); return; }
        System.out.printf("%-5s %-8s %-10s %-18s %-10s %-10s%n",
                "ID","EQUIP.","CRIT.","STATUS","TÉCNICO","ABERTA EM");
        System.out.println("-".repeat(80));
        for (OrdemServico os : lista)
            System.out.printf("%-5d %-8d %-10s %-18s %-10s %-10s%n",
                    os.getIdOrdemServico(), os.getEquipamentoId(), os.getCriticidade(), os.getStatus(),
                    os.getTecnicoId() == null ? "-" : os.getTecnicoId().toString(),
                    os.getAbertoEm() == null ? "-" : os.getAbertoEm().format(FMT));
    }

    // Exibe todos os detalhes de uma OS, incluindo o equipamento, o técnico e o histórico de intervenções
    private void visualizar() {
        int id = lerId("ID da OS: ");
        OrdemServico os = osDao.buscarOrdemServicoPorId(id);
        if (os == null) { System.out.println(">> OS não encontrada."); return; }

        Equipamento equipamento = equipamentoDao.buscarEquipamentoPorId(os.getEquipamentoId());

        System.out.println("\n===== DETALHES DA OS #" + os.getIdOrdemServico() + " =====");
        System.out.println("Equipamento: " + os.getEquipamentoId()
                + (equipamento == null ? "" : " - " + equipamento.getNome()
                + " (" + equipamento.getCodigoPatrimonio() + ", " + equipamento.getLocalizacao() + ")"));
        System.out.println("Técnico: " + (os.getTecnicoId() == null ? "Não atribuído" : descreverUsuario(os.getTecnicoId())));
        System.out.println("Usuário de abertura: " + descreverUsuario(os.getGestorAberturaId()));
        System.out.println("Falha: " + os.getDescricaoFalha());
        System.out.println("Criticidade: " + os.getCriticidade());
        System.out.println("Status: " + os.getStatus());
        System.out.println("Diagnóstico: " + valor(os.getDiagnosticoInicial()));
        System.out.println("Causa-raiz: " + valor(os.getCausaRaiz()));
        System.out.println("Horas trabalhadas: " + (os.getHorasTrabalhadas() == null ? "0.00" : os.getHorasTrabalhadas()));
        System.out.println("Peças/materiais: " + valor(os.getPecasUtilizadas()));
        System.out.println("Aberta em: " + format(os.getAbertoEm()));
        System.out.println("Concluída em: " + format(os.getConcluidoEm()));
        System.out.println("\n--- Histórico ---");
        List<Historico> hs = historicoDao.listarPorOrdemServico(id);
        if (hs.isEmpty()) System.out.println("Nenhum registro.");
        for (Historico h : hs)
            System.out.printf("#%d | %s | usuário %d | %s | %s -> %s%n",
                    h.getIdHistorico(), format(h.getRegistradoEm()), h.getUsuarioId(),
                    h.getDescricaoAcao(),
                    h.getStatusAnterior() == null ? "-" : h.getStatusAnterior(), h.getStatusNovo());
    }

    // Lista as OS que estão no status escolhido pelo usuário
    private void filtrar() {
        System.out.println("1-ABERTA 2-EM_EXECUCAO 3-AGUARDANDO_PECA 4-REPARO_FINALIZADO 5-CONCLUIDA 6-CANCELADA");
        System.out.print("Escolha o status: ");
        StatusOS status = statusPorNumero(scanner.nextLine().trim());
        listar(osDao.listarPorStatus(status));
    }

    // Técnico: lista as OS atribuídas a ele / Gestor: lista todas as OS
    private void minhasOS() {
        int id = SessaoAtual.getUsuarioLogado().getIdUsuario();
        if (SessaoAtual.isTecnico()) listar(osDao.listarOrdensServicoPorTecnico(id));
        else listar(osDao.listarOrdensServico());
    }

    // Gestor: atribui (ou reatribui) a OS a um técnico ativo e atualiza a disponibilidade dos técnicos envolvidos
    private void atribuir() {
        int idOs = lerId("ID da OS: ");
        OrdemServico os = exigirOS(idOs);
        if (isEncerrada(os))
            throw new IllegalStateException("Não é possível atribuir uma OS encerrada/cancelada.");

        System.out.println("--- Técnicos disponíveis ---");
        List<Usuario> disponiveis = usuarioService.listarTecnicosDisponiveis();
        if (disponiveis.isEmpty()) System.out.println("(nenhum técnico com status DISPONIVEL no momento)");
        for (Usuario t : disponiveis)
            System.out.printf("%d - %s (%s)%n", t.getIdUsuario(), t.getNome(), t.getStatus());

        int tecnicoId = lerId("ID do técnico: ");
        Usuario tecnico = usuarioService.buscarPorId(tecnicoId);
        if (tecnico.getPerfil() != PerfilUsuario.TECNICO || !tecnico.isAtivo())
            throw new IllegalArgumentException("Usuário não é um técnico ativo.");

        Integer tecnicoAnterior = os.getTecnicoId();
        if (tecnicoAnterior != null && tecnicoAnterior == tecnicoId)
            throw new IllegalArgumentException("A OS já está atribuída a este técnico.");

        osDao.atribuirTecnico(idOs, tecnicoId);
        usuarioService.alterarDisponibilidade(tecnicoId, StatusUsuario.EM_ATENDIMENTO);

        // Em caso de reatribuição, libera o técnico anterior se ele não tiver outras OS em andamento
        if (tecnicoAnterior != null) liberarTecnico(tecnicoAnterior, idOs);

        statusService.registrarHistorico(idOs, SessaoAtual.getUsuarioLogado().getIdUsuario(),
                (tecnicoAnterior == null ? "OS atribuída ao técnico " : "OS reatribuída ao técnico ")
                        + tecnico.getNome() + ".",
                os.getStatus(), os.getStatus());
        System.out.println(">> Técnico atribuído.");
    }

    // Técnico: registra o parecer inicial e a causa-raiz da falha
    private void diagnostico() {
        OrdemServico os = exigirOS(lerId("ID da OS: "));
        validarTecnicoDaOS(os);
        if (isEncerrada(os))
            throw new IllegalStateException("OS encerrada/cancelada.");
        System.out.print("Parecer técnico inicial: ");
        String diag = scanner.nextLine().trim();
        System.out.print("Causa-raiz identificada: ");
        String causa = scanner.nextLine().trim();
        if (diag.isBlank() || causa.isBlank()) throw new IllegalArgumentException("Diagnóstico e causa-raiz são obrigatórios.");
        osDao.atualizarDiagnostico(os.getIdOrdemServico(), diag, causa);
        statusService.registrarHistorico(os.getIdOrdemServico(), SessaoAtual.getUsuarioLogado().getIdUsuario(),
                "Diagnóstico técnico registrado.", os.getStatus(), os.getStatus());
        System.out.println(">> Diagnóstico registrado.");
    }

    // Técnico: registra horas trabalhadas, peças/materiais e a intervenção realizada
    private void apontarReparo() {
        OrdemServico os = exigirOS(lerId("ID da OS: "));
        validarTecnicoDaOS(os);
        if (isEncerrada(os))
            throw new IllegalStateException("OS encerrada/cancelada.");
        BigDecimal horas = lerHoras();
        System.out.print("Peças/materiais utilizados: ");
        String pecas = scanner.nextLine().trim();
        System.out.print("Intervenção realizada: ");
        String intervencao = scanner.nextLine().trim();
        if (intervencao.isBlank()) throw new IllegalArgumentException("Descreva a intervenção.");
        osDao.atualizarReparo(os.getIdOrdemServico(), horas, pecas.isBlank() ? null : pecas);
        statusService.registrarHistorico(os.getIdOrdemServico(), SessaoAtual.getUsuarioLogado().getIdUsuario(),
                "Reparo executado: " + intervencao + ". Horas: " + horas + ". Peças/materiais: " + (pecas.isBlank() ? "nenhum" : pecas) + ".",
                os.getStatus(), os.getStatus());
        System.out.println(">> Apontamento registrado.");
    }

    // Técnico: altera o status operacional da OS (a service registra a mudança no histórico)
    private void statusTecnico() {
        OrdemServico os = exigirOS(lerId("ID da OS: "));
        validarTecnicoDaOS(os);
        // Impede que o técnico "reabra" uma OS que já foi concluída ou cancelada pelo gestor
        if (isEncerrada(os))
            throw new IllegalStateException("OS encerrada/cancelada. O status não pode mais ser alterado.");
        System.out.println("Status atual: " + os.getStatus());
        System.out.println("1-EM_EXECUCAO 2-AGUARDANDO_PECA 3-REPARO_FINALIZADO");
        System.out.print("Escolha o novo status: ");
        StatusOS novo = switch (scanner.nextLine().trim()) {
            case "1" -> StatusOS.EM_EXECUCAO;
            case "2" -> StatusOS.AGUARDANDO_PECA;
            case "3" -> StatusOS.REPARO_FINALIZADO;
            default -> throw new IllegalArgumentException("Status inválido.");
        };
        System.out.print("Observação (opcional): ");
        String desc = scanner.nextLine().trim();
        statusService.alterarStatus(os.getIdOrdemServico(), novo,
                SessaoAtual.getUsuarioLogado().getIdUsuario(), desc);
        System.out.println(">> Status atualizado.");
    }

    // Técnico: submete a OS (já em REPARO_FINALIZADO) para o encerramento administrativo do gestor
    private void concluirTecnico() {
        OrdemServico os = exigirOS(lerId("ID da OS: "));
        validarTecnicoDaOS(os);
        if (os.getStatus() != StatusOS.REPARO_FINALIZADO)
            throw new IllegalStateException("Antes de submeter para encerramento, a OS deve estar em REPARO_FINALIZADO.");
        System.out.print("Testes funcionais realizados e aprovados? (S/N): ");
        if (!"S".equalsIgnoreCase(scanner.nextLine().trim())) {
            System.out.println(">> OS não submetida.");
            return;
        }
        statusService.registrarHistorico(os.getIdOrdemServico(), SessaoAtual.getUsuarioLogado().getIdUsuario(),
                "Conclusão técnica submetida após testes funcionais.",
                os.getStatus(), os.getStatus());
        System.out.println(">> OS pronta para aprovação/encerramento administrativo do gestor.");
    }

    // Gestor: valida o reparo e fecha a OS como CONCLUIDA, liberando o técnico responsável
    private void encerrar() {
        OrdemServico os = exigirOS(lerId("ID da OS: "));
        if (os.getStatus() != StatusOS.REPARO_FINALIZADO)
            throw new IllegalStateException("Apenas OS em REPARO_FINALIZADO podem ser encerradas administrativamente.");
        System.out.print("Validar reparo e fechar OS? (S/N): ");
        if (!"S".equalsIgnoreCase(scanner.nextLine().trim())) {
            System.out.println(">> Encerramento cancelado.");
            return;
        }
        statusService.alterarStatus(os.getIdOrdemServico(), StatusOS.CONCLUIDA,
                SessaoAtual.getUsuarioLogado().getIdUsuario(),
                "Reparo validado e OS encerrada administrativamente pelo gestor.");
        if (os.getTecnicoId() != null) liberarTecnico(os.getTecnicoId(), os.getIdOrdemServico());
        System.out.println(">> OS encerrada.");
    }

    // Gestor: cancela uma OS que ainda não foi encerrada, registrando o motivo no histórico
    private void cancelar() {
        OrdemServico os = exigirOS(lerId("ID da OS: "));
        if (isEncerrada(os))
            throw new IllegalStateException("OS já encerrada/cancelada.");
        System.out.print("Motivo do cancelamento: ");
        String motivo = scanner.nextLine().trim();
        if (motivo.isBlank()) throw new IllegalArgumentException("Informe o motivo.");
        statusService.alterarStatus(os.getIdOrdemServico(), StatusOS.CANCELADA,
                SessaoAtual.getUsuarioLogado().getIdUsuario(), "OS cancelada: " + motivo);
        if (os.getTecnicoId() != null) liberarTecnico(os.getTecnicoId(), os.getIdOrdemServico());
        System.out.println(">> OS cancelada.");
    }

    // Volta o técnico para DISPONIVEL, mas só se ele estiver EM_ATENDIMENTO e não tiver outras OS em andamento
    // (idOsIgnorada é a OS que acabou de ser encerrada/cancelada/reatribuída e por isso não deve ser contada)
    private void liberarTecnico(int tecnicoId, int idOsIgnorada) {
        try {
            boolean possuiOutrasOS = osDao.listarOrdensServicoPorTecnico(tecnicoId).stream()
                    .anyMatch(o -> o.getIdOrdemServico() != idOsIgnorada && !isEncerrada(o));
            Usuario tecnico = usuarioService.buscarPorId(tecnicoId);
            if (!possuiOutrasOS && tecnico.getStatus() == StatusUsuario.EM_ATENDIMENTO) {
                usuarioService.alterarDisponibilidade(tecnicoId, StatusUsuario.DISPONIVEL);
            }
        } catch (Exception ignored) {
            // A liberação do técnico é complementar: uma falha aqui não deve desfazer a ação principal já concluída
        }
    }

    // Garante que apenas o técnico atribuído à OS consiga executar ações técnicas nela
    private void validarTecnicoDaOS(OrdemServico os) {
        if (os.getTecnicoId() == null || os.getTecnicoId() != SessaoAtual.getUsuarioLogado().getIdUsuario())
            throw new IllegalStateException("Esta OS não está atribuída ao técnico logado.");
    }

    // Indica se a OS já está em um estado final (CONCLUIDA ou CANCELADA)
    private boolean isEncerrada(OrdemServico os) {
        return os.getStatus() == StatusOS.CONCLUIDA || os.getStatus() == StatusOS.CANCELADA;
    }

    // Busca a OS pelo ID e lança uma exceção caso ela não exista
    private OrdemServico exigirOS(int id) {
        OrdemServico os = osDao.buscarOrdemServicoPorId(id);
        if (os == null) throw new IllegalArgumentException("OS não encontrada.");
        return os;
    }

    // Lê a criticidade da OS a partir do número digitado
    private CriticidadeOS lerCriticidade() {
        System.out.println("1-BAIXA 2-MEDIA 3-ALTA 4-CRITICA");
        System.out.print("Criticidade: ");
        return switch (scanner.nextLine().trim()) {
            case "1" -> CriticidadeOS.BAIXA;
            case "2" -> CriticidadeOS.MEDIA;
            case "3" -> CriticidadeOS.ALTA;
            case "4" -> CriticidadeOS.CRITICA;
            default -> throw new IllegalArgumentException("Criticidade inválida.");
        };
    }

    // Converte a opção digitada no filtro para o StatusOS correspondente
    private StatusOS statusPorNumero(String n) {
        return switch (n) {
            case "1" -> StatusOS.ABERTA;
            case "2" -> StatusOS.EM_EXECUCAO;
            case "3" -> StatusOS.AGUARDANDO_PECA;
            case "4" -> StatusOS.REPARO_FINALIZADO;
            case "5" -> StatusOS.CONCLUIDA;
            case "6" -> StatusOS.CANCELADA;
            default -> throw new IllegalArgumentException("Status inválido.");
        };
    }

    // Lê um ID numérico do terminal, exibindo uma mensagem amigável caso o valor digitado não seja um número
    private int lerId(String prompt) {
        System.out.print(prompt);
        String valor = scanner.nextLine().trim();
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID inválido: \"" + valor + "\". Informe um número inteiro.");
        }
    }

    // Lê as horas trabalhadas aceitando vírgula ou ponto como separador decimal (ex.: 2,5 ou 2.50)
    // O limite de 999.99 vem da coluna DECIMAL(5,2) do banco
    private BigDecimal lerHoras() {
        System.out.print("Horas trabalhadas (ex.: 2.50): ");
        String valor = scanner.nextLine().trim().replace(",", ".");
        BigDecimal horas;
        try {
            horas = new BigDecimal(valor);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Horas inválidas: \"" + valor + "\". Informe um número (ex.: 2.50).");
        }
        if (horas.signum() < 0) throw new IllegalArgumentException("Horas não podem ser negativas.");
        if (horas.compareTo(new BigDecimal("999.99")) > 0) throw new IllegalArgumentException("Horas devem ser no máximo 999.99.");
        return horas;
    }

    // Monta o texto "ID - Nome" de um usuário para exibição; se não encontrar, mostra apenas o ID
    private String descreverUsuario(int idUsuario) {
        try {
            return idUsuario + " - " + usuarioService.buscarPorId(idUsuario).getNome();
        } catch (Exception e) {
            return String.valueOf(idUsuario);
        }
    }

    // Retorna "-" para textos vazios, deixando a exibição mais limpa
    private String valor(String s) { return s == null || s.isBlank() ? "-" : s; }

    // Formata data/hora no padrão dd/MM/yyyy HH:mm, ou "-" quando não houver valor
    private String format(LocalDateTime dt) { return dt == null ? "-" : dt.format(FMT); }
}
