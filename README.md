# 企业外部账号管理系统

一个基于 **JSP + Servlet 4.0 + SQLite** 的企业级外部账号管理 Web 应用，支持账号的集中存储、分配管理、申请审批、密码审计等完整流程。

## 功能特性

### 账号管理
- **账号创建**：录入外部账号的名称、密码、平台类型、运维子类型、所属项目、部门、到期时间、连接信息、备注等
- **账号编辑**：支持逐字段修改，自动记录变更日志
- **账号删除**：级联清理管理关系和日志
- **密码加密存储**：使用 AES-256-GCM 对密码进行加密存储，支持密码历史追溯
- **平台类型管理**：支持自定义平台类型（主域名管理、公司邮箱、小程序账号等）
- **CSV 导入导出**：支持批量导入导出账号数据（含密码解密）

### 用户与权限
- **三种角色**：
  - **超级管理员 (admin)**：全部权限，系统初始化时创建
  - **管理员 (manager)**：管理被分配的账号，审批申请
  - **普通用户 (user)**：申请使用账号，查看已审批的账号
- **用户组管理**：支持用户分组，可以按组分配账号管理权限
- **权限粒度**：
  - 管理员只能看到自己被分配的账号
  - 普通用户只能看到审批通过的账号
  - 超级管理员可以看到全部

### 申请审批流程
1. 普通用户提交账号使用申请（选择使用天数，默认 7 天）
2. 管理员（分配了该账号管理权限的）审批申请
3. 审批通过后自动生成账号分配记录（含到期时间）
4. 用户在有效期内可查看密码，到期后自动失效

### 审计日志
- **密码查看日志**：记录谁在何时查看了哪个账号的密码
- **账号操作日志**：记录账号的创建、修改、删除操作，包含字段级别的变更详情
- **登录日志**：记录用户登录的 IP 和时间
- **申请日志**：记录所有申请和审批操作

### 仪表盘
- **超级管理员**：查看全系统统计数据
- **管理员**：查看自己管理的账号和审批统计
- **普通用户**：查看自己的申请和已授权账号统计

## 技术栈

| 组件 | 技术 |
|------|------|
| 后端语言 | Java 8+ |
| Web 框架 | Servlet 4.0 (javax.servlet) |
| 视图层 | JSP + JSTL |
| 数据库 | SQLite |
| 密码加密 | AES-256-GCM (账号密码) / BCrypt (用户密码) |
| 构建工具 | 批处理脚本 (build.bat) |
| 运行环境 | Tomcat 9+ |

## 数据库表结构

| 表名 | 说明 |
|------|------|
| `users` | 系统用户 |
| `accounts` | 外部账号 |
| `applications` | 使用申请 |
| `account_managers` | 用户级管理员分配 |
| `account_group_managers` | 用户组级管理员分配 |
| `account_assignments` | 账号分配（含到期时间） |
| `user_groups` | 用户组 |
| `user_group_members` | 用户组成员 |
| `platforms` | 平台类型字典 |
| `password_logs` | 密码查看日志 |
| `password_history` | 密码历史记录 |
| `account_logs` | 账号操作日志 |
| `login_logs` | 登录日志 |

## 快速开始

### 第一步：安装 JDK（Java 开发工具包）

> 如果电脑上已经有 JDK，可以跳过这一步。

1. **下载 JDK 8**
   - 访问 https://www.oracle.com/java/technologies/javase/javase8u211-later-archive-downloads.html
   - 选择 **Windows x64** 版本下载（文件名类似 `jdk-8uXXX-windows-x64.exe`）

2. **安装 JDK**
   - 双击下载的 `.exe` 文件，一路点"下一步"即可
   - **记下安装路径**，默认是 `C:\Program Files\Java\jdk1.8.0_XXX`

3. **验证安装**
   - 按键盘 `Win + R`，输入 `cmd`，回车
   - 在弹出的黑窗口中输入以下命令，按回车：
   ```
   java -version
   ```
   - 如果显示类似 `java version "1.8.0_XXX"` 的信息，说明安装成功

