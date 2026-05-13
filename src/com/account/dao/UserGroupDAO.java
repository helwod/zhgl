package com.account.dao;

import com.account.model.User;
import com.account.model.UserGroup;
import com.account.model.UserGroupMember;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户组数据访问对象，提供对 user_groups 表的 CRUD 操作和组成员管理。
 * <p>
 * 用户组用于批量管理账号权限，一个组包含多个用户，一个用户可以属于多个组。
 * 支持组的增删查、成员的批量添加/删除、查询组内成员、查询用户所属组、
 * 以及查询不在某组中的用户（用于多选添加）。
 */
public class UserGroupDAO {
    private DBUtil dbUtil = DBUtil.getInstance();

    /**
     * 创建新的用户组。
     * <p>
     * SQL: INSERT INTO user_groups (name, description) VALUES (?, ?)
     *
     * @param group 用户组对象
     */
    public void insert(UserGroup group) {
        String sql = "INSERT INTO user_groups (name, description) VALUES (?, ?)";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, group.getName());
            ps.setString(2, group.getDescription());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert user group", e);
        }
    }

    /**
     * 删除指定 ID 的用户组。
     * <p>
     * SQL: DELETE FROM user_groups WHERE id=?
     *
     * @param id 用户组 ID
     */
    public void delete(int id) {
        String sql = "DELETE FROM user_groups WHERE id=?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user group", e);
        }
    }

    /**
     * 根据 ID 查询用户组。
     *
     * @param id 用户组 ID
     * @return 用户组对象，未找到时返回 null
     */
    public UserGroup findById(int id) {
        String sql = "SELECT * FROM user_groups WHERE id=?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapGroup(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user group by id", e);
        }
        return null;
    }

    public List<UserGroup> findAll() {
        List<UserGroup> list = new ArrayList<>();
        String sql = "SELECT g.*, (SELECT COUNT(*) FROM user_group_members m WHERE m.group_id = g.id) AS member_count " +
                     "FROM user_groups g ORDER BY g.name ASC";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapGroup(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all user groups", e);
        }
        return list;
    }

    // Member management
    public void addMember(int groupId, int userId) {
        String sql = "INSERT OR IGNORE INTO user_group_members (group_id, user_id) VALUES (?, ?)";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add group member", e);
        }
    }

    /**
     * 批量向用户组添加多名成员（批量执行 INSERT OR IGNORE）。
     *
     * @param groupId 用户组 ID
     * @param userIds 用户 ID 列表
     */
    public void addMembers(int groupId, List<Integer> userIds) {
        String sql = "INSERT OR IGNORE INTO user_group_members (group_id, user_id) VALUES (?, ?)";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int userId : userIds) {
                ps.setInt(1, groupId);
                ps.setInt(2, userId);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to batch add group members", e);
        }
    }

    /**
     * 从用户组中移除一名成员。
     * <p>
     * SQL: DELETE FROM user_group_members WHERE group_id=? AND user_id=?
     *
     * @param groupId 用户组 ID
     * @param userId  用户 ID
     */
    public void removeMember(int groupId, int userId) {
        String sql = "DELETE FROM user_group_members WHERE group_id=? AND user_id=?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove group member", e);
        }
    }

    public List<UserGroupMember> findMembersByGroup(int groupId) {
        List<UserGroupMember> list = new ArrayList<>();
        String sql = "SELECT m.*, u.username, u.display_name AS user_display_name " +
                     "FROM user_group_members m " +
                     "LEFT JOIN users u ON m.user_id = u.id " +
                     "WHERE m.group_id = ? ORDER BY u.display_name";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapMember(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find group members", e);
        }
        return list;
    }

    /**
     * 查询用户组中所有成员的用户 ID。
     *
     * @param groupId 用户组 ID
     * @return 成员用户 ID 列表
     */
    public List<Integer> findUserIdsByGroup(int groupId) {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT user_id FROM user_group_members WHERE group_id=?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getInt("user_id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user ids by group", e);
        }
        return list;
    }

    /**
     * 查询不在指定用户组中的所有用户（用于多选添加成员时列出可选用户）。
     *
     * @param groupId 用户组 ID
     * @return 不在该组中的用户列表，按显示名排序
     */
    public List<User> findUsersNotInGroup(int groupId) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE id NOT IN " +
                     "(SELECT user_id FROM user_group_members WHERE group_id=?) " +
                     "ORDER BY display_name";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setDisplayName(rs.getString("display_name"));
                    u.setRole(rs.getString("role"));
                    u.setDepartment(rs.getString("department"));
                    list.add(u);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find users not in group", e);
        }
        return list;
    }

    /**
     * 查询用户所属的所有用户组。
     *
     * @param userId 用户 ID
     * @return 用户所属的组成员关系列表（含组名）
     */
    public List<UserGroupMember> findGroupsByUser(int userId) {
        List<UserGroupMember> list = new ArrayList<>();
        String sql = "SELECT m.*, g.name AS group_name FROM user_group_members m " +
                     "LEFT JOIN user_groups g ON m.group_id = g.id " +
                     "WHERE m.user_id = ? ORDER BY g.name";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UserGroupMember m = mapMember(rs);
                    try { m.setGroupName(rs.getString("group_name")); } catch (SQLException ignored) {}
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find groups by user", e);
        }
        return list;
    }

    private UserGroup mapGroup(ResultSet rs) throws SQLException {
        UserGroup g = new UserGroup();
        g.setId(rs.getInt("id"));
        g.setName(rs.getString("name"));
        g.setDescription(rs.getString("description"));
        g.setCreatedAt(rs.getString("created_at"));
        try { g.setMemberCount(rs.getInt("member_count")); } catch (SQLException ignored) {}
        return g;
    }

    private UserGroupMember mapMember(ResultSet rs) throws SQLException {
        UserGroupMember m = new UserGroupMember();
        m.setId(rs.getInt("id"));
        m.setGroupId(rs.getInt("group_id"));
        m.setUserId(rs.getInt("user_id"));
        try { m.setUserName(rs.getString("username")); } catch (SQLException ignored) {}
        try { m.setUserDisplayName(rs.getString("user_display_name")); } catch (SQLException ignored) {}
        return m;
    }
}
