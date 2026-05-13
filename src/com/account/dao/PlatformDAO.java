package com.account.dao;

import com.account.model.Platform;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 平台类型字典数据访问对象，提供对 platforms 表的 CRUD 操作。
 * <p>
 * platforms 表作为字典表，存储账号可选的平台分类（如"主域名管理"、"公司邮箱"等）。
 * 初始数据在 {@link DBUtil#initDatabase()} 中预置。
 */
public class PlatformDAO {
    private DBUtil dbUtil = DBUtil.getInstance();

    /**
     * 查询所有平台类型。
     *
     * @return 平台类型列表，按 ID 排序
     */
    public List<Platform> findAll() {
        List<Platform> list = new ArrayList<>();
        String sql = "SELECT * FROM platforms ORDER BY id";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapPlatform(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all platforms", e);
        }
        return list;
    }

    public void insert(Platform platform) {
        String sql = "INSERT INTO platforms (name) VALUES (?)";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, platform.getName());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert platform", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM platforms WHERE id=?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete platform", e);
        }
    }

    /**
     * 统计指定平台类型下有多少个账号。
     *
     * @param platformName 平台类型名称
     * @return 该平台下的账号数量
     */
    public int countAccountsByPlatform(String platformName) {
        String sql = "SELECT COUNT(*) FROM accounts WHERE platform_type=?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, platformName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count accounts by platform", e);
        }
    }

    private Platform mapPlatform(ResultSet rs) throws SQLException {
        Platform p = new Platform();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setCreatedAt(rs.getString("created_at"));
        return p;
    }
}
