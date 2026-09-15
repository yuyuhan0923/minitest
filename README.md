项目演示
点击观看完整演示视频（B 站）：https://www.bilibili.com/video/BV17MeJ6uEZD/?spm_id_from=333.1387.list.card_archive.click&vd_source=6502eb51fefb8a7f0e166d4872f4e459

项目简介

本项目是面向 AI 算法教育培训领域的微信小程序获客系统，采用前后端分离架构。以 AI 能力测评作为流量钩子，完成用户拉新、能力分层、线索留资与转化，配套 PC 端 Vue3 管理后台以及小程序内嵌管理面板，实现全链路用户行为数据采集、运营指标统计、题库与课程运营配置，形成拉新‑激活‑留资‑转化‑数据优化完整商业闭环。

技术栈：
- 小程序端：微信原生 WXML/WXSS/JavaScript、微信云开发（云函数、MongoDB）
- 后端：SpringBoot 3.2、MyBatis‑Plus、SpringSecurity、JWT、BCrypt
- 管理后台：Vue3、Vite5、Element‑Plus、Axios、Pinia、Vue‑Router4
- 数据库：MySQL8.0、微信云开发 MongoDB
- 运行环境：JDK17、Node16

主要功能
用户小程序端
1. 微信一键授权登录，支持匿名浏览
2. AI 能力测评（18 道题目，覆盖编程、数学、机器学习、深度学习、框架工具、项目实战六大维度）
3. 自动算分，生成个性化测评报告、能力等级与学习路径建议
4. 留资领取学习资料、添加授课老师微信、扫码加入学习社群
5. 课程展示、试听跳转、个人中心查看历史测评记录

管理后台（PC Vue3 + 小程序内嵌管理面板）
1. 数据看板：访问量、注册量、测评完成数、留资量、购课点击、转化率核心指标统计
2. 用户管理、测评记录查询筛选
3. 题库维护管理、课程信息配置
4. 老师微信、学习群活码配置、线索留资信息查看（手机号脱敏）

部署条件
1. JDK17、MySQL8.0、Node16 环境
2. 微信小程序账号，开通云开发环境
3. Nginx（用于部署 Vue 后台静态资源与接口反向代理）

快速部署步骤
1. 数据库：执行`init.sql`初始化 MySQL 数据库`ai_assessment`，导入初始管理员与题库数据；微信云开发创建对应业务集合，导入题目数据。
2. SpringBoot 后端：`mvn clean package -DskipTests`打包 jar 包，启动服务监听 8083 端口。
3. Vue 管理后台：`npm run build`打包 dist，Nginx 部署静态页面，配置`/api`反向代理指向后端 8083 端口。
4. 微信小程序：填入 AppID 与云环境 ID，配置合法域名，上传代码提交发布。

Project Demo
Click to watch the full demo video (Bilibili): https://www.bilibili.com/video/BV17MeJ6uEZD/?spm_id_from=333.1387.list.card_archive.click&vd_source=6502eb51fefb8a7f0e166d4872f4e459

Project Introduction

This project is a WeChat Mini‑Program customer‑acquisition system for AI algorithm education and training, built on a separation‑of‑frontend‑and‑backend architecture. Using AI capability assessment as a traffic hook, it realizes user acquisition, capability stratification, lead collection and conversion. Equipped with a PC‑side Vue3 management background and an embedded management panel inside the mini‑program, the system collects full‑link user behavior data, computes operational KPIs, and supports configuration of question banks and courses. It forms a complete business loop: **Acquisition‑Activation‑Lead Generation‑Conversion‑Data‑Driven Optimization**.

Tech Stack:
- Mini‑Program: Native WeChat WXML/WXSS/JavaScript, WeChat Cloud Development (Cloud Functions, MongoDB)
- Backend: SpringBoot 3.2, MyBatis‑Plus, SpringSecurity, JWT, BCrypt
- Admin Dashboard: Vue3, Vite5, Element‑Plus, Axios, Pinia, Vue‑Router4
- Database: MySQL8.0, WeChat Cloud MongoDB
- Runtime: JDK17, Node16

Core Features
Mini‑Program (User Side)
1. One‑click WeChat OAuth login, support anonymous browsing
2. AI capability assessment with 18 questions covering six dimensions: programming foundation, mathematics, machine learning, deep learning, framework tools, project practice
3. Automatic score calculation, generate personalized assessment report, capability level and learning suggestions
4. Collect user leads for learning materials, add tutor WeChat, join study groups via QR‑code
5. Course display, trial‑lesson redirection, personal center for historical assessment records

Admin System (PC Vue3 + Mini‑Program Embedded Panel)
1. Data dashboard: real‑time statistics of visits, registrations, completed assessments, leads, purchase clicks and conversion rate
2. User management, filter & query assessment records
3. Question bank maintenance, course information configuration
4. Configure tutor WeChat QR‑code & group QR‑code; view desensitized lead contact information

Prerequisites for Deployment
1. Runtime environment: JDK17, MySQL8.0, Node16
2. WeChat Mini‑Program account with Cloud Development enabled
3. Nginx (for static resource hosting and API reverse proxy)

Deployment Guide
1. Database: Run `init.sql` to initialize MySQL database `ai_assessment` with default administrator accounts and sample questions. Create required collections in WeChat Cloud Development and import question data.
2. SpringBoot Backend: Build jar package with `mvn clean package -DskipTests`, start service listening on port 8083.
3. Vue Admin Dashboard: Execute `npm run build` to generate `dist` static files. Deploy via Nginx and set `/api` reverse proxy forwarding to backend port 8083.
4. WeChat Mini‑Program: Fill in AppID and cloud environment ID, configure authorized domain names, upload source code and submit for official release.
