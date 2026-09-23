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
import com.sistema.manutencao.service.OrdemServicoStatusService;
import com.sistema.manutencao.service.UsuarioService;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class OrdemServicoMenu {
    private final Scanner scanner;
    private final OrdemServicoDAO osDao = new OrdemServicoDAO();
    private final EquipamentoDAO equipamentoDao = new EquipamentoDAO();
    private final HistoricoDAO historicoDao = new HistoricoDAO();
    private final UsuarioService usuarioService = new UsuarioService();
    private final OrdemServicoStatusService statusService = new OrdemServicoStatusService();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public OrdemServicoMenu(Scanner scanner) { this.scanner = scanner; }

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

    private void abrir() {
        try {
            System.out.println("\n--- Abertura de chamado ---");
            listarEquipamentosAtivos();
            System.out.print("ID do equipamento: ");
            int equipamentoId = Integer.parseInt(scanner.nextLine().trim());
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

            statusService.registrarHistorico(os.getIdOrdemServico(),
                    SessaoAtual.getUsuarioLogado().getIdUsuario(),
                    "OS aberta pelo usuário " + SessaoAtual.getUsuarioLogado().getNome() + ".",
                    null, StatusOS.ABERTA);

            System.out.println(">> OS aberta com sucesso. Número: " + os.getIdOrdemServico());
        } catch (Exception e) { System.out.println(">> " + e.getMessage()); }
    }

    private void listarEquipamentosAtivos() {
        List<Equipamento> equipamentos = equipamentoDao.listarEquipamentosAtivos();
        System.out.printf("%-5s %-18s %-30s %-25s%n", "ID","PATRIMÔNIO","NOME","LOCALIZAÇÃO");
        for (Equipamento e : equipamentos)
            System.out.printf("%-5d %-18s %-30s %-25s%n", e.getIdEquipamento(), e.getCodigoPatrimonio(), e.getNome(), e.getLocalizacao());
    }

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

    private void visualizar() {
        int id = lerId("ID da OS: ");
        OrdemServico os = osDao.buscarOrdemServicoPorId(id);
        if (os == null) { System.out.println(">> OS não encontrada."); return; }
        System.out.println("\n===== DETALHES DA OS #" + os.getIdOrdemServico() + " =====");
        System.out.println("Equipamento: " + os.getEquipamentoId());
        System.out.println("Técnico: " + (os.getTecnicoId() == null ? "Não atribuído" : os.getTecnicoId()));
        System.out.println("Usuário de abertura: " + os.getGestorAberturaId());
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
                    h.getDescricaoAcao(), h.getStatusAnterior(), h.getStatusNovo());
    }

    private void filtrar() {
        System.out.println("1-ABERTA 2-EM_EXECUCAO 3-AGUARDANDO_PECA 4-REPARO_FINALIZADO 5-CONCLUIDA 6-CANCELADA");
        StatusOS status = statusPorNumero(scanner.nextLine().trim());
        listar(osDao.listarPorStatus(status));
    }

    private void minhasOS() {
        int id = SessaoAtual.getUsuarioLogado().getIdUsuario();
        if (SessaoAtual.isTecnico()) listar(osDao.listarOrdensServicoPorTecnico(id));
        else listar(osDao.listarOrdensServico());
    }

    private void atribuir() {
        try {
            int idOs = lerId("ID da OS: ");
            OrdemServico os = exigirOS(idOs);
            if (os.getStatus() == StatusOS.CONCLUIDA || os.getStatus() == StatusOS.CANCELADA)
                throw new IllegalStateException("Não é possível atribuir uma OS encerrada/cancelada.");

            System.out.println("--- Técnicos disponíveis ---");
            for (Usuario t : usuarioService.listarTecnicosDisponiveis())
                System.out.printf("%d - %s (%s)%n", t.getIdUsuario(), t.getNome(), t.getStatus());
            System.out.print("ID do técnico: ");
            int tecnicoId = Integer.parseInt(scanner.nextLine().trim());
            Usuario tecnico = usuarioService.buscarPorId(tecnicoId);
            if (tecnico.getPerfil() != PerfilUsuario.TECNICO || !tecnico.isAtivo())
                throw new IllegalArgumentException("Usuário não é um técnico ativo.");
            osDao.atribuirTecnico(idOs, tecnicoId);
            usuarioService.alterarDisponibilidade(tecnicoId, com.sistema.manutencao.model.enums.StatusUsuario.EM_ATENDIMENTO);
            statusService.registrarHistorico(idOs, SessaoAtual.getUsuarioLogado().getIdUsuario(),
                    "OS atribuída ao técnico " + tecnico.getNome() + ".",
                    os.getStatus(), os.getStatus());
            System.out.println(">> Técnico atribuído.");
        } catch (Exception e) { System.out.println(">> " + e.getMessage()); }
    }

    private void diagnostico() {
        OrdemServico os = exigirOS(lerId("ID da OS: "));
        validarTecnicoDaOS(os);
        if (os.getStatus() == StatusOS.CONCLUIDA || os.getStatus() == StatusOS.CANCELADA)
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

    private void apontarReparo() {
        OrdemServico os = exigirOS(lerId("ID da OS: "));
        validarTecnicoDaOS(os);
        if (os.getStatus() == StatusOS.CONCLUIDA || os.getStatus() == StatusOS.CANCELADA)
            throw new IllegalStateException("OS encerrada/cancelada.");
        System.out.print("Horas trabalhadas (ex.: 2.50): ");
        BigDecimal horas = new BigDecimal(scanner.nextLine().trim().replace(",", "."));
        if (horas.signum() < 0) throw new IllegalArgumentException("Horas não podem ser negativas.");
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

    private void statusTecnico() {
        OrdemServico os = exigirOS(lerId("ID da OS: "));
        validarTecnicoDaOS(os);
        System.out.println("1-EM_EXECUCAO 2-AGUARDANDO_PECA 3-REPARO_FINALIZADO");
        StatusOS novo = switch (scanner.nextLine().trim()) {
            case "1" -> StatusOS.EM_EXECUCAO;
            case "2" -> StatusOS.AGUARDANDO_PECA;
            case "3" -> StatusOS.REPARO_FINALIZADO;
            default -> throw new IllegalArgumentException("Status inválido.");
        };
        String desc;
        System.out.print("Observação (opcional): "); desc = scanner.nextLine().trim();
        statusService.alterarStatus(os.getIdOrdemServico(), novo,
                SessaoAtual.getUsuarioLogado().getIdUsuario(), desc);
        System.out.println(">> Status atualizado.");
    }

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

    private void encerrar() {
        OrdemServico os = exigirOS(lerId("ID da OS: "));
        if (os.getStatus() != StatusOS.REPARO_FINALIZADO)
            throw new IllegalStateException("Apenas OS em REPARO_FINALIZADO podem ser encerradas administrativamente.");
        System.out.print("Validar reparo e fechar OS? (S/N): ");
        if (!"S".equalsIgnoreCase(scanner.nextLine().trim())) return;
        statusService.alterarStatus(os.getIdOrdemServico(), StatusOS.CONCLUIDA,
                SessaoAtual.getUsuarioLogado().getIdUsuario(),
                "Reparo validado e OS encerrada administrativamente pelo gestor.");
        if (os.getTecnicoId() != null) {
            try { usuarioService.alterarDisponibilidade(os.getTecnicoId(), com.sistema.manutencao.model.enums.StatusUsuario.DISPONIVEL); }
            catch (Exception ignored) {}
        }
        System.out.println(">> OS encerrada.");
    }

    private void cancelar() {
        OrdemServico os = exigirOS(lerId("ID da OS: "));
        if (os.getStatus() == StatusOS.CONCLUIDA || os.getStatus() == StatusOS.CANCELADA)
            throw new IllegalStateException("OS já encerrada/cancelada.");
        System.out.print("Motivo do cancelamento: ");
        String motivo = scanner.nextLine().trim();
        if (motivo.isBlank()) throw new IllegalArgumentException("Informe o motivo.");
        statusService.alterarStatus(os.getIdOrdemServico(), StatusOS.CANCELADA,
                SessaoAtual.getUsuarioLogado().getIdUsuario(), "OS cancelada: " + motivo);
        if (os.getTecnicoId() != null) {
            try { usuarioService.alterarDisponibilidade(os.getTecnicoId(), com.sistema.manutencao.model.enums.StatusUsuario.DISPONIVEL); }
            catch (Exception ignored) {}
        }
        System.out.println(">> OS cancelada.");
    }

    private void validarTecnicoDaOS(OrdemServico os) {
        if (os.getTecnicoId() == null || os.getTecnicoId() != SessaoAtual.getUsuarioLogado().getIdUsuario())
            throw new IllegalStateException("Esta OS não está atribuída ao técnico logado.");
    }

    private OrdemServico exigirOS(int id) {
        OrdemServico os = osDao.buscarOrdemServicoPorId(id);
        if (os == null) throw new IllegalArgumentException("OS não encontrada.");
        return os;
    }

    private CriticidadeOS lerCriticidade() {
        System.out.println("1-BAIXA 2-MEDIA 3-ALTA 4-CRITICA");
        return switch (scanner.nextLine().trim()) {
            case "1" -> CriticidadeOS.BAIXA;
            case "2" -> CriticidadeOS.MEDIA;
            case "3" -> CriticidadeOS.ALTA;
            case "4" -> CriticidadeOS.CRITICA;
            default -> throw new IllegalArgumentException("Criticidade inválida.");
        };
    }

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

    private int lerId(String prompt) {
        System.out.print(prompt);
        return Integer.parseInt(scanner.nextLine().trim());
    }

    private String valor(String s) { return s == null || s.isBlank() ? "-" : s; }
    private String format(java.time.LocalDateTime dt) { return dt == null ? "-" : dt.format(FMT); }
}
