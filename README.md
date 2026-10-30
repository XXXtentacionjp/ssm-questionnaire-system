# 企业级开发框架 · 问卷系统（SSM）

基于 Spring + Spring MVC + MyBatis 的问卷系统后端，对外只提供 RESTful JSON 接口。

当前进度：**考核要求里的「基础需求」已全部实现并有集成测试覆盖**，「中等需求」和「进阶需求」尚未开始，
本文末尾的 [已知限制](#已知限制) 逐条列出了还没做的部分，方便接手的人直接看到缺口。

---

## 技术栈

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| JDK | 8+ | 编译目标锁到 1.8，本仓库在 JDK 17 上构建通过 |
| Maven | 3.6+ | `war` 打包 |
| Spring | 5.3.21 | IoC / DI，XML 配置 |
| Spring MVC | 5.3.21 | REST 接口层 |
| MyBatis | 3.5.10 | 数据持久化，Mapper XML 写法 |
| mybatis-spring | 2.0.7 | MyBatis 与 Spring 整合 |
| Druid | 1.2.20 | 数据库连接池 |
| MySQL | 5.7 / 8.0 | 生产数据库 |
| H2 | 2.2.224 | 仅测试作用域，跑 MySQL 兼容模式 |
| Jackson | 2.13.4 | JSON 序列化 |
| JUnit 4 + spring-test | 4.13.2 / 5.3.21 | MockMvc 接口测试 |
| Tomcat | 9+ | 运行容器 |

## 目录结构

```
src/main/java/com/ssm/questionnaire
├── common        统一响应体 Result / PageResult、业务异常、全局异常处理、常量
├── controller    REST 接口层，只做参数接收和结果封装
├── service       业务接口
│   └── impl      业务实现，事务边界在这里
├── mapper        MyBatis Mapper 接口
├── entity        与数据库表一一对应的实体
├── dto           入参对象
├── vo            出参对象，实体不会直接吐给前端
├── interceptor   登录令牌校验
└── util          口令摘要、令牌生成
src/main/resources
├── applicationContext.xml   数据源、事务、MyBatis、service 扫描
├── spring-mvc.xml           MVC、JSON 转换器、拦截器注册
├── mybatis-config.xml       MyBatis 全局设置
├── jdbc.properties          MySQL 连接参数
├── mapper/*.xml             SQL 语句
└── sql/schema-mysql.sql     建库建表 + 演示数据
src/main/webapp/WEB-INF/web.xml
src/test
├── java/...      基类 BaseIntegrationTest + 各层测试
└── resources     jdbc.properties（H2）、schema-h2.sql
```

分层约定：controller 不写业务，service 不碰 `HttpServletRequest`，mapper 只负责 SQL。

## 环境要求

- JDK 8 及以上
- Maven 3.6 及以上
- MySQL 5.7 / 8.0（**只在手动跑接口时才需要**，`mvn test` 用 H2 内存库）
- Tomcat 9

## 快速开始

### 1. 初始化数据库

```bash
mysql -uroot -p < src/main/resources/sql/schema-mysql.sql
```

脚本会重建 `questionnaire_db`，建 7 张表并插入一个演示问卷。

### 2. 改连接参数

编辑 `src/main/resources/jdbc.properties`，把用户名口令换成本机的：

```properties
jdbc.url=jdbc:mysql://localhost:3306/questionnaire_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
jdbc.username=root
jdbc.password=你的口令
```

真实口令不要提交到仓库，可以另建 `jdbc-local.properties`（已在 `.gitignore` 里）。

### 3. 打包并部署

```bash
mvn clean package
```

把 `target/questionnaire.war` 丢进 `$TOMCAT_HOME/webapps/`，启动 Tomcat 后访问：

```
http://localhost:8080/questionnaire/          # 看到部署成功提示页即为正常
http://localhost:8080/questionnaire/api/...   # 接口统一前缀
```

### 4. 不装 MySQL 的验证方式

```bash
mvn test
```

测试用 `src/test/resources/jdbc.properties` 指向 H2 内存库（`MODE=MySQL`），
该文件与主配置同名，surefire 会优先加载 test 目录下的版本，所以不需要本机有 MySQL。

## 接口清单

统一响应格式：

```json
{ "code": 200, "message": "success", "data": {}, "timestamp": 1746000000000 }
```

`code` 取值：200 成功、400 参数错误、401 未登录或令牌失效、403 无权限、404 资源不存在、500 系统异常。
业务出错时 HTTP 状态码与 `code` 保持一致，方便前端拦截器直接判断。

鉴权方式：登录接口返回 `data.token`，之后所有带鉴权的接口都要在请求头里带上

```
X-Token: <登录返回的 token>
```

令牌有效期 120 分钟，落在 `t_user_token` 表里，过期后需要重新登录。

| # | 方法 | 路径 | 鉴权 | 说明 |
| --- | --- | --- | --- | --- |
| 1 | POST | `/api/user/register` | 否 | 注册 |
| 2 | POST | `/api/user/login` | 否 | 登录，返回 token |
| 3 | GET | `/api/user/info` | 是 | 当前登录用户信息 |
| 4 | POST | `/api/questionnaire/create` | 是 | 创建问卷，题目和选项一次提交 |
| 5 | GET | `/api/questionnaire/list?pageNum=1&pageSize=10` | 是 | 我创建的问卷，分页 |
| 6 | GET | `/api/questionnaire/{id}` | 是 | 问卷详情，含题目和选项 |
| 7 | PUT | `/api/questionnaire/update` | 是 | 修改问卷，仅创建者可改 |
| 8 | DELETE | `/api/questionnaire/delete/{id}` | 是 | 删除问卷，仅创建者可删 |

### 请求示例

注册并登录：

```bash
curl -X POST http://localhost:8080/questionnaire/api/user/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"demo","password":"123456","nickname":"演示账号"}'

curl -X POST http://localhost:8080/questionnaire/api/user/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"demo","password":"123456"}'
```

创建问卷（`token` 换成上一步返回的值）：

```bash
curl -X POST http://localhost:8080/questionnaire/api/questionnaire/create \
  -H 'Content-Type: application/json' \
  -H 'X-Token: <token>' \
  -d '{
    "title": "Java 学习情况调查",
    "description": "了解大家学习 Java 的常见困难",
    "status": 1,
    "questions": [
      { "content": "你觉得最难的部分是什么？", "type": "SINGLE",
        "options": [ {"content": "集合框架"}, {"content": "多线程"}, {"content": "SSM 框架"} ] },
      { "content": "你平时通过哪些方式学习？", "type": "MULTIPLE",
        "options": [ {"content": "看视频"}, {"content": "刷算法题"}, {"content": "读源码"} ] }
    ]
  }'
```

演示账号：`demo / 123456`（口令按 `MD5(明文 + 固定盐)` 存，SQL 里已给出对应密文）。

## 数据库设计

| 表 | 说明 | 关键约束 |
| --- | --- | --- |
| `t_user` | 用户 | `username` 唯一 |
| `t_user_token` | 登录令牌 | `token` 唯一，外键指向用户，用户删除时级联 |
| `t_questionnaire` | 问卷 | 外键指向创建者，`ON DELETE CASCADE` |
| `t_question` | 题目 | `type` 为 SINGLE / MULTIPLE，外键级联到问卷 |
| `t_option` | 选项 | 外键级联到题目 |
| `t_answer` | 答卷 | `(questionnaire_id, user_id)` 唯一，即一人一份 |
| `t_answer_detail` | 答卷明细 | 一行一个被选中的选项 |

`src/main/resources/sql/schema-mysql.sql` 与 `src/test/resources/schema-h2.sql` 是同一套表结构的两个方言版本，
**改表必须两边一起改**，否则 `mvn test` 会直接报错。

## 测试

| 测试类 | 覆盖点 |
| --- | --- |
| `UserMapperTest` | 主键回填、下划线到驼峰的列映射 |
| `UserServiceTest` | 注册校验、重复用户名、登录签发令牌、令牌解析、用户不存在 |
| `QuestionnaireServiceTest` | 题目选项落库、发布前校验、分页、改标题不动题目、整体替换题目、越权修改、级联删除 |
| `UserControllerTest` | 注册登录取用户信息、无令牌 401、口令错误 401、重复注册 400 |
| `QuestionnaireControllerTest` | 完整 CRUD 流、分页参数、未登录 401、非法题目 400、他人问卷不出现在我的列表且改删被拒 |

全部继承 `BaseIntegrationTest`，每个用例执行前重建一次 H2 表结构，用例之间互不影响。

```bash
mvn test                        # 全部
mvn test -Dtest=QuestionnaireControllerTest   # 单个类
```

## 已知限制

以下是有意留下的未完成部分，也是下一轮要做的功能：

1. **列表只看得到自己创建的问卷**，没有公开问卷列表，无法浏览和回答他人问卷。
2. **没有标签/类型**：既没有标签表，也没有按类型筛选的接口。
3. **搜索和排序没做**：`list` 只接受 `pageNum`、`pageSize`，不支持按标题/标签模糊搜索，也不支持 `sort`、`order` 参数，排序写死按创建时间倒序。
4. **回答功能没做**：`t_answer`、`t_answer_detail` 只建了表，提交答案和选项分布统计、回答人数统计都还没有接口。
5. **用户注销没做**：没有删除用户及其问卷的接口（目前只靠外键级联，走不到业务校验）。
6. **删除是物理删除**：没有回收站，没有软删除标记、恢复和彻底删除。
7. **没有提醒和定时任务**，**没有 AOP 操作日志**，**没有管理员角色**（`role` 字段已经预留，但没有使用它的鉴权逻辑）。
8. **详情接口有 N+1 查询**：先查题目，再对每道题单独查选项；列表接口的 `questionCount` 也是逐条 count。
9. **口令是 MD5 + 全局固定盐**，强度不够，应换成 BCrypt 之类的加盐慢哈希。
10. **没有参数校验框架**：`@Valid` 未引入，校验散在 service 里手写；重复用户名是「先查后插」，并发下仍有窗口。
11. **没有接口文档**：Swagger / OpenAPI 尚未接入。

## Git 约定

- `master` 只放可运行的代码，开发走 `feature/xxx` 分支，合并前保证 `mvn test` 通过。
- 提交信息用中文，格式 `模块: 做了什么`，例如 `问卷: 列表接口支持按创建时间排序`。
- 一次提交只做一件事，不要把功能、格式化和依赖升级混在同一个 commit 里。
