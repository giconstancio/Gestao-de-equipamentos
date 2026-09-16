package com.sistema.manutencao.model;

import com.sistema.manutencao.model.enums.StatusOS;

import java.time.LocalDateTime;

/*
 Classe que representa um registro do histórico de intervenções
 realizadas em uma Ordem de Serviço.
*/
public class Historico {

    private int idHistorico;
    private int ordemServicoId;
    private int usuarioId;
    private String descricaoAcao;
    private StatusOS statusAnterior;
    private StatusOS statusNovo;
    private LocalDateTime registradoEm;

    // Retorna o identificador do registro de histórico.
    public int getIdHistorico() {
        return idHistorico;
    }

    // Define o identificador do registro de histórico.
    public void setIdHistorico(int idHistorico) {
        this.idHistorico = idHistorico;
    }

    // Retorna o identificador da Ordem de Serviço relacionada ao histórico.
    public int getOrdemServicoId() {
        return ordemServicoId;
    }

    // Define a Ordem de Serviço relacionada ao histórico.
    public void setOrdemServicoId(int ordemServicoId) {
        this.ordemServicoId = ordemServicoId;
    }

    // Retorna o identificador do usuário responsável pela intervenção.
    public int getUsuarioId() {
        return usuarioId;
    }

    // Define o usuário responsável pela intervenção.
    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    // Retorna a descrição da ação realizada na Ordem de Serviço.
    public String getDescricaoAcao() {
        return descricaoAcao;
    }

    // Define a descrição da ação realizada na Ordem de Serviço.
    public void setDescricaoAcao(String descricaoAcao) {
        this.descricaoAcao = descricaoAcao;
    }

    // Retorna o status que a Ordem de Serviço possuía antes da alteração.
    public StatusOS getStatusAnterior() {
        return statusAnterior;
    }

    // Define o status anterior da Ordem de Serviço.
    public void setStatusAnterior(StatusOS statusAnterior) {
        this.statusAnterior = statusAnterior;
    }

    // Retorna o novo status atribuído à Ordem de Serviço.
    public StatusOS getStatusNovo() {
        return statusNovo;
    }

    // Define o novo status da Ordem de Serviço.
    public void setStatusNovo(StatusOS statusNovo) {
        this.statusNovo = statusNovo;
    }

    // Retorna a data e a hora em que a intervenção foi registrada.
    public LocalDateTime getRegistradoEm() {
        return registradoEm;
    }

    // Define a data e a hora em que a intervenção foi registrada.
    public void setRegistradoEm(LocalDateTime registradoEm) {
        this.registradoEm = registradoEm;
    }
}