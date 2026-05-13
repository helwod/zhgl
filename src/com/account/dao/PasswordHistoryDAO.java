package com.account.dao;

import com.account.model.PasswordHistory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 密码历史数据访问对象，提供对 password_history 表（密码改动的历史存档）的插入和查询。
 * <p>
 * 每次账号密码被修改时，旧的加密密码会被保存到 password_history 表，以便追踪密码变更。
 * 支持按账号 ID 查询历史记录，结果按变更时间倒序排列。
 */
public class PasswordHistoryDAO {
    private DBUtil dbUtil = DBUtil.getInstance();

    public void insert(PasswordHistory ph) {
        String sql = "INSERT INTO password_history (account_id, password_encrypted, password_iv, password_tag, " +
                     "changed_by, changed_name) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ph.getAccountId());
            ps.setString(2, ph.getPasswordEncrypted());
            ps.setString(3, ph.getPasswordIv());
            ps.setString(4, ph.getPasswordTag());
            ps.setInt(5, ph.getChangedBy());
            ps.setString(6, ph.getChangedName());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert password history", e);
        }
    }

    /**
     * 查询指定账号的密码修改历史记录，按变更时间倒序排列。
     *
     * @param accountId 账号 ID
     * @return 密码历史记录列表
     */
    public List<PasswordHistory> findByAccountId(int accountId) {
        List<PasswordHistory> list = new ArrayList<>();
        String sql = "SELECT * FROM password_history WHERE account_id = ? ORDER BY changed_at DESC";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapHistory(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find password history by account", e);
        }
        return list;
    }

    private PasswordHistory mapHistory(ResultSet rs) throws SQLException {
        PasswordHistory ph = new PasswordHistory();
        ph.setId(rs.getInt("id"));
        ph.setAccountId(rs.getInt("account_id"));
        ph.setPasswordEncrypted(rs.getString("password_encrypted"));
        ph.setPasswordIv(rs.getString("password_iv"));
        ph.setPasswordTag(rs.getString("password_tag"));
        ph.setChangedBy(rs.getInt("changed_by"));
        ph.setChangedName(rs.getString("changed_name"));
        ph.setChangedAt(rs.getString("changed_at"));
        return ph;
    }
}
