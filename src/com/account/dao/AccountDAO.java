package com.account.dao;

import com.account.model.Account;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 账号数据访问对象，提供对 accounts 表的完整 CRUD 操作以及受管账号的查询能力。
 * <p>
 * 支持按条件分页查询全部账号或仅查询当前用户个人和组级别受管的账号。
 * 受管账号的权限基于 account_managers（个人直接授权）和 account_group_managers
 * （通过用户组成员身份间接授权）两张关联表。
 */
public class AccountDAO {
    private DBUtil dbUtil = DBUtil.getInstance();

    /**
     * 插入新账号并返回自增 ID。
     * <p>
     * SQL: INSERT INTO accounts (name, password_encrypted, ... , created_by) VALUES ...
     *
     * @param acc 账号对象
     * @return 新账号的自增 ID，插入失败时返回 -1
     */
    public int insert(Account acc) {
        String sql = "INSERT INTO accounts (name, password_encrypted, password_iv, password_tag, " +
                     "platform_type, sub_type, project, department, expiry_date, status, login_url, notes, created_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, acc.getName());
            ps.setString(2, acc.getPasswordEncrypted());
            ps.setString(3, acc.getPasswordIv());
            ps.setString(4, acc.getPasswordTag());
            ps.setString(5, acc.getPlatformType());
            ps.setString(6, acc.getSubType());
            ps.setString(7, acc.getProject());
            ps.setString(8, acc.getDepartment());
            ps.setString(9, acc.getExpiryDate());
            ps.setString(10, acc.getStatus());
            ps.setString(11, acc.getLoginUrl());
            ps.setString(12, acc.getNotes());
            ps.setInt(13, acc.getCreatedBy());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert account", e);
        }
    }

    /**
     * 更新账号信息（同时自动更新 updated_at 字段）。
     * <p>
     * SQL: UPDATE accounts SET name=?, password_encrypted=?, ..., updated_at=datetime('now','localtime') WHERE id=?
     *
     * @param acc 包含最新信息的账号对象
     */
    public void update(Account acc) {
        String sql = "UPDATE accounts SET name=?, password_encrypted=?, password_iv=?, password_tag=?, " +
                     "platform_type=?, sub_type=?, project=?, department=?, expiry_date=?, status=?, login_url=?, notes=?, " +
                     "updated_at=datetime('now','localtime') WHERE id=?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, acc.getName());
            ps.setString(2, acc.getPasswordEncrypted());
            ps.setString(3, acc.getPasswordIv());
            ps.setString(4, acc.getPasswordTag());
            ps.setString(5, acc.getPlatformType());
            ps.setString(6, acc.getSubType());
            ps.setString(7, acc.getProject());
            ps.setString(8, acc.getDepartment());
            ps.setString(9, acc.getExpiryDate());
            ps.setString(10, acc.getStatus());
            ps.setString(11, acc.getLoginUrl());
            ps.setString(12, acc.getNotes());
            ps.setInt(13, acc.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update account", e);
        }
    }

    /**
     * 删除指定 ID 的账号。
     * <p>
     * SQL: DELETE FROM accounts WHERE id=?
     *
     * @param id 要删除的账号 ID
     */
    public void delete(int id) {
        String sql = "DELETE FROM accounts WHERE id=?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete account", e);
        }
    }

    public Account findById(int id) {
        String sql = "SELECT * FROM accounts WHERE id=?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapAccount(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find account by id", e);
        }
        return null;
    }

    /**
     * 统计符合筛选条件的账号总数。
     *
     * @param platformType 平台类型筛选（为空时不限制）
     * @param status       状态筛选（为空时不限制）
     * @param keyword      关键词模糊搜索（按名称/项目/子类型/部门/备注匹配，为空时不限制）
     * @return 符合条件的账号总数
     */
    public int count(String platformType, String status, String keyword) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM accounts WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, platformType, status, keyword);

        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count accounts", e);
        }
    }

    /**
     * 分页查询符合条件的账号列表，按项目/平台/子类型/名称排序。
     *
     * @param platformType 平台类型筛选
     * @param status       状态筛选
     * @param keyword      关键词模糊搜索
     * @param page         页码（从 1 开始）
     * @param pageSize     每页条数
     * @return 账号分页列表
     */
    public List<Account> findPage(String platformType, String status, String keyword,
                                   int page, int pageSize) {
        List<Account> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM accounts WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, platformType, status, keyword);
        sql.append(" ORDER BY project ASC, platform_type ASC, sub_type ASC, name ASC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAccount(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find accounts page", e);
        }
        return list;
    }

    public List<Account> findAll() {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM accounts ORDER BY project ASC, platform_type ASC, sub_type ASC, name ASC";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapAccount(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all accounts", e);
        }
        return list;
    }

    /**
     * 查询当前用户（个人或组级别）有权管理的所有账号。
     * <p>
     * 权限查找方式：先从 account_managers 表查个人授权，再从 account_group_managers +
     * user_group_members 查组授权，取并集去重。
     *
     * @param userId 当前用户 ID
     * @return 该用户有权管理的账号列表
     */
    public List<Account> findAllManaged(int userId) {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT DISTINCT a.* FROM accounts a " +
                     "WHERE a.id IN (" +
                     "  SELECT am.account_id FROM account_managers am WHERE am.user_id=? " +
                     "  UNION " +
                     "  SELECT agm.account_id FROM account_group_managers agm " +
                     "  INNER JOIN user_group_members ugm ON agm.group_id = ugm.group_id " +
                     "  WHERE ugm.user_id=?" +
                     ") " +
                     "ORDER BY a.project ASC, a.platform_type ASC, a.sub_type ASC, a.name ASC";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapAccount(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all managed accounts", e);
        }
        return list;
    }

    /**
     * 统计当前用户受管账号中符合筛选条件的数量。
     *
     * @param userId       当前用户 ID
     * @param platformType 平台类型筛选
     * @param status       状态筛选
     * @param keyword      关键词模糊搜索
     * @return 符合条件的受管账号数
     */
    public int countManaged(int userId, String platformType, String status, String keyword) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(DISTINCT a.id) FROM accounts a " +
            "WHERE a.id IN (" +
            "  SELECT am.account_id FROM account_managers am WHERE am.user_id=? " +
            "  UNION " +
            "  SELECT agm.account_id FROM account_group_managers agm " +
            "  INNER JOIN user_group_members ugm ON agm.group_id = ugm.group_id " +
            "  WHERE ugm.user_id=?" +
            ")");
        List<Object> params = new ArrayList<>();
        params.add(userId);
        params.add(userId);
        appendFilters(sql, params, platformType, status, keyword);
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count managed accounts", e);
        }
    }

    /**
     * 分页查询当前用户受管账号（含个人授权 + 组授权）。
     *
     * @param userId       当前用户 ID
     * @param platformType 平台类型筛选
     * @param status       状态筛选
     * @param keyword      关键词模糊搜索
     * @param page         页码（从 1 开始）
     * @param pageSize     每页条数
     * @return 受管账号分页列表
     */
    public List<Account> findPageManaged(int userId, String platformType, String status, String keyword,
                                          int page, int pageSize) {
        List<Account> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT DISTINCT a.* FROM accounts a " +
            "WHERE a.id IN (" +
            "  SELECT am.account_id FROM account_managers am WHERE am.user_id=? " +
            "  UNION " +
            "  SELECT agm.account_id FROM account_group_managers agm " +
            "  INNER JOIN user_group_members ugm ON agm.group_id = ugm.group_id " +
            "  WHERE ugm.user_id=?" +
            ")");
        List<Object> params = new ArrayList<>();
        params.add(userId);
        params.add(userId);
        appendFilters(sql, params, platformType, status, keyword);
        sql.append(" ORDER BY a.project ASC, a.platform_type ASC, a.sub_type ASC, a.name ASC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapAccount(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find managed accounts page", e);
        }
        return list;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM accounts";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count all accounts", e);
        }
    }

    /**
     * 根据 ID 列表查询账号（含筛选和分页），通常用于基于一组受管账号 ID 做进一步过滤。
     *
     * @param ids          账号 ID 列表
     * @param platformType 平台类型筛选
     * @param status       状态筛选
     * @param keyword      关键词模糊搜索
     * @param page         页码（从 1 开始）
     * @param pageSize     每页条数
     * @return 符合条件的账号分页列表
     */
    public List<Account> findByIds(List<Integer> ids, String platformType, String status, String keyword,
                                    int page, int pageSize) {
        List<Account> list = new ArrayList<>();
        if (ids == null || ids.isEmpty()) return list;

        StringBuilder sql = new StringBuilder("SELECT * FROM accounts WHERE id IN (");
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sql.append(",");
            sql.append("?");
            params.add(ids.get(i));
        }
        sql.append(")");
        appendFilters(sql, params, platformType, status, keyword);
        sql.append(" ORDER BY project ASC, platform_type ASC, sub_type ASC, name ASC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapAccount(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find accounts by ids", e);
        }
        return list;
    }

    private void appendFilters(StringBuilder sql, List<Object> params,
                                String platformType, String status, String keyword) {
        if (platformType != null && !platformType.isEmpty()) {
            sql.append(" AND platform_type=?");
            params.add(platformType);
        }
        if (status != null && !status.isEmpty()) {
            sql.append(" AND status=?");
            params.add(status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (name LIKE ? OR project LIKE ? OR sub_type LIKE ? OR department LIKE ? OR notes LIKE ?)");
            String kw = "%" + keyword + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
    }

    private Account mapAccount(ResultSet rs) throws SQLException {
        Account a = new Account();
        a.setId(rs.getInt("id"));
        a.setName(rs.getString("name"));
        a.setPasswordEncrypted(rs.getString("password_encrypted"));
        a.setPasswordIv(rs.getString("password_iv"));
        a.setPasswordTag(rs.getString("password_tag"));
        a.setPlatformType(rs.getString("platform_type"));
        a.setSubType(rs.getString("sub_type"));
        a.setProject(rs.getString("project"));
        a.setDepartment(rs.getString("department"));
        a.setExpiryDate(rs.getString("expiry_date"));
        a.setStatus(rs.getString("status"));
        a.setLoginUrl(rs.getString("login_url"));
        a.setNotes(rs.getString("notes"));
        a.setCreatedBy(rs.getInt("created_by"));
        a.setCreatedAt(rs.getString("created_at"));
        a.setUpdatedAt(rs.getString("updated_at"));
        return a;
    }
}
