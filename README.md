# 📚 BookRecSys - AI 智能图书推荐全栈平台

> **🔥 快速体验**：如果你只想直接运行系统，请 **[点击此处前往 Release 页面下载一键运行包]** > (无需配置前端环境，双击即可运行)

---
## 🏗️ 项目架构简介
本项目采用 **Java (Spring Boot 3)** + **Python (Flask/AI)** + **Vue 3 (TS)** 的异构微服务架构。
- **业务中台**：处理用户、图书、评论等核心逻辑。
- **算法中台**：独立运行 Python 推荐引擎，支持协同过滤。
- **网关转发**：利用 Nginx 实现前后端分离部署与负载均衡。

---

## 📥 获取项目 (Two Ways)

### 方案 A：面向使用者 (推荐)
1. 前往仓库右侧的 [Releases](https://github.com/Tan-zhen-yu/bookRecSys/releases) 页面。
2. 下载最新版本的 `BookRecSys_v1.0_Windows.zip`。
3. 参考包内的说明，使用 `一键启动.bat` 极速运行。

### 方案 B：面向开发者 (源码编译)
如果你想修改代码或进行二次开发，请参考以下步骤：

#### 🛠️ 1. 环境自检 (Prerequisites)
- **Java**: JDK 17+
- **Node.js**: v18.0+
- **Python**: 3.9+ (需安装 flask, flask-cors, requests)
- **MySQL**: 8.0

#### 📂 2. 目录结构导航
```text
bookRecSys
├── backend/                # Java 后端源码 (Port: 8080)
├── book-rec-frontend/      # 前端源码 (Port: 5173)
├── python_service/         # 算法服务
│   ├── web.py              # API 网关 (Port: 5001)
│   └── recommend_service.py # 推荐引擎 (Port: 5000)
├── start_services.bat      # 源码环境一键启动脚本
└── database.sql            # 数据库初始化脚本
