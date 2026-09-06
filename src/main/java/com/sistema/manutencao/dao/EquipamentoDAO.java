package com.sistema.manutencao.dao;

import com.sistema.manutencao.config.DatabaseConnection;
import com.sistema.manutencao.model.Equipamento;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipamentoDAO {

    // insere um equipamento novo no banco e já devolve o objeto com o id que o banco gerou
    public Equipamento cadastrarEquipamento(Equipamento e) {
        String sql = "INSERT INTO equipamentos(codigo_patrimonio, nome, descricao, localizacao) VALUES (?,?,?,?)";

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, e.getCodigoPatrimonio());
            stmt.setString(2, e.getNome());
            stmt.setString(3, e.getDescricao());
            stmt.setString(4, e.getLocalizacao());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    e.setIdEquipamento(rs.getInt(1));
                }
            } return e;
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao cadastrar equipamento: " + ex);
        }
    }

    // pega todos os equipamentos cadastrados, ordenados por nome
    public List<Equipamento> listarEquipamentos() {
        String sql = "SELECT * FROM equipamentos ORDER BY nome";

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<Equipamento> equipamentos = new ArrayList<>();
            while (rs.next()) {
                equipamentos.add(mapRow(rs));
            }
            return equipamentos;
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao listar equipamentos: " + ex);
        }
    }

    // mesma coisa da listagem normal, mas só traz os que estão ativos
    public List<Equipamento> listarEquipamentosAtivos() {
        String sql = "SELECT * FROM equipamentos WHERE ativo = TRUE ORDER BY nome";

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<Equipamento> equipamentos = new ArrayList<>();
            while (rs.next()) {
                equipamentos.add(mapRow(rs));
            }
            return equipamentos;
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao listar equipamentos ativos: " + ex);
        }
    }

    // busca um equipamento específico pelo id, usado na tela de visualização
    public Equipamento buscarEquipamentoPorId(int idEquipamento) {
        String sql = "SELECT * FROM equipamentos WHERE id = ?";

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEquipamento);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao buscar equipamento por ID: " + ex);
        }
    }

    // busca pelo código de patrimônio, uso principalmente pra checar se já existe antes de cadastrar
    public Equipamento buscarEquipamentoPorCodigo(String codigoPatrimonio) {
        String sql = "SELECT * FROM equipamentos WHERE codigo_patrimonio = ?";

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codigoPatrimonio);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao buscar equipamento por código de patrimônio: " + ex);
        }
    }

    // atualiza os dados de um equipamento que já existe no banco
    public Equipamento editarEquipamento(Equipamento e) {
        String sql = "UPDATE equipamentos SET codigo_patrimonio=?, nome=?, descricao=?, localizacao=?, ativo=? WHERE id = ?";

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, e.getCodigoPatrimonio());
            stmt.setString(2, e.getNome());
            stmt.setString(3, e.getDescricao());
            stmt.setString(4, e.getLocalizacao());
            stmt.setBoolean(5, e.isAtivo());
            stmt.setInt(6, e.getIdEquipamento());
            stmt.executeUpdate();
            return e;
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao editar equipamento: " + ex);
        }
    }

    // não apaga o registro, só marca como inativo (soft delete)
    public void inativarEquipamento(int idEquipamento) {
        String sql = "UPDATE equipamentos SET ativo=false WHERE id = ?";

        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEquipamento);
            stmt.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao inativar equipamento: " + ex);
        }
    }

    // método auxiliar pra não ficar repetindo esse bloco de conversão em todo lugar que usa ResultSet
    private Equipamento mapRow(ResultSet rs) throws SQLException {
        Equipamento e = new Equipamento();
        e.setIdEquipamento(rs.getInt("id"));
        e.setCodigoPatrimonio(rs.getString("codigo_patrimonio"));
        e.setNome(rs.getString("nome"));
        e.setDescricao(rs.getString("descricao"));
        e.setLocalizacao(rs.getString("localizacao"));
        e.setAtivo(rs.getBoolean("ativo"));
        e.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
        return e;
    }
}