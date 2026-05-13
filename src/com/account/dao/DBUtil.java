package com.account.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据库工具类（单例模式），负责管理 SQLite 数据库连接和所有表的初始化。
 * <p>
 * 在应用启动时，通过 {@link #initDatabase()} 创建所有必要的表结构和默认数据。
 * 采用懒汉式单例，首次调用 {@link #getInstance(String)} 时完成数据库驱动加载和实例创建。
 * <p>
 * 管理的表包括：users（用户）、accounts（账号）、applications（申请）、
 * password_logs（密码操作日志）、login_logs（登录日志）、account_logs（账号变更日志）、
 * password_history（密码历史）、account_managers（账号管理员/个人级）、
 * user_groups（用户组）、user_group_members（组成员）、
 * account_group_managers（账号管理员/组级）、account_assignments（账号分配/过期跟踪）、
 * platforms（平台类型字典）。
 */
public class DBUtil {
    private static DBUtil instance;
    private String dbPath;

    /**
     * 私有构造函数，初始化数据库文件路径并加载 SQLite JDBC 驱动。
     *
     * @param dbPath SQLite 数据库文件的路径（支持相对路径或绝对路径）
     */
    private DBUtil(String dbPath) {
        this.dbPath = dbPath;
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }
    }

    /**
     * 获取 DBUtil 单例实例（首次调用时需指定数据库路径）。
     *
     * @param dbPath SQLite 数据库文件路径
     * @return DBUtil 单例实例
     */
    public static synchronized DBUtil getInstance(String dbPath) {
        if (instance == null) {
            instance = new DBUtil(dbPath);
        }
        return instance;
    }

    public static synchronized DBUtil getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DBUtil not initialized. Call getInstance(dbPath) first.");
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        conn.createStatement().execute("PRAGMA foreign_keys = ON");
        return conn;
    }

    /**
     * 初始化数据库：创建所有必需的表结构（如不存在则创建），并插入默认平台类型数据。
     * <p>
     * 依次创建的表：users, accounts, applications, password_logs, login_logs,
     * account_logs, password_history, account_managers, user_groups,
     * user_group_members, account_group_managers, account_assignments, platforms。
     * <p>
     * 同时执行数据库迁移：为已有表补充新追加的列（valid_days, sub_type, project, action_type）。
     */
    public void initDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Users table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  username TEXT UNIQUE NOT NULL," +
                "  password TEXT NOT NULL," +
                "  display_name TEXT NOT NULL," +
                "  role TEXT NOT NULL DEFAULT 'user'," +
                "  department TEXT," +
                "  created_at TEXT DEFAULT (datetime('now','localtime'))" +
                ")"
            );

            // Accounts table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS accounts (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  name TEXT NOT NULL," +
                "  password_encrypted TEXT," +
                "  password_iv TEXT," +
                "  password_tag TEXT," +
                "  platform_type TEXT NOT NULL," +
                "  sub_type TEXT," +
                "  project TEXT," +
                "  department TEXT," +
                "  expiry_date TEXT," +
                "  status TEXT NOT NULL DEFAULT '可用'," +
                "  login_url TEXT," +
                "  notes TEXT," +
                "  created_by INTEGER," +
                "  created_at TEXT DEFAULT (datetime('now','localtime'))," +
                "  updated_at TEXT DEFAULT (datetime('now','localtime'))," +
                "  FOREIGN KEY (created_by) REFERENCES users(id)" +
                ")"
            );

            // Applications table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS applications (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  account_id INTEGER NOT NULL," +
                "  applicant_id INTEGER NOT NULL," +
                "  reason TEXT," +
                "  status TEXT NOT NULL DEFAULT 'pending'," +
                "  review_comment TEXT," +
                "  reviewed_by INTEGER," +
                "  created_at TEXT DEFAULT (datetime('now','localtime'))," +
                "  reviewed_at TEXT," +
                "  FOREIGN KEY (account_id) REFERENCES accounts(id)," +
                "  FOREIGN KEY (applicant_id) REFERENCES users(id)," +
                "  FOREIGN KEY (reviewed_by) REFERENCES users(id)" +
                ")"
            );

            // Password logs table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS password_logs (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  account_id INTEGER NOT NULL," +
                "  viewer_id INTEGER NOT NULL," +
                "  viewer_name TEXT," +
                "  action_type TEXT DEFAULT 'view'," +
                "  viewed_at TEXT DEFAULT (datetime('now','localtime'))" +
                ")"
            );

            // Login logs table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS login_logs (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  user_id INTEGER NOT NULL," +
                "  username TEXT," +
                "  display_name TEXT," +
                "  ip_address TEXT," +
                "  login_time TEXT DEFAULT (datetime('now','localtime'))," +
                "  FOREIGN KEY (user_id) REFERENCES users(id)" +
                ")"
            );

            // Account change logs table (records all create/update/delete operations on accounts)
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS account_logs (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  account_id INTEGER NOT NULL," +
                "  action_type TEXT NOT NULL," +
                "  field_name TEXT," +
                "  old_value TEXT," +
                "  new_value TEXT," +
                "  operator_id INTEGER NOT NULL," +
                "  operator_name TEXT," +
                "  created_at TEXT DEFAULT (datetime('now','localtime'))" +
                ")"
            );

            // Password history table (saves old password when password is changed)
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS password_history (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  account_id INTEGER NOT NULL," +
                "  password_encrypted TEXT," +
                "  password_iv TEXT," +
                "  password_tag TEXT," +
                "  changed_by INTEGER NOT NULL," +
                "  changed_name TEXT," +
                "  changed_at TEXT DEFAULT (datetime('now','localtime'))" +
                ")"
            );

            // Account managers table (which users can manage which accounts)
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS account_managers (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  account_id INTEGER NOT NULL," +
                "  user_id INTEGER NOT NULL," +
                "  assigned_by INTEGER NOT NULL," +
                "  assigned_at TEXT DEFAULT (datetime('now','localtime'))," +
                "  UNIQUE(account_id, user_id)" +
                ")"
            );

            // User groups table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS user_groups (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  name TEXT UNIQUE NOT NULL," +
                "  description TEXT," +
                "  created_at TEXT DEFAULT (datetime('now','localtime'))" +
                ")"
            );

            // User group members table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS user_group_members (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  group_id INTEGER NOT NULL," +
                "  user_id INTEGER NOT NULL," +
                "  UNIQUE(group_id, user_id)," +
                "  FOREIGN KEY (group_id) REFERENCES user_groups(id)," +
                "  FOREIGN KEY (user_id) REFERENCES users(id)" +
                ")"
            );

            // Account group managers table (assign accounts to groups)
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS account_group_managers (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  account_id INTEGER NOT NULL," +
                "  group_id INTEGER NOT NULL," +
                "  assigned_by INTEGER NOT NULL," +
                "  assigned_at TEXT DEFAULT (datetime('now','localtime'))," +
                "  UNIQUE(account_id, group_id)," +
                "  FOREIGN KEY (account_id) REFERENCES accounts(id)," +
                "  FOREIGN KEY (group_id) REFERENCES user_groups(id)" +
                ")"
            );

            // Account assignments table (user-account expiry tracking)
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS account_assignments (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  account_id INTEGER NOT NULL," +
                "  user_id INTEGER NOT NULL," +
                "  expiry_date TEXT NOT NULL," +
                "  created_at TEXT DEFAULT (datetime('now','localtime'))," +
                "  FOREIGN KEY (account_id) REFERENCES accounts(id)," +
                "  FOREIGN KEY (user_id) REFERENCES users(id)" +
                ")"
            );

            // Platforms table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS platforms (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  name TEXT UNIQUE NOT NULL," +
                "  created_at TEXT DEFAULT (datetime('now','localtime'))" +
                ")"
            );

            // Insert default platforms if table is empty
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM platforms")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String[] defaults = {
                        "主域名管理", "公司邮箱", "小程序账号", "公司微信账号",
                        "运维服务器", "VPN账号", "OA账号", "财务账号"
                    };
                    for (String name : defaults) {
                        stmt.executeUpdate("INSERT INTO platforms (name) VALUES ('" + name + "')");
                    }
                    System.out.println("Default platforms inserted.");
                }
            }
            // Ensure 运维服务器 exists (for existing DBs)
            try {
                stmt.executeUpdate("INSERT OR IGNORE INTO platforms (name) VALUES ('运维服务器')");
            } catch (SQLException ignored) {}

            // Add valid_days column to applications (migration for existing DBs)
            try {
                stmt.executeUpdate("ALTER TABLE applications ADD COLUMN valid_days INTEGER DEFAULT 7");
                System.out.println("Added valid_days column to applications table.");
            } catch (SQLException ignored) {}

            // Add sub_type column to accounts (migration for existing DBs)
            try {
                stmt.executeUpdate("ALTER TABLE accounts ADD COLUMN sub_type TEXT");
                System.out.println("Added sub_type column to accounts table.");
            } catch (SQLException ignored) {}

            // Add project column to accounts (migration for existing DBs)
            try {
                stmt.executeUpdate("ALTER TABLE accounts ADD COLUMN project TEXT");
                System.out.println("Added project column to accounts table.");
            } catch (SQLException ignored) {}

            // Add action_type column to password_logs (migration for existing DBs)
            try {
                stmt.executeUpdate("ALTER TABLE password_logs ADD COLUMN action_type TEXT DEFAULT 'view'");
                System.out.println("Added action_type column to password_logs table.");
            } catch (SQLException ignored) {}

            System.out.println("Database initialized successfully: " + dbPath);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
}
