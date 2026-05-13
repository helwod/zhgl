package com.account.dao;

import com.account.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户数据访问对象，提供对 users 表的 CRUD 操作。
 * <p>
 * 支持按用户名查询、按 ID 查询、查询所有用户、新增用户（返回自增 ID）、
 * 更新用户信息、更新密码、删除用户以及统计用户总数。
 */
public class UserDAO {
    private DBUtil dbUtil = DBUtil.getInstance();

    /**
     * 根据用户名查询用户。
     *
     * @param username 用户名
     * @return 匹配的用户对象，未找到时返回 null
     */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by username", e);
        }
        return null;
    }

    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by id", e);
        }
        return null;
    }

    /**
     * 查询所有用户，按创建时间倒序排列。
     *
     * @return 用户列表
     */
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all users", e);
        }
        return list;
    }

    /**
     * 插入新用户（不返回自增 ID，委托给 insertAndReturnId）。
     *
     * @param user 用户对象
     */
    public void insert(User user) {
        insertAndReturnId(user);
    }

    /**
     * 插入新用户并返回自增 ID。
     * <p>
     * SQL: INSERT INTO users (username, password, display_name, role, department) VALUES ...
     *
     * @param user 用户对象
     * @return 新用户的自增 ID，插入失败时返回 -1
     */
    public int insertAndReturnId(User user) {
        String sql = "INSERT INTO users (username, password, display_name, role, department) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getDisplayName());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getDepartment());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert user", e);
        }
    }

    /**
     * 更新用户基本信息（用户名、显示名、角色、部门），不修改密码。
     * <p>
     * SQL: UPDATE users SET username=?, display_name=?, role=?, department=? WHERE id=?
     *
     * @param user 包含新信息的用户对象
     */
    public void update(User user) {
        String sql = "UPDATE users SET username=?, display_name=?, role=?, department=? WHERE id=?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getDisplayName());
            ps.setString(3, user.getRole());
            ps.setString(4, user.getDepartment());
            ps.setInt(5, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user", e);
        }
    }

    /**
     * 更新用户密码。
     * <p>
     * SQL: UPDATE users SET password=? WHERE id=?
     *
     * @param id            用户 ID
     * @param hashedPassword 经过哈希处理的新密码
     */
    public void updatePassword(int id, String hashedPassword) {
        String sql = "UPDATE users SET password=? WHERE id=?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user password", e);
        }
    }

    /**
     * 删除指定 ID 的用户。
     * <p>
     * SQL: DELETE FROM users WHERE id=?
     *
     * @param id 要删除的用户 ID
     */
    public void delete(int id) {
        String sql = "DELETE FROM users WHERE id=?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    /**
     * 统计用户总数。
     * <p>
     * SQL: SELECT COUNT(*) FROM users
     *
     * @return 用户总数
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count users", e);
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setDisplayName(rs.getString("display_name"));
        u.setRole(rs.getString("role"));
        u.setDepartment(rs.getString("department"));
        u.setCreatedAt(rs.getString("created_at"));
        return u;
    }
}