---

### 第二步：安装 Tomcat（Web 服务器）

1. **下载 Tomcat 9**
   - 访问 https://tomcat.apache.org/download-90.cgi
   - 在 **Core** 下面选择 `64-bit Windows zip` 下载（约 10MB）

2. **解压 Tomcat**
   - 将下载的压缩包解压到一个**没有中文和空格的路径**，例如：
     - `D:\apache-tomcat-9.0.XX`（推荐）
     - 或 `C:\tools\apache-tomcat-9.0.XX`
   - 记下这个路径，后面会用到（下面简称为 `Tomcat目录`）

3. **验证 Tomcat**
   - 进入 `Tomcat目录\bin\` 文件夹
   - 双击 `startup.bat`（会弹出一个黑窗口，不要关闭它）
   - 打开浏览器，访问 `http://localhost:8080`
   - 如果看到 Tomcat 的默认页面（一只猫的图案），说明 Tomcat 启动成功
   - 关掉黑窗口就是关闭 Tomcat

---

### 第三步：编译项目

> 把 Java 源代码（.java）转换成 Tomcat 能运行的类文件（.class）

1. **打开命令提示符**
   - 按 `Win + R`，输入 `cmd`，回车

2. **进入项目目录**
   ```
   cd C:\Users\Administrator\CodeBuddy\zhanghaoguanli
   ```

3. **执行编译**
   ```
   build.bat
   ```
   - 如果看到 `BUILD SUCCESSFUL` 的绿色文字，说明编译成功
   - 如果看到 `BUILD FAILED`，请检查 JDK 路径是否和 `build.bat` 文件中第 4 行的路径一致

   > **遇到报错怎么办？** 打开项目根目录下的 `build.bat` 文件，找到第 4 行：
   > ```
   > set JAVA_HOME=D:\Program Files\ojdkbuild\java-1.8.0-openjdk-1.8.0.302-1
   > ```
   > 把这行改成你电脑上 JDK 的实际安装路径。

4. **确认编译结果**
   - 编译成功后，打开 `web\WEB-INF\classes\com\account\` 目录
   - 如果能看到 `dao`、`model`、`servlet` 等文件夹和一堆 `.class` 文件，说明编译成功

---

### 第四步：部署到 Tomcat

> 把编译好的项目放到 Tomcat 的 webapps 目录下

1. **复制项目到 Tomcat**
   - 打开文件资源管理器，找到项目的 `web` 文件夹
   - 选中 `web` 文件夹，按 `Ctrl + C` 复制
   - 打开 Tomcat 的安装目录下的 `webapps` 文件夹（例如 `D:\apache-tomcat-9.0.XX\webapps`）
   - 按 `Ctrl + V` 粘贴
   - 将粘贴进来的 `web` 文件夹重命名为 `zhanghaoguanli`（或其他你想要的名字）

   > 最终 Tomcat 目录应该是这样的结构：
   > ```
   > D:\apache-tomcat-9.0.XX\
   > ├── bin\              （Tomcat 启动程序）
   > ├── webapps\          （存放所有 Web 应用）
   > │   └── zhanghaoguanli\  （← 我们的项目）
   > │       ├── login.jsp
   > │       ├── dashboard.jsp
   > │       ├── WEB-INF\
   > │       └── ...
   > ```

2. **（可选）配置加密密钥**
   - 用记事本打开 `Tomcat目录\webapps\zhanghaoguanli\WEB-INF\web.xml`
   - 找到下面的内容：
   ```xml
   <context-param>
       <param-name>cryptoKey</param-name>
       <param-value>你的Base64编码256位密钥</param-value>
   </context-param>
   ```
   - **初学者可以先不改这个配置**，系统会自动生成一个临时密钥。但重启 Tomcat 之前保存的密码会无法解密，请知悉。

---

### 第五步：启动 Tomcat 并初始化系统

1. **启动 Tomcat**
   - 进入 `Tomcat目录\bin\` 文件夹
   - 双击 `startup.bat`
   - 保持黑窗口打开（关闭它就等于关闭服务器）

2. **初始化系统（仅首次需要）**
   - 打开浏览器
   - 在地址栏输入：`http://localhost:8080/zhanghaoguanli/init`
   - 回车后等待页面加载
   - 如果看到 **"初始化成功！"** 的大标题，说明系统初始化完成
   - 页面上会显示默认的登录账号和密码

