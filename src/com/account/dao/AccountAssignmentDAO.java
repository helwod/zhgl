package com.account.dao;

import com.account.model.AccountAssignment;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 账号分配数据访问对象，管理用户对账号的使用权分配及有效期跟踪。
 * <p>
 * 通过 account_assignments 表记录每个用户对每个账号的使用权及到期时间。
 * 提供插入分配记录、检查有效分配、查询用户所有有效账号 ID 等操作。
 * 有效期通过 expiry_date 与当前日期比较判断，过期后用户将无法继续使用该账号。
 */
public class AccountAssignmentDAO {
    private DBUtil dbUtil = DBUtil.getInstance();

    /**
     * 插入一条账号分配记录（为用户分配账号使用权及到期时间）。
     * <p>
     * SQL: INSERT INTO account_assignments (account_id, user_id, expiry_date) VALUES (?, ?, ?)
     *
     * @param aa 账号分配对象
     */
    public void insert(AccountAssignment aa) {
        String sql = "INSERT INTO account_assignments (account_id, user_id, expiry_date) VALUES (?, ?, ?)";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, aa.getAccountId());
            ps.setInt(2, aa.getUserId());
            ps.setString(3, aa.getExpiryDate());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert account assignment", e);
        }
    }

    /**
     * 检查用户对指定账号是否有有效（未过期）的分配。
     * <p>
     * SQL: SELECT COUNT(*) FROM account_assignments WHERE account_id=? AND user_id=? AND expiry_date >= date('now','localtime')
     *
     * @param accountId 账号 ID
     * @param userId    用户 ID
     * @return true 如果存在有效的分配
     */
    public boolean hasValidAssignment(int accountId, int userId) {
        String sql = "SELECT COUNT(*) FROM account_assignments " +
                     "WHERE account_id=? AND user_id=? AND expiry_date >= date('now','localtime')";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check valid assignment", e);
        }
    }

    /**
     * 查询用户所有有效（未过期）分配的账号 ID（去重）。
     * <p>
     * SQL: SELECT DISTINCT account_id FROM account_assignments WHERE user_id=? AND expiry_date >= date('now','localtime')
     *
     * @param userId 用户 ID
     * @return 有效分配的账号 ID 列表（去重）
     */
    public List<Integer> findValidAccountIdsByUserId(int userId) {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT DISTINCT account_id FROM account_assignments " +
                     "WHERE user_id=? AND expiry_date >= date('now','localtime')";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getInt("account_id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find valid account IDs by user", e);
        }
        return list;
    }
}
