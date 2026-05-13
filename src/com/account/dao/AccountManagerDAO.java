package com.account.dao;

import com.account.model.AccountManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 账号管理员数据访问对象，管理账号与用户/用户组之间的授权关系。
 * <p>
 * 提供两套独立的管理机制：
 * <ul>
 *   <li><b>个人级授权</b>（account_managers 表）：直接为某个用户授予账号管理权限</li>
 *   <li><b>组级授权</b>（account_group_managers 表）：为某个用户组整体授予管理权限，<br>
 *       组内所有成员自动继承该权限</li>
 * </ul>
 * 还提供 "组扩展" 查询方法，同时检查个人授权和组授权以判断用户是否有权限。
 */
public class AccountManagerDAO {
    private DBUtil dbUtil = DBUtil.getInstance();

    public void insert(AccountManager am) {
        String sql = "INSERT OR IGNORE INTO account_managers (account_id, user_id, assigned_by) VALUES (?, ?, ?)";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, am.getAccountId());
            ps.setInt(2, am.getUserId());
            ps.setInt(3, am.getAssignedBy());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert account manager", e);
        }
    }

    /**
     * 移除指定账号和用户之间的个人管理员关系。
     * <p>
     * SQL: DELETE FROM account_managers WHERE account_id=? AND user_id=?
     *
     * @param accountId 账号 ID
     * @param userId    用户 ID
     */
    public void delete(int accountId, int userId) {
        String sql = "DELETE FROM account_managers WHERE account_id=? AND user_id=?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete account manager", e);
        }
    }

    public void deleteByAccount(int accountId) {
        String sql = "DELETE FROM account_managers WHERE account_id=?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete account managers by account", e);
        }
    }

    public List<AccountManager> findByAccountId(int accountId) {
        List<AccountManager> list = new ArrayList<>();
        String sql = "SELECT am.*, u.display_name AS user_display_name, u.username AS user_name " +
                     "FROM account_managers am " +
                     "LEFT JOIN users u ON am.user_id = u.id " +
                     "WHERE am.account_id = ? ORDER BY u.display_name";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapManager(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find account managers by account", e);
        }
        return list;
    }

    /**
     * 查询用户通过个人授权管理的所有账号 ID。
     *
     * @param userId 用户 ID
     * @return 受管账号 ID 列表
     */
    public List<Integer> findManagedAccountIds(int userId) {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT account_id FROM account_managers WHERE user_id = ?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getInt("account_id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find managed account IDs", e);
        }
        return list;
    }

    /**
     * Check if user is a manager of an account, via individual assignment OR group assignment.
     * Returns true if user is in account_managers OR belongs to a group that is in account_group_managers.
     */
    public boolean isManagerWithGroupExpansion(int accountId, int userId) {
        String sql = "SELECT COUNT(*) FROM (" +
                     "  SELECT am.account_id FROM account_managers am " +
                     "  WHERE am.account_id=? AND am.user_id=? " +
                     "  UNION " +
                     "  SELECT agm.account_id FROM account_group_managers agm " +
                     "  INNER JOIN user_group_members ugm ON agm.group_id = ugm.group_id " +
                     "  WHERE agm.account_id=? AND ugm.user_id=?" +
                     ")";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setInt(2, userId);
            ps.setInt(3, accountId);
            ps.setInt(4, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check account manager with group expansion", e);
        }
    }

    /**
     * 查询用户通过个人授权或组授权管理的所有账号 ID（取并集去重）。
     *
     * @param userId 用户 ID
     * @return 受管账号 ID 列表，按账号 ID 排序
     */
    public List<Integer> findManagedAccountIdsWithGroups(int userId) {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT account_id FROM (" +
                     "  SELECT am.account_id FROM account_managers am WHERE am.user_id=? " +
                     "  UNION " +
                     "  SELECT agm.account_id FROM account_group_managers agm " +
                     "  INNER JOIN user_group_members ugm ON agm.group_id = ugm.group_id " +
                     "  WHERE ugm.user_id=?" +
                     ") ORDER BY account_id";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getInt("account_id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find managed account IDs with groups", e);
        }
        return list;
    }

    private AccountManager mapManager(ResultSet rs) throws SQLException {
        AccountManager am = new AccountManager();
        am.setId(rs.getInt("id"));
        am.setAccountId(rs.getInt("account_id"));
        am.setUserId(rs.getInt("user_id"));
        am.setAssignedBy(rs.getInt("assigned_by"));
        am.setAssignedAt(rs.getString("assigned_at"));
        try { am.setUserDisplayName(rs.getString("user_display_name")); } catch (SQLException ignored) {}
        try { am.setUserName(rs.getString("user_name")); } catch (SQLException ignored) {}
        return am;
    }

    // Account group manager operations

    public void insertGroupManager(int accountId, int groupId, int assignedBy) {
        String sql = "INSERT OR IGNORE INTO account_group_managers (account_id, group_id, assigned_by) VALUES (?, ?, ?)";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setInt(2, groupId);
            ps.setInt(3, assignedBy);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert account group manager", e);
        }
    }

    public void deleteGroupManager(int accountId, int groupId) {
        String sql = "DELETE FROM account_group_managers WHERE account_id=? AND group_id=?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setInt(2, groupId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete account group manager", e);
        }
    }

    /**
     * 移除指定账号的所有组级别管理员。
     * <p>
     * SQL: DELETE FROM account_group_managers WHERE account_id=?
     *
     * @param accountId 账号 ID
     */
    public void deleteGroupManagersByAccount(int accountId) {
        String sql = "DELETE FROM account_group_managers WHERE account_id=?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete account group managers by account", e);
        }
    }

    /**
     * 查询指定账号的所有组级别管理员（含组名和组成员数）。
     *
     * @param accountId 账号 ID
     * @return 组管理员列表，按组名排序
     */
    public List<com.account.model.AccountGroupManager> findGroupManagersByAccount(int accountId) {
        List<com.account.model.AccountGroupManager> list = new ArrayList<>();
        String sql = "SELECT agm.*, g.name AS group_name, " +
                     "(SELECT COUNT(*) FROM user_group_members ugm2 WHERE ugm2.group_id = agm.group_id) AS member_count " +
                     "FROM account_group_managers agm " +
                     "LEFT JOIN user_groups g ON agm.group_id = g.id " +
                     "WHERE agm.account_id = ? ORDER BY g.name";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    com.account.model.AccountGroupManager agm = new com.account.model.AccountGroupManager();
                    agm.setId(rs.getInt("id"));
                    agm.setAccountId(rs.getInt("account_id"));
                    agm.setGroupId(rs.getInt("group_id"));
                    agm.setAssignedBy(rs.getInt("assigned_by"));
                    agm.setAssignedAt(rs.getString("assigned_at"));
                    try { agm.setGroupName(rs.getString("group_name")); } catch (SQLException ignored) {}
                    try { agm.setMemberCount(rs.getInt("member_count")); } catch (SQLException ignored) {}
                    list.add(agm);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find account group managers", e);
        }
        return list;
    }
}
