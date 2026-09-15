package com.sistema.manutencao.model;

import com.sistema.manutencao.model.enums.CriticidadeOS;
import com.sistema.manutencao.model.enums.StatusOS;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Classe Model de Ordem de Serviço
public class OrdemServico {
    private int idOrdemServico;
    private int equipamentoId;
    private Integer tecnicoId; // Integer (e não int) porque pode ficar nulo enquanto a OS não é atribuída a um técnico
    private int gestorAberturaId;
    private String descricaoFalha;
    private CriticidadeOS criticidade;
    private StatusOS status;
    private String diagnosticoInicial;
    private String causaRaiz;
    private BigDecimal horasTrabalhadas;
    private String pecasUtilizadas;
    private LocalDateTime abertoEm;
    private LocalDateTime concluidoEm;

    public int getIdOrdemServico() {
        return idOrdemServico;
    }

    public void setIdOrdemServico(int idOrdemServico) {
        this.idOrdemServico = idOrdemServico;
    }

    public int getEquipamentoId() {
        return equipamentoId;
    }

    public void setEquipamentoId(int equipamentoId) {
        this.equipamentoId = equipamentoId;
    }

    public Integer getTecnicoId() {
        return tecnicoId;
    }

    public void setTecnicoId(Integer tecnicoId) {
        this.tecnicoId = tecnicoId;
    }

    public int getGestorAberturaId() {
        return gestorAberturaId;
    }

    public void setGestorAberturaId(int gestorAberturaId) {
        this.gestorAberturaId = gestorAberturaId;
    }

    public String getDescricaoFalha() {
        return descricaoFalha;
    }

    public void setDescricaoFalha(String descricaoFalha) {
        this.descricaoFalha = descricaoFalha;
    }

    public CriticidadeOS getCriticidade() {
        return criticidade;
    }

    public void setCriticidade(CriticidadeOS criticidade) {
        this.criticidade = criticidade;
    }

    public StatusOS getStatus() {
        return status;
    }

    public void setStatus(StatusOS status) {
        this.status = status;
    }

    public String getDiagnosticoInicial() {
        return diagnosticoInicial;
    }

    public void setDiagnosticoInicial(String diagnosticoInicial) {
        this.diagnosticoInicial = diagnosticoInicial;
    }

    public String getCausaRaiz() {
        return causaRaiz;
    }

    public void setCausaRaiz(String causaRaiz) {
        this.causaRaiz = causaRaiz;
    }

    public BigDecimal getHorasTrabalhadas() {
        return horasTrabalhadas;
    }

    public void setHorasTrabalhadas(BigDecimal horasTrabalhadas) {
        this.horasTrabalhadas = horasTrabalhadas;
    }

    public String getPecasUtilizadas() {
        return pecasUtilizadas;
    }

    public void setPecasUtilizadas(String pecasUtilizadas) {
        this.pecasUtilizadas = pecasUtilizadas;
    }

    public LocalDateTime getAbertoEm() {
        return abertoEm;
    }

    public void setAbertoEm(LocalDateTime abertoEm) {
        this.abertoEm = abertoEm;
    }

    public LocalDateTime getConcluidoEm() {
        return concluidoEm;
    }

    public void setConcluidoEm(LocalDateTime concluidoEm) {
        this.concluidoEm = concluidoEm;
    }
}