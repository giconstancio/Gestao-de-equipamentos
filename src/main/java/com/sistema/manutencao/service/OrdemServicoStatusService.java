package com.sistema.manutencao.service;

import com.sistema.manutencao.dao.HistoricoDAO;
import com.sistema.manutencao.dao.OrdemServicoDAO;
import com.sistema.manutencao.model.Historico;
import com.sistema.manutencao.model.OrdemServico;
import com.sistema.manutencao.model.enums.StatusOS;

/*
 Classe de serviço responsável pela lógica de alteração
 de status das Ordens de Serviço e pelo registro automático
 dessas alterações no histórico.
*/
public class OrdemServicoStatusService {

    private final OrdemServicoDAO ordemServicoDAO;
    private final HistoricoDAO historicoDAO;

    // Inicializa os DAOs utilizados para alteração da OS e registro do histórico.
    public OrdemServicoStatusService() {
        this.ordemServicoDAO = new OrdemServicoDAO();
        this.historicoDAO = new HistoricoDAO();
    }

    // Altera o status de uma Ordem de Serviço e registra automaticamente a alteração no histórico.
    public void alterarStatus(int idOrdemServico, StatusOS novoStatus,
                              int idUsuario, String descricaoAcao) {

        // Verifica se um novo status foi informado.
        if (novoStatus == null) {
            throw new IllegalArgumentException(
                    "O novo status da ordem de serviço deve ser informado.");
        }

        // Busca a OS para descobrir qual era o status antes da alteração.
        OrdemServico ordemServico =
                ordemServicoDAO.buscarOrdemServicoPorId(idOrdemServico);

        // Impede a alteração caso a OS informada não exista.
        if (ordemServico == null) {
            throw new IllegalArgumentException(
                    "Ordem de serviço não encontrada para o ID informado.");
        }

        StatusOS statusAnterior = ordemServico.getStatus();

        // Evita registrar uma alteração quando o status continua igual.
        if (statusAnterior == novoStatus) {
            throw new IllegalArgumentException(
                    "O novo status deve ser diferente do status atual.");
        }

        String descricao = descricaoAcao;

        // Gera uma descrição automática caso nenhuma seja informada.
        if (descricao == null || descricao.isBlank()) {
            descricao = "Status alterado de " +
                    statusAnterior + " para " + novoStatus + ".";
        }

        // Atualiza o status da Ordem de Serviço no banco.
        ordemServicoDAO.atualizarStatus(idOrdemServico, novoStatus);

        // Monta o registro que será armazenado no histórico.
        Historico historico = new Historico();
        historico.setOrdemServicoId(idOrdemServico);
        historico.setUsuarioId(idUsuario);
        historico.setDescricaoAcao(descricao);
        historico.setStatusAnterior(statusAnterior);
        historico.setStatusNovo(novoStatus);

        // Registra automaticamente a alteração realizada.
        historicoDAO.registrar(historico);
    }
}