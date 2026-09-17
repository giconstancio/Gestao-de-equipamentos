package com.sistema.manutencao.dao;

import com.sistema.manutencao.config.DatabaseConnection;
import com.sistema.manutencao.model.Historico;
import com.sistema.manutencao.model.enums.StatusOS;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*
 Classe DAO responsável pelo acesso aos dados do histórico
 de intervenções realizadas nas Ordens de Serviço.
*/
public class HistoricoDAO {

    // Registra uma nova intervenção no histórico da Ordem de Serviço.
    public Historico registrar(Historico historico) {
        String sql = "INSERT INTO historico_intervencoes " +
                "(ordem_servico_id, usuario_id, descricao_acao, status_anterior, status_novo) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, historico.getOrdemServicoId());
            stmt.setInt(2, historico.getUsuarioId());
            stmt.setString(3, historico.getDescricaoAcao());
            stmt.setString(4, historico.getStatusAnterior().name());
            stmt.setString(5, historico.getStatusNovo().name());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    historico.setIdHistorico(rs.getInt(1));
                }
            }

            return historico;

        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao registrar histórico da ordem de serviço: " + ex);
        }
    }

    // Lista todas as intervenções registradas para uma determinada Ordem de Serviço.
    public List<Historico> listarPorOrdemServico(int idOrdemServico) {
        String sql = "SELECT * FROM historico_intervencoes " +
                "WHERE ordem_servico_id = ? " +
                "ORDER BY registrado_em ASC, id ASC";

        List<Historico> historicos = new ArrayList<>();

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idOrdemServico);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    historicos.add(mapRow(rs));
                }
            }

            return historicos;

        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao listar histórico da ordem de serviço: " + ex);
        }
    }

    // Converte uma linha retornada pelo banco de dados em um objeto Historico.
    private Historico mapRow(ResultSet rs) throws SQLException {
        Historico historico = new Historico();

        historico.setIdHistorico(rs.getInt("id"));
        historico.setOrdemServicoId(rs.getInt("ordem_servico_id"));
        historico.setUsuarioId(rs.getInt("usuario_id"));
        historico.setDescricaoAcao(rs.getString("descricao_acao"));

        String statusAnterior = rs.getString("status_anterior");
        if (statusAnterior != null) {
            historico.setStatusAnterior(StatusOS.valueOf(statusAnterior));
        }

        String statusNovo = rs.getString("status_novo");
        if (statusNovo != null) {
            historico.setStatusNovo(StatusOS.valueOf(statusNovo));
        }

        Timestamp registradoEm = rs.getTimestamp("registrado_em");
        if (registradoEm != null) {
            historico.setRegistradoEm(registradoEm.toLocalDateTime());
        }

        return historico;
    }
}