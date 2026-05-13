package com.account.dao;

import com.account.model.AccountLog;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 账号变更日志数据访问对象，提供对 account_logs 表的插入和查询操作。
 * <p>
 * 记录账号的创建、更新、删除等操作详情（含字段级别的旧值/新值对比）。
 * 支持按操作类型、关键词、日期范围进行分页搜索，并可限定只查询
 * 当前用户（个人或组级别）受管账号的相关日志。
 */
public class AccountLogDAO {
    private DBUtil dbUtil = DBUtil.getInstance();

    public void insert(AccountLog log) {
        String sql = "INSERT INTO account_logs (account_id, action_type, field_name, old_value, new_value, " +
                     "operator_id, operator_name) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, log.getAccountId());
            ps.setString(2, log.getActionType());
            ps.setString(3, log.getFieldName());
            ps.setString(4, log.getOldValue());
            ps.setString(5, log.getNewValue());
            ps.setInt(6, log.getOperatorId());
            ps.setString(7, log.getOperatorName());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert account log", e);
        }
    }

    /**
     * 统计符合条件的账号变更日志总数（不限用户权限）。
     *
     * @param actionType 操作类型筛选（"all" 或不传表示不限）
     * @param keyword    关键词模糊搜索
     * @param dateFrom   起始日期（yyyy-MM-dd）
     * @param dateTo     结束日期（yyyy-MM-dd）
     * @return 符合条件的日志总数
     */
    public int count(String actionType, String keyword, String dateFrom, String dateTo) {
        return count(actionType, keyword, dateFrom, dateTo, 0);
    }

    public int count(String actionType, String keyword, String dateFrom, String dateTo, int userId) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM account_logs al LEFT JOIN accounts ac ON al.account_id = ac.id WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendManagedFilter(sql, params, userId);
        appendFilters(sql, params, actionType, keyword, dateFrom, dateTo);
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count account logs", e);
        }
    }

    /**
     * 分页搜索账号变更日志（不限用户权限），结果含关联的账号名称、项目等信息。
     *
     * @param actionType 操作类型筛选
     * @param keyword    关键词模糊搜索
     * @param dateFrom   起始日期
     * @param dateTo     结束日期
     * @param page       页码（从 1 开始）
     * @param pageSize   每页条数
     * @return 日志分页列表
     */
    public List<AccountLog> search(String actionType, String keyword, String dateFrom, String dateTo,
                                    int page, int pageSize) {
        return search(actionType, keyword, dateFrom, dateTo, page, pageSize, 0);
    }

    public List<AccountLog> search(String actionType, String keyword, String dateFrom, String dateTo,
                                    int page, int pageSize, int userId) {
        List<AccountLog> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT al.*, ac.name AS account_name, ac.project, ac.platform_type, ac.sub_type, ac.login_url FROM account_logs al " +
            "LEFT JOIN accounts ac ON al.account_id = ac.id WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendManagedFilter(sql, params, userId);
        appendFilters(sql, params, actionType, keyword, dateFrom, dateTo);
        sql.append(" ORDER BY al.created_at DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapLog(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search account logs", e);
        }
        return list;
    }

    /**
     * 查询指定账号的所有变更日志，按创建时间倒序排列。
     *
     * @param accountId 账号 ID
     * @return 该账号的日志列表
     */
    public List<AccountLog> findByAccountId(int accountId) {
        List<AccountLog> list = new ArrayList<>();
        String sql = "SELECT al.*, ac.name AS account_name, ac.project, ac.platform_type, ac.sub_type, ac.login_url FROM account_logs al " +
                     "LEFT JOIN accounts ac ON al.account_id = ac.id " +
                     "WHERE al.account_id = ? ORDER BY al.created_at DESC";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapLog(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find account logs by account", e);
        }
        return list;
    }

    private void appendFilters(StringBuilder sql, List<Object> params,
                                String actionType, String keyword, String dateFrom, String dateTo) {
        if (actionType != null && !actionType.isEmpty() && !"all".equals(actionType)) {
            sql.append(" AND al.action_type=?");
            params.add(actionType);
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (ac.name LIKE ? OR ac.project LIKE ? OR ac.platform_type LIKE ? OR ac.sub_type LIKE ?" +
                       " OR ac.login_url LIKE ? OR al.operator_name LIKE ?" +
                       " OR al.field_name LIKE ? OR al.old_value LIKE ? OR al.new_value LIKE ?)");
            String kw = "%" + keyword + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (dateFrom != null && !dateFrom.isEmpty()) {
            sql.append(" AND al.created_at >= ?");
            params.add(dateFrom + " 00:00:00");
        }
        if (dateTo != null && !dateTo.isEmpty()) {
            sql.append(" AND al.created_at <= ?");
            params.add(dateTo + " 23:59:59");
        }
    }

    private void appendManagedFilter(StringBuilder sql, List<Object> params, int userId) {
        if (userId > 0) {
            sql.append(" AND al.account_id IN (");
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

    private AccountLog mapLog(ResultSet rs) throws SQLException {
        AccountLog log = new AccountLog();
        log.setId(rs.getInt("id"));
        log.setAccountId(rs.getInt("account_id"));
        log.setActionType(rs.getString("action_type"));
        log.setFieldName(rs.getString("field_name"));
        log.setOldValue(rs.getString("old_value"));
        log.setNewValue(rs.getString("new_value"));
        log.setOperatorId(rs.getInt("operator_id"));
        log.setOperatorName(rs.getString("operator_name"));
        log.setCreatedAt(rs.getString("created_at"));
        try { log.setAccountName(rs.getString("account_name")); } catch (SQLException ignored) {}
        try { log.setProject(rs.getString("project")); } catch (SQLException ignored) {}
        try { log.setPlatformType(rs.getString("platform_type")); } catch (SQLException ignored) {}
        try { log.setSubType(rs.getString("sub_type")); } catch (SQLException ignored) {}
        try { log.setLoginUrl(rs.getString("login_url")); } catch (SQLException ignored) {}
        return log;
    }
}
