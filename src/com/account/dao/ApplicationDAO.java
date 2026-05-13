package com.account.dao;

import com.account.model.Application;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 申请数据访问对象，提供对 applications 表的完整操作，覆盖申请审批工作流。
 * <p>
 * 主要功能包括：提交申请、审批通过/驳回、按申请人/状态/关键词分页搜索、
 * 检查已批准的申请及过期状态（通过 account_assignments 表的有效期跟踪）。
 * 审批流转状态：pending（待审批）→ approved（已通过）/ rejected（已驳回）。
 */
public class ApplicationDAO {
    private DBUtil dbUtil = DBUtil.getInstance();

    public void insert(Application app) {
        String sql = "INSERT INTO applications (account_id, applicant_id, reason, status, valid_days) VALUES (?, ?, ?, 'pending', ?)";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, app.getAccountId());
            ps.setInt(2, app.getApplicantId());
            ps.setString(3, app.getReason());
            if (app.getValidDays() != null) {
                ps.setInt(4, app.getValidDays());
            } else {
                ps.setInt(4, 7);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert application", e);
        }
    }

    /**
     * 审批通过指定申请（将状态更新为 'approved'）。
     *
     * @param id         申请 ID
     * @param reviewerId 审批人 ID
     * @param comment    审批意见
     */
    public void approve(int id, int reviewerId, String comment) {
        String sql = "UPDATE applications SET status='approved', review_comment=?, reviewed_by=?, " +
                     "reviewed_at=datetime('now','localtime') WHERE id=?";
        executeReview(sql, comment, reviewerId, id);
    }

    /**
     * 驳回指定申请（将状态更新为 'rejected'）。
     *
     * @param id         申请 ID
     * @param reviewerId 审批人 ID
     * @param comment    驳回原因
     */
    public void reject(int id, int reviewerId, String comment) {
        String sql = "UPDATE applications SET status='rejected', review_comment=?, reviewed_by=?, " +
                     "reviewed_at=datetime('now','localtime') WHERE id=?";
        executeReview(sql, comment, reviewerId, id);
    }

    private void executeReview(String sql, String comment, int reviewerId, int id) {
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, comment);
            ps.setInt(2, reviewerId);
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to review application", e);
        }
    }

    public List<Application> findByApplicant(int applicantId) {
        List<Application> list = new ArrayList<>();
        String sql = "SELECT a.*, ac.name AS account_name, u.display_name AS applicant_name, ac.login_url, " +
                     "ac.project, ac.platform_type, ac.sub_type " +
                     "FROM applications a " +
                     "LEFT JOIN accounts ac ON a.account_id = ac.id " +
                     "LEFT JOIN users u ON a.applicant_id = u.id " +
                     "WHERE a.applicant_id = ? ORDER BY a.created_at DESC";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapApplication(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find applications by applicant", e);
        }
        return list;
    }

    /**
     * 根据状态查询所有申请（含申请人/审批人显示名）。
     *
     * @param status 状态筛选（如 "pending", "approved", "rejected"）
     * @return 申请列表
     */
    public List<Application> findByStatus(String status) {
        List<Application> list = new ArrayList<>();
        String sql = "SELECT a.*, ac.name AS account_name, u1.display_name AS applicant_name, " +
                     "u2.display_name AS reviewer_name, ac.login_url " +
                     "FROM applications a " +
                     "LEFT JOIN accounts ac ON a.account_id = ac.id " +
                     "LEFT JOIN users u1 ON a.applicant_id = u1.id " +
                     "LEFT JOIN users u2 ON a.reviewed_by = u2.id " +
                     "WHERE a.status = ? ORDER BY a.created_at DESC";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapApplication(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find applications by status", e);
        }
        return list;
    }

    /**
     * 查询所有申请。
     *
     * @return 全部申请列表，按创建时间倒序
     */
    public List<Application> findAll() {
        List<Application> list = new ArrayList<>();
        String sql = "SELECT a.*, ac.name AS account_name, u1.display_name AS applicant_name, " +
                     "u2.display_name AS reviewer_name, ac.login_url, " +
                     "ac.project, ac.platform_type, ac.sub_type " +
                     "FROM applications a " +
                     "LEFT JOIN accounts ac ON a.account_id = ac.id " +
                     "LEFT JOIN users u1 ON a.applicant_id = u1.id " +
                     "LEFT JOIN users u2 ON a.reviewed_by = u2.id " +
                     "ORDER BY a.created_at DESC";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapApplication(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all applications", e);
        }
        return list;
    }

    /**
     * 统计待审批的申请数。
     *
     * @return 待审批申请总数
     */
    public int countPending() {
        String sql = "SELECT COUNT(*) FROM applications WHERE status='pending'";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count pending applications", e);
        }
    }

    public int countByApplicant(int applicantId) {
        String sql = "SELECT COUNT(*) FROM applications WHERE applicant_id=?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicantId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count applications by applicant", e);
        }
    }

    /**
     * 统计所有申请总数。
     *
     * @return 申请总数
     */
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM applications";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count all applications", e);
        }
    }

    public int searchCount(String keyword, String statusFilter, String dateFrom, String dateTo) {
        return searchCount(keyword, statusFilter, dateFrom, dateTo, 0);
    }

    /**
     * 统计符合条件的申请数（可限定只查询当前用户受管账号的申请）。
     *
     * @param keyword      关键词模糊搜索
     * @param statusFilter 状态筛选
     * @param dateFrom     起始日期
     * @param dateTo       结束日期
     * @param userId       用户 ID（>0 时只查该用户受管账号的申请）
     * @return 符合条件的申请总数
     */
    public int searchCount(String keyword, String statusFilter, String dateFrom, String dateTo, int userId) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM applications a " +
            "LEFT JOIN accounts ac ON a.account_id = ac.id " +
            "LEFT JOIN users u ON a.applicant_id = u.id WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendManagedFilter(sql, params, userId);
        appendSearchFilters(sql, params, keyword, statusFilter, dateFrom, dateTo);
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search count applications", e);
        }
    }

    public List<Application> search(String keyword, String statusFilter, String dateFrom, String dateTo, int page, int pageSize) {
        return search(keyword, statusFilter, dateFrom, dateTo, page, pageSize, 0);
    }

    /**
     * 分页搜索申请（可限定用户权限范围），按创建时间倒序排列。
     *
     * @param keyword      关键词模糊搜索
     * @param statusFilter 状态筛选
     * @param dateFrom     起始日期
     * @param dateTo       结束日期
     * @param page         页码（从 1 开始）
     * @param pageSize     每页条数
     * @param userId       用户 ID（>0 时只查该用户受管账号的申请）
     * @return 申请分页列表
     */
    public List<Application> search(String keyword, String statusFilter, String dateFrom, String dateTo, int page, int pageSize, int userId) {
        List<Application> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT a.*, ac.name AS account_name, u.display_name AS applicant_name, " +
            "u2.display_name AS reviewer_name, " +
            "ac.project, ac.platform_type, ac.sub_type, ac.login_url " +
            "FROM applications a " +
            "LEFT JOIN accounts ac ON a.account_id = ac.id " +
            "LEFT JOIN users u ON a.applicant_id = u.id " +
            "LEFT JOIN users u2 ON a.reviewed_by = u2.id WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendManagedFilter(sql, params, userId);
        appendSearchFilters(sql, params, keyword, statusFilter, dateFrom, dateTo);
        sql.append(" ORDER BY a.created_at DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapApplication(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search applications", e);
        }
        return list;
    }

    private void appendSearchFilters(StringBuilder sql, List<Object> params, String keyword, String statusFilter, String dateFrom, String dateTo) {
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (ac.name LIKE ? OR ac.project LIKE ? OR ac.platform_type LIKE ?" +
                       " OR ac.sub_type LIKE ? OR ac.login_url LIKE ?" +
                       " OR u.display_name LIKE ? OR a.reason LIKE ?)");
            String kw = "%" + keyword + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (statusFilter != null && !statusFilter.isEmpty() && !"all".equals(statusFilter)) {
            sql.append(" AND a.status=?");
            params.add(statusFilter);
        }
        if (dateFrom != null && !dateFrom.isEmpty()) {
            sql.append(" AND a.created_at >= ?");
            params.add(dateFrom + " 00:00:00");
        }
        if (dateTo != null && !dateTo.isEmpty()) {
            sql.append(" AND a.created_at <= ?");
            params.add(dateTo + " 23:59:59");
        }
    }

    private void appendManagedFilter(StringBuilder sql, List<Object> params, int userId) {
        if (userId > 0) {
            sql.append(" AND a.account_id IN (");
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

    /**
     * 查询指定账号的所有申请。
     *
     * @param accountId 账号 ID
     * @return 申请列表，按创建时间倒序
     */
    public List<Application> findByAccountId(int accountId) {
        List<Application> list = new ArrayList<>();
        String sql = "SELECT a.*, ac.name AS account_name, u.display_name AS applicant_name, " +
                     "u2.display_name AS reviewer_name, ac.login_url " +
                     "FROM applications a " +
                     "LEFT JOIN accounts ac ON a.account_id = ac.id " +
                     "LEFT JOIN users u ON a.applicant_id = u.id " +
                     "LEFT JOIN users u2 ON a.reviewed_by = u2.id " +
                     "WHERE a.account_id = ? ORDER BY a.created_at DESC";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapApplication(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find applications by account", e);
        }
        return list;
    }

    public boolean hasApprovedApplication(int accountId, int applicantId) {
        String sql = "SELECT COUNT(*) FROM applications WHERE account_id=? AND applicant_id=? AND status='approved'";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setInt(2, applicantId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check approved application", e);
        }
    }

    /**
     * 检查用户对指定账号是否有未过期的有效分配。
     * <p>
     * 通过 account_assignments 表查询，判断 expiry_date >= 当前日期。
     *
     * @param accountId   账号 ID
     * @param applicantId 申请人 ID
     * @return true 如果存在有效（未过期）的账号分配
     */
    public boolean hasApprovedApplicationValid(int accountId, int applicantId) {
        // Delegates to account_assignments table for expiry tracking
        String sql = "SELECT COUNT(*) FROM account_assignments " +
                     "WHERE account_id=? AND user_id=? AND expiry_date >= date('now','localtime')";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setInt(2, applicantId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check valid approved application", e);
        }
    }

    /**
     * 查询申请人所有待审批状态的账号 ID。
     *
     * @param applicantId 申请人 ID
     * @return 待审批的账号 ID 列表
     */
    public List<Integer> findPendingAccountIdsByApplicant(int applicantId) {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT account_id FROM applications WHERE applicant_id=? AND status='pending'";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getInt("account_id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find pending account IDs by applicant", e);
        }
        return list;
    }

    /** Find distinct account IDs that the user has a valid (non-expired) assignment for. */
    public List<Integer> findApprovedAccountIdsByApplicant(int applicantId) {
        // Delegates to account_assignments table for expiry tracking
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT DISTINCT account_id FROM account_assignments " +
                     "WHERE user_id=? AND expiry_date >= date('now','localtime')";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getInt("account_id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find approved account IDs by applicant", e);
        }
        return list;
    }

    /**
     * 根据 ID 查询申请详情（含账号名称、申请人/审批人显示名）。
     *
     * @param id 申请 ID
     * @return 申请对象，未找到时返回 null
     */
    public Application findById(int id) {
        String sql = "SELECT a.*, ac.name AS account_name, u.display_name AS applicant_name, " +
                     "u2.display_name AS reviewer_name, ac.login_url " +
                     "FROM applications a " +
                     "LEFT JOIN accounts ac ON a.account_id = ac.id " +
                     "LEFT JOIN users u ON a.applicant_id = u.id " +
                     "LEFT JOIN users u2 ON a.reviewed_by = u2.id " +
                     "WHERE a.id=?";
        try (Connection conn = dbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapApplication(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find application by id", e);
        }
        return null;
    }

    private Application mapApplication(ResultSet rs) throws SQLException {
        Application a = new Application();
        a.setId(rs.getInt("id"));
        a.setAccountId(rs.getInt("account_id"));
        a.setApplicantId(rs.getInt("applicant_id"));
        a.setReason(rs.getString("reason"));
        a.setStatus(rs.getString("status"));
        a.setReviewComment(rs.getString("review_comment"));
        a.setReviewedBy(rs.getObject("reviewed_by") != null ? rs.getInt("reviewed_by") : null);
        a.setCreatedAt(rs.getString("created_at"));
        a.setReviewedAt(rs.getString("reviewed_at"));
        try { a.setValidDays(rs.getObject("valid_days") != null ? rs.getInt("valid_days") : 7); } catch (SQLException ignored) {}
        try { a.setAccountName(rs.getString("account_name")); } catch (SQLException ignored) {}
        try { a.setApplicantName(rs.getString("applicant_name")); } catch (SQLException ignored) {}
        try { a.setReviewerName(rs.getString("reviewer_name")); } catch (SQLException ignored) {}
        try { a.setProject(rs.getString("project")); } catch (SQLException ignored) {}
        try { a.setPlatformType(rs.getString("platform_type")); } catch (SQLException ignored) {}
        try { a.setSubType(rs.getString("sub_type")); } catch (SQLException ignored) {}
        try { a.setLoginUrl(rs.getString("login_url")); } catch (SQLException ignored) {}
        return a;
    }
}
