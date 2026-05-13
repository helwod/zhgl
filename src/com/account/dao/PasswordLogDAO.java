package com.account.dao;

import com.account.model.PasswordLog;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 密码查看/导出审计日志数据访问对象，提供对 password_logs 表的插入和查询操作。
 * <p>
 * 记录用户查看或导出账号密码的行为（含操作类型：view/export），用于安全审计。
 * 支持按关键词、日期范围分页搜索，并可限定只查询当前用户受管账号的相关日志。
 */
public class PasswordLogDAO {
    private DBUtil dbUtil = DBUtil.getInstance();

    /**
     * 插入一条密码查看/导出日志。
     * <p>
     * SQL: INSERT INTO password_logs (account_id, viewer_id, viewer_name, action_type) VALUES (?, ?, ?, ?)
     *
     * @param log 密码日志对象
     */
    public void insert(PasswordLog log) {
        String sql = "INSERT INTO password_logs (account_id, viewer_id, viewer_name, action_type) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, log.getAccountId());
            ps.setInt(2, log.getViewerId());
            ps.setString(3, log.getViewerName());
            ps.setString(4, log.getActionType() != null ? log.getActionType() : "view");
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert password log", e);
        }
    }


    /**
     * 统计符合条件的密码日志总数（不限用户权限）。
     *
     * @param keyword  关键词模糊搜索
     * @param dateFrom 起始日期
     * @param dateTo   结束日期
     * @return 符合条件的日志总数
     */
    public int count(String keyword, String dateFrom, String dateTo) {
        return count(keyword, dateFrom, dateTo, 0);
    }

    /**
     * 统计符合条件的密码日志总数（可限定用户权限）。
     *
     * @param keyword  关键词模糊搜索
     * @param dateFrom 起始日期
     * @param dateTo   结束日期
     * @param userId   用户 ID（>0 时只统计该用户受管账号的日志）
     * @return 符合条件的日志总数
     */
    public int count(String keyword, String dateFrom, String dateTo, int userId) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM password_logs pl LEFT JOIN accounts ac ON pl.account_id = ac.id WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendManagedFilter(sql, params, userId);
        appendFilters(sql, params, keyword, dateFrom, dateTo);
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count password logs", e);
        }
    }

    /**
     * 分页搜索密码日志（不限用户权限），结果含账号信息。
     *
     * @param keyword  关键词模糊搜索
     * @param dateFrom 起始日期
     * @param dateTo   结束日期
     * @param page     页码（从 1 开始）
     * @param pageSize 每页条数
     * @return 日志分页列表
     */
    public List<PasswordLog> search(String keyword, String dateFrom, String dateTo, int page, int pageSize) {
        return search(keyword, dateFrom, dateTo, page, pageSize, 0);
    }

    public List<PasswordLog> search(String keyword, String dateFrom, String dateTo, int page, int pageSize, int userId) {
        List<PasswordLog> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT pl.*, ac.name AS account_name, ac.project, ac.platform_type, ac.sub_type, ac.login_url FROM password_logs pl " +
            "LEFT JOIN accounts ac ON pl.account_id = ac.id WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendManagedFilter(sql, params, userId);
        appendFilters(sql, params, keyword, dateFrom, dateTo);
        sql.append(" ORDER BY pl.viewed_at DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapLog(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search password logs", e);
        }
        return list;
    }

    private void appendManagedFilter(StringBuilder sql, List<Object> params, int userId) {
        if (userId > 0) {
            sql.append(" AND pl.account_id IN (");
            sql.append("  SELECT am.account_id FROM account_managers am WHERE am.user_id=?");
            sql.append("  UNION");
            sql.append("  SELECT agm.account_id FROM account_group_managers agm");
            sql.append("  INNER JOIN user_group_members ugm ON agm.group_id = ugm.group_id");
            sql.append("  WHERE ugm.user_id=?");
            sql.append(") ");
            params.add(userId);
            params.add(userId);
        }
    }

    private void appendFilters(StringBuilder sql, List<Object> params, String keyword, String dateFrom, String dateTo) {
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (ac.name LIKE ? OR ac.project LIKE ? OR ac.platform_type LIKE ? OR ac.sub_type LIKE ? OR pl.viewer_name LIKE ?)");
            String kw = "%" + keyword + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (dateFrom != null && !dateFrom.isEmpty()) {
            sql.append(" AND pl.viewed_at >= ?");
            params.add(dateFrom + " 00:00:00");
        }
        if (dateTo != null && !dateTo.isEmpty()) {
            sql.append(" AND pl.viewed_at <= ?");
            params.add(dateTo + " 23:59:59");
        }
    }

    public List<PasswordLog> findAll() {
        List<PasswordLog> list = new ArrayList<>();
        String sql = "SELECT pl.*, ac.name AS account_name, ac.project, ac.platform_type, ac.sub_type, ac.login_url " +
                     "FROM password_logs pl " +
                     "LEFT JOIN accounts ac ON pl.account_id = ac.id " +
                     "ORDER BY pl.viewed_at DESC";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapLog(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all password logs", e);
        }
        return list;
    }

    public List<PasswordLog> findByViewer(int viewerId) {
        List<PasswordLog> list = new ArrayList<>();
        String sql = "SELECT pl.*, ac.name AS account_name, ac.project, ac.platform_type, ac.sub_type, ac.login_url " +
                     "FROM password_logs pl " +
                     "LEFT JOIN accounts ac ON pl.account_id = ac.id " +
                     "WHERE pl.viewer_id = ? ORDER BY pl.viewed_at DESC";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, viewerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapLog(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find password logs by viewer", e);
        }
        return list;
    }

    /**
     * 统计密码日志总条数。
     *
     * @return 日志总数
     */
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM password_logs";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count all password logs", e);
        }
    }

    private PasswordLog mapLog(ResultSet rs) throws SQLException {
        PasswordLog log = new PasswordLog();
        log.setId(rs.getInt("id"));
        log.setAccountId(rs.getInt("account_id"));
        log.setViewerId(rs.getInt("viewer_id"));
        log.setViewerName(rs.getString("viewer_name"));
        log.setViewedAt(rs.getString("viewed_at"));
        try { log.setActionType(rs.getString("action_type")); } catch (SQLException ignored) {}
        try { log.setAccountName(rs.getString("account_name")); } catch (SQLException ignored) {}
        try { log.setProject(rs.getString("project")); } catch (SQLException ignored) {}
        try { log.setPlatformType(rs.getString("platform_type")); } catch (SQLException ignored) {}
        try { log.setSubType(rs.getString("sub_type")); } catch (SQLException ignored) {}
        try { log.setLoginUrl(rs.getString("login_url")); } catch (SQLException ignored) {}
        return log;
    }
}