3. **登录系统**
   - 在浏览器输入：`http://localhost:8080/zhanghaoguanli/login`
   - 使用下面的账号登录：
     - **管理员账号**：用户名 `admin`，密码 `admin123`
     - **测试用户**：用户名 `user`，密码 `user123`

4. **登录成功后**
   - 会看到系统的**仪表盘**页面
   - 左侧有导航菜单：账号管理、用户管理、审批管理等
   - 尽情探索吧！

---

### 第六步：修改代码后如何更新

> 如果你修改了项目的代码，需要重新部署才能看到效果

**情况一：只改了 JSP 页面（.jsp 文件）**
- 把修改后的 `.jsp` 文件复制到 `Tomcat目录\webapps\zhanghaoguanli\` 对应目录下覆盖
- 刷新浏览器页面即可，**不需要重启 Tomcat**

**情况二：改了 Java 源文件（.java 文件）**
```
# 1. 在项目根目录重新编译
cd C:\Users\Administrator\CodeBuddy\zhanghaoguanli
build.bat

# 2. 关闭 Tomcat（关闭 startup.bat 的黑窗口）

# 3. 把整个 web 目录复制到 Tomcat 的 webapps 下覆盖
# 4. 重启 Tomcat（双击 startup.bat）
```

---

### 第七步：初学者常见问题

| 现象 | 原因 | 解决方法 |
|------|------|----------|
| **浏览器输入地址后显示"无法访问此网站"** | Tomcat 没启动 | 双击 `Tomcat目录\bin\startup.bat` 启动 |
| **输入地址后显示 404 页面** | 应用名不对 | 地址应为 `http://localhost:8080/应用名/`，应用名就是 `webapps` 下的文件夹名 |
| **访问 /init 显示空白页** | 没编译或编译失败 | 重新运行 `build.bat`，看有没有红色报错 |
| **访问任何页面都报 500 错误** | Java 类文件有问题 | 重新编译并重启 Tomcat |
| **中文显示为乱码或问号** | 编码问题 | 确认 `web.xml` 中 `EncodingFilter` 的 `encoding` 参数是 `UTF-8` |
| **登录后提示"密码错误"** | 默认密码被修改了 | 重新初始化（删除 webapps 下的项目，重新部署） |
| **启动 Tomcat 的黑窗口一闪就没了** | 端口被占用或 Java 没装好 | 检查 `java -version` 是否正常；检查 8080 端口是否被其他程序占用 |

### 项目目录结构

```
zhanghaoguanli/
├── src/                          # Java 源代码
│   └── com/account/
│       ├── dao/                  # 数据访问层
│       │   ├── DBUtil.java       # 数据库连接与初始化
│       │   ├── AccountDAO.java   # 账号 CRUD
│       │   ├── UserDAO.java      # 用户 CRUD
│       │   ├── ApplicationDAO.java      # 申请审批
│       │   ├── AccountAssignmentDAO.java # 账号分配
│       │   ├── AccountManagerDAO.java    # 管理员分配
│       │   ├── AccountLogDAO.java        # 操作日志
│       │   ├── PasswordLogDAO.java       # 密码查看日志
│       │   ├── PasswordHistoryDAO.java   # 密码历史
│       │   ├── LoginLogDAO.java          # 登录日志
│       │   ├── PlatformDAO.java          # 平台类型
│       │   └── UserGroupDAO.java         # 用户组
│       ├── model/                # 实体模型
│       ├── servlet/              # 控制器层 (30个 Servlet)
│       ├── filter/               # 过滤器
│       └── util/                 # 工具类
├── web/                          # Web 资源
│   ├── login.jsp                 # 登录页
│   ├── dashboard.jsp             # 仪表盘
│   ├── logs.jsp                  # 日志查看
│   ├── accounts/                 # 账号管理页面
│   ├── applications/             # 申请管理页面
│   ├── users/                    # 用户管理页面
│   ├── groups/                   # 用户组页面
│   └── platforms/                # 平台类型页面
├── lib/                          # 依赖 JAR
├── build.bat                     # 编译脚本
└── README.md                     # 本文件
```

