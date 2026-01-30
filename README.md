# 📚 BookRecSys - AI 智能图书推荐全栈平台

![GitHub release (latest by date)](https://img.shields.io/github/v/release/Tan-zhen-yu/bookRecSys?color=orange&logo=github)
![Java](https://img.shields.io/badge/Java-17-blue?logo=openjdk)
![Python](https://img.shields.io/badge/Python-3.9+-blue?logo=python)

> **🚀 极速体验**：[![Download Zip](https://img.shields.io/badge/立即下载-v1.0_一键运行包-brightgreen?style=for-the-badge&logo=windows)](https://github.com/Tan-zhen-yu/bookRecSys/releases/download/BookRecSys/Releasev1.0.zip)
>
> *(注：下载后解压，确保 MySQL 已启动并导入 sql，双击「一键启动.bat」即可自动运行)*

---

## 🏗️ 项目架构简介
本项目采用 **Java (Spring Boot 3)** + **Python (Flask/AI)** + **Vue 3 (TS)** 的异构微服务架构，模拟大厂真实生产环境。
- **业务中台**：处理用户、图书、评论等核心业务逻辑。
- **算法中台**：独立运行 Python 推荐引擎，支持协同过滤算法。
- **网关转发**：利用 Nginx 实现前后端分离部署与请求分发。

---

## 🖼️ 界面预览
| 首页展示 | 图书详情 |
| :---: | :---: |
| ![主页](./docs/主页.png) | ![详情页](./docs/详情页.png) |

---

## 📥 获取项目 (Two Ways)

### 方案 A：面向使用者 (极速部署)
这是最简单的运行方式，无需配置复杂的开发环境：
1. **下载**：点击顶部的 [立即下载] 按钮或前往 [Releases 页面](https://github.com/Tan-zhen-yu/bookRecSys/releases)。
2. **数据库**：在 MySQL 中创建 `book_rec_db` 并运行 `init.sql`。
3. **启动**：双击运行 `一键启动.bat`。

### 方案 B：面向开发者 (源码调试)
如果你想进行二次开发，请参考以下配置：

#### 🛠️ 1. 环境自检 (Prerequisites)
- **Java**: JDK 17+
- **Node.js**: v18.0+
- **Python**: 3.9+ (依赖: `flask`, `flask-cors`, `requests`)
- **MySQL**: 8.0

#### 📂 2. 目录结构导航
```text
bookRecSys
├── backend/                # Java 后端源码 (Port: 8080)
├── book-rec-frontend/      # 前端源码 (Port: 5173)
├── python_service/         # 算法服务
│   ├── web.py              # 算法 API 网关 (Port: 5001)
│   └── recommend_service.py # 核心推荐引擎 (Port: 5000)
├── start_services.bat      # 源码环境一键启动脚本
└── database.sql            # 数据库初始化脚本