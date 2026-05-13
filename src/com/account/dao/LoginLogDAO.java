package com.account.dao;

import com.account.model.LoginLog;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 登录日志数据访问对象，提供对 login_logs 表的插入和查询操作。
 * <p>
 * 记录每次用户登录的详细信息（用户 ID、用户名、显示名、IP 地址、登录时间）。
 * 支持按关键词（用户名/显示名）和日期范围进行分页搜索。
 */
public class LoginLogDAO {
    private DBUtil dbUtil = DBUtil.getInstance();

    /**
     * 插入一条登录日志。
     * <p>
     * SQL: INSERT INTO login_logs (user_id, username, display_name, ip_address) VALUES (?, ?, ?, ?)
     *
     * @param log 登录日志对象
     */
    public void insert(LoginLog log) {
        String sql = "INSERT INTO login_logs (user_id, username, display_name, ip_address) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, log.getUserId());
            ps.setString(2, log.getUsername());
            ps.setString(3, log.getDisplayName());
            ps.setString(4, log.getIpAddress());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert login log", e);
        }
    }

    /**
     * 统计符合条件的登录日志总数。
     *
     * @param keyword  关键词模糊搜索（按用户名/显示名）
     * @param dateFrom 起始日期（yyyy-MM-dd）
     * @param dateTo   结束日期（yyyy-MM-dd）
     * @return 符合条件的日志总数
     */
    public int count(String keyword, String dateFrom, String dateTo) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM login_logs WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, keyword, dateFrom, dateTo);
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count login logs", e);
        }
    }

    /**
     * 分页搜索登录日志，按登录时间倒序排列。
     *
     * @param keyword  关键词模糊搜索
     * @param dateFrom 起始日期
     * @param dateTo   结束日期
     * @param page     页码（从 1 开始）
     * @param pageSize 每页条数
     * @return 日志分页列表
     */
    public List<LoginLog> search(String keyword, String dateFrom, String dateTo, int page, int pageSize) {
        List<LoginLog> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM login_logs WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, keyword, dateFrom, dateTo);
        sql.append(" ORDER BY login_time DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapLog(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search login logs", e);
        }
        return list;
    }

    private void appendFilters(StringBuilder sql, List<Object> params, String keyword, String dateFrom, String dateTo) {
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (username LIKE ? OR display_name LIKE ?)");
            String kw = "%" + keyword + "%";
            params.add(kw);
            params.add(kw);
        }
        if (dateFrom != null && !dateFrom.isEmpty()) {
            sql.append(" AND login_time >= ?");
            params.add(dateFrom + " 00:00:00");
        }
        if (dateTo != null && !dateTo.isEmpty()) {
            sql.append(" AND login_time <= ?");
            params.add(dateTo + " 23:59:59");
        }
    }

    private LoginLog mapLog(ResultSet rs) throws SQLException {
        LoginLog log = new LoginLog();
        log.setId(rs.getInt("id"));
        log.setUserId(rs.getInt("user_id"));
        log.setUsername(rs.getString("username"));
        log.setDisplayName(rs.getString("display_name"));
        log.setIpAddress(rs.getString("ip_address"));
        log.setLoginTime(rs.getString("login_time"));
        return log;
    }
}