## 操作说明

### 1. 系统初始化
- **首次使用**：访问 `/init` 路径完成初始化，创建默认管理员和测试用户
- **已初始化**：登录页会自动隐藏初始化入口

### 2. 用户管理 (仅超级管理员)
- **创建用户**：设置用户名、显示名、角色、部门，可同时选择所属用户组
- **编辑用户**：可修改信息、重设密码、调整角色、同步用户组成员
- **删除用户**：禁止删除超级管理员账号和当前登录用户

### 3. 平台类型管理 (仅超级管理员)
- 管理账号的平台分类（如"运维服务器"、"公司邮箱"等）
- 有账号关联的平台不可直接删除

### 4. 账号管理
- **创建账号**：填写账号名称、密码、平台类型等信息，密码自动加密存储
- **分配管理**：创建者自动成为该账号的管理员
- **认证管理**：
  - 每个账号可以分配多个管理员（个人级别）
  - 也可以分配给整个用户组（组级别）
  - 组内所有成员自动获得该账号的管理权限
- **查看密码**：
  - 管理员可查看自己管理的账号密码
  - 普通用户仅可查看已审批通过的账号密码（有效期内）
- **导入导出**：支持 CSV 格式批量操作

### 5. 申请审批
- **提交申请**：普通用户在账号详情页提交使用申请，设置使用天数
- **审批管理**（管理员）：
  - 只能看到自己有管理权限的账号的申请
  - 支持"仅显示可用"筛选（排除已过期分配）
- **审批通过**：自动生成账号分配记录，用户获得有效期内的访问权限
- **审批驳回**：可填写驳回原因

### 6. 日志审计
- **密码查看日志**：记录每次密码查看和导出操作
- **账号操作日志**：记录创建、修改、删除操作及字段级变更
- **申请日志**：包含申请人、审批人、审批结果
- **登录日志**：记录登录 IP 和时间（仅超级管理员可查看）
- **搜索过滤**：支持按关键词、日期范围、操作类型筛选

### 7. 仪表盘
- 显示账号总数、待审批数、申请总数、日志总数、用户数
- 数据范围根据角色自动调整：
  - 超级管理员：全局统计
  - 管理员：自己管理的账号范围
  - 普通用户：自己的申请和授权范围

## 安全特性

- 账号密码使用 AES-256-GCM 加密存储
- 用户密码使用 BCrypt 哈希存储
- 操作权限严格按角色控制
- 所有敏感操作记录审计日志
- 密码查看需要有效授权（管理员权限或有效分配记录）
- 完善的管理员分配机制（个人和用户组）

## 编译说明

```bash
# 使用批处理脚本编译
build.bat

# 或手动编译
javac -cp "lib/*;web/WEB-INF/lib/*" -d web/WEB-INF/classes src/com/account/**/*.java
```

## 依赖列表

位于 `lib/` 目录：
- `servlet-api.jar` - Servlet 4.0 API
- `jstl.jar` - JSTL 标签库
- `standard.jar` - JSTL 标准实现
- `sqlite-jdbc.jar` - SQLite JDBC 驱动
- `jbcrypt.jar` - BCrypt 密码哈希

## 注意事项

- 数据库文件位于 `web/WEB-INF/accountant.db`
- 加密密钥通过 `web.xml` 的 `cryptoKey` 上下文参数配置
- 生产环境部署时请务必修改默认密码并设置自定义加密密钥
- 删除用户组前请确认该组没有被分配到账号管理权限
