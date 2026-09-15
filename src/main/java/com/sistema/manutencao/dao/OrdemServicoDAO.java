package com.sistema.manutencao.dao;

import com.sistema.manutencao.config.DatabaseConnection;
import com.sistema.manutencao.model.OrdemServico;
import com.sistema.manutencao.model.enums.CriticidadeOS;
import com.sistema.manutencao.model.enums.StatusOS;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/* Classe DAO de Ordem de Serviço
Responsável por realizar ações relacionadas às ordens de serviço dentro do banco de dados, sendo a camada
intermediária entre o banco e a classe Service
*/
public class OrdemServicoDAO {

    // Método de abertura de uma nova Ordem de Serviço no banco de dados
    // O status inicial já é definido como ABERTA por padrão no próprio banco, então não precisa ser informado aqui
    public OrdemServico abrirOrdemServico(OrdemServico os) {
        String sql = "INSERT INTO ordens_servico(equipamento_id, gestor_abertura_id, descricao_falha, criticidade) VALUES (?,?,?,?)";

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, os.getEquipamentoId());
            stmt.setInt(2, os.getGestorAberturaId());
            stmt.setString(3, os.getDescricaoFalha());
            stmt.setString(4, os.getCriticidade().name());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    os.setIdOrdemServico(rs.getInt(1));
                }
            } return os; // Retorna a Ordem de Serviço cadastrada já com o ID atribuído
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao abrir ordem de serviço: " + ex);
        }
    }

    // Método responsável por atribuir (ou reatribuir) um técnico a uma Ordem de Serviço já existente
    public void atribuirTecnico(int idOrdemServico, int idTecnico) {
        String sql = "UPDATE ordens_servico SET tecnico_id = ? WHERE id = ?";

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idTecnico);
            stmt.setInt(2, idOrdemServico);
            stmt.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao atribuir técnico à ordem de serviço: " + ex);
        }
    }

    // Método responsável por listar todas as Ordens de Serviço do sistema
    // Retorna uma lista com os objetos das Ordens de Serviço completas
    public List<OrdemServico> listarOrdensServico() {
        // Ordenadas das mais recentes para as mais antigas
        String sql = "SELECT * FROM ordens_servico ORDER BY aberto_em DESC";

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<OrdemServico> ordens = new ArrayList<>();
            while (rs.next()) {
                ordens.add(mapRow(rs)); // Adiciona cada uma na lista que será retornada (com auxílio do método de conversão)
            }
            return ordens;
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao listar ordens de serviço: " + ex);
        }
    }

    // Método responsável por listar todas as Ordens de Serviço atribuídas a um técnico específico
    public List<OrdemServico> listarOrdensServicoPorTecnico(int idTecnico) {
        String sql = "SELECT * FROM ordens_servico WHERE tecnico_id = ? ORDER BY aberto_em DESC";

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idTecnico);
            try (ResultSet rs = stmt.executeQuery()) {
                List<OrdemServico> ordens = new ArrayList<>();
                while (rs.next()) {
                    ordens.add(mapRow(rs));
                }
                return ordens;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao listar ordens de serviço do técnico: " + ex);
        }
    }

    // Método responsável por retornar o objeto completo da Ordem de Serviço com base no ID informado pelo parâmetro
    public OrdemServico buscarOrdemServicoPorId(int idOrdemServico) {
        String sql = "SELECT * FROM ordens_servico WHERE id = ?";

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idOrdemServico);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao buscar ordem de serviço por ID: " + ex);
        }
    }

    // Método interno auxiliar para criação e mapeamento dos dados da Ordem de Serviço retornada por um ResultSet, ou
    // seja, um objeto OrdemServico com os valores obtidos pelo ResultSet
    private OrdemServico mapRow(ResultSet rs) throws SQLException {
        OrdemServico os = new OrdemServico();
        os.setIdOrdemServico(rs.getInt("id"));
        os.setEquipamentoId(rs.getInt("equipamento_id"));

        int tecnicoId = rs.getInt("tecnico_id");
        os.setTecnicoId(rs.wasNull() ? null : tecnicoId); // tecnico_id pode ser nulo enquanto a OS não é atribuída

        os.setGestorAberturaId(rs.getInt("gestor_abertura_id"));
        os.setDescricaoFalha(rs.getString("descricao_falha"));
        os.setCriticidade(CriticidadeOS.valueOf(rs.getString("criticidade")));
        os.setStatus(StatusOS.valueOf(rs.getString("status")));
        os.setDiagnosticoInicial(rs.getString("diagnostico_inicial"));
        os.setCausaRaiz(rs.getString("causa_raiz"));
        os.setHorasTrabalhadas(rs.getBigDecimal("horas_trabalhadas"));
        os.setPecasUtilizadas(rs.getString("pecas_utilizadas"));
        os.setAbertoEm(rs.getTimestamp("aberto_em").toLocalDateTime());

        Timestamp concluidoEm = rs.getTimestamp("concluido_em");
        os.setConcluidoEm(concluidoEm != null ? concluidoEm.toLocalDateTime() : null);

        return os;
    }
}