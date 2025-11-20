# lqq-ai-agent 后端架构设计（初版）

> 本文档基于当前工程代码结构，并参考 langchain4j-aideepin 的成熟架构，总结和规划 `lqq-ai-agent` 的后端结构，作为后续实现通用智能助手 / 多智能体功能的蓝图。后续可在此文档基础上持续补充和修正。

---

## 一、工程整体结构

当前后端采用多模块 Maven + Spring Boot 架构，整体与 aideepin 的分层类似：

- **根工程 `lqq-ai-agent`**
  - 统一依赖和版本管理，聚合各子模块。

- **子模块**
  - **`lqq-common`**：
    - 核心域模型与通用能力模块。
    - 包含：实体、DTO、Service、LangChain4j 集成（模型配置、工具）、用户上下文、统一响应、异常处理等。
  - **`lqq-chat`**：
    - 面向「用户端 Web」的 REST API 层。
    - 包含：用户注册登录、应用（智能体）管理、聊天接口、代码生成接口等。
  - **`lqq-admin`**：
    - 面向「管理端 Web」的 REST API 层。
    - 当前包含：后台应用（智能体）管理、后台用户管理等，后续可扩展为完整的运营后台。
  - **`lqq-bootstrap`**：
    - 聚合启动模块，只有一个 Spring Boot 启动类 `LqqAiAgentApplication`，依赖 `lqq-chat` 和 `lqq-admin`，对外暴露所有 API。

依赖关系规划如下：

```text
           +-----------+
           | lqq-chat  |
           +-----------+
                 ^
                 |
+-----------+    |
| lqq-admin |    |
+-----------+    |
      ^          |
      |          |
   +------------------+
   |    lqq-common    |
   +------------------+
            ^
            |
     +-------------------+
     |  lqq-bootstrap    |
     +-------------------+
```

- 所有核心领域逻辑、与 LangChain4j 相关的配置、工具等 **集中在 `lqq-common`**。
- `lqq-chat` / `lqq-admin` 只负责 API 暴露和基础校验，尽量保持 Controller 薄、Service 厚。
- `lqq-bootstrap` 统一启动，方便部署与运维。

---

## 二、`lqq-common` 模块结构与职责

`lqq-common` 是整个系统的核心模块，目前主要包结构如下：

- `annotation`：
  - `AuthCheck` 等自定义注解，用于权限校验。

- `aop`：
  - `AuthInterceptor`：基于注解和用户上下文的权限拦截器。

- `common`：
  - `BaseResponse`、`ResultUtils`：统一响应封装与工具。
  - `PageRequest`、`DeleteRequest`：通用请求 DTO。

- `config`：
  - `CorsConfig`：跨域配置。
  - `RedisConfig`：Redis 相关配置。
  - `SecurityConfig`：基础安全配置（如密码加密等）。
  - `WebConfig` + `interceptor.UserContextInterceptor`：
    - 注册用户上下文拦截器，解析登录态并写入 `UserContext`。
  - （预留）`config.mysql`：数据库配置占位，可后续补充多数据源等。

- `constant`：
  - `AiConstant`：AI 相关常量。
  - `AppConstant`：应用（智能体）相关常量。
  - `UserConstant`：用户业务相关常量。

- `exception`：
  - `BusinessException`、`ErrorCode`、`GlobalExceptionHandler`、`ThrowUtils`：统一业务异常与全局异常处理。

- `langchain4j`：
  - **LangChain4j 集成与 AI Service 能力的核心包。**
  - 当前主要类：
    - `LangChain4jConfig`：
      - 配置基础的 ChatModel / StreamingChatModel。
      - 封装 DashScope / 其他模型平台的适配信息。
    - `DashScopeChatLanguageModel`：对 DashScope Chat 模型的封装（旧接口风格，已在向新接口迁移）。
    - `DashScopeStreamingChatModel`：对 DashScope 流式模型的封装，实现 `StreamingChatModel` 接口。
    - `ChatMemoryTool`、`FileOperationTool`、`HtmlTool` 等：
      - 作为 LangChain4j 工具（Tool），用于代码生成、文件操作、HTML 处理等场景。
    - `HtmlAssistant`、`UniversalAssistant`：
      - 示例/通用 AI 服务接口，用于封装「助手」能力。
    - `SimpleAgentController`（暂时仍在 common 下）：
      - simple-agent 示例入口，后续可迁移到 `lqq-chat` 模块的 controller。

- `mapper`：
  - `UserMapper`、`AppMapper`、`AiChatMemoryMapper`：MyBatis 映射接口。

- `model`：
  - `entity`：
    - `User`：用户实体。
    - `App`：应用（智能体）实体，是当前「智能体模板」的核心数据结构，类似 aideepin 的 `ConversationPreset`。
    - `AiChatMemory`：存储会话 / 记忆相关信息。
  - `dto`：
    - `user.*`：注册、登录、查询、更新等用户相关请求 DTO。
    - `app.*`：
      - `AppAddRequest`、`AppUpdateMyRequest`、`AppAdminUpdateRequest` 等：应用创建/编辑请求对象。
    - `CodeGenResult`、`HtmlCodeResult`、`MultiFileCodeResult` 等：代码生成场景的返回结构。
  - `enums`：
    - `UserRoleEnum`、`UserStatusEnum`：用户角色和状态。
    - `CodeGenTypeEnum` 等：代码生成类型枚举。
  - `vo`：
    - `LoginUserVO`、`UserVO`、`AppVO`：对外返回的精简视图对象。

- `service`：
  - 顶层接口：
    - `UserService`：用户业务。
    - `AppService`：应用（智能体）业务核心接口。
    - `AiChatMemoryService`：会话记忆存储与查询。
    - `AiCodeGeneratorService`：AI 代码生成服务。
  - `impl` 实现包：
    - `UserServiceImpl`、`AppServiceImpl`、`AiChatMemoryServiceImpl`、`LangChain4jCodeGeneratorService` 等。
  - `facade`：
    - `AiCodeGeneratorFacade`：对外暴露统一的代码生成门面，组装多个 Service 与工具。

- `util`：
  - `UserContext`：线程级当前用户上下文。
  - `PromptLoader`、`CodeGenPrompts`：Prompt 加载与模版工具。
  - `CodeFileSaver`：将生成的代码落盘。
  - `MD5Util` 等通用工具。

> **定位总结：**
>
> - `lqq-common` = 领域模型（User / App / 会话记忆）+ 通用基础设施（异常、响应、上下文）+ LangChain4j 集成（模型、工具、AI Service）。
> - 未来要实现「像 aideepin 一样的多智能体平台」，核心改造点都在本模块：
>   - 以 `App` 为中心扩展智能体配置字段与关联（知识库、MCP、模型、语音等）。
>   - 在 `langchain4j` 包下完善 AI Services（例如 AppAssistant / GeneralAssistant）。
>   - 在 `AiChatMemory` 与会话结构之间建立更清晰的 Conversation / Message 概念。

---

## 三、`lqq-chat`：用户端 API 层

`lqq-chat` 对应 aideepin 的 `adi-chat`，主要暴露给「用户前端」使用的接口：

- `chat.LqqChatApplication`：
  - 用户端 Spring Boot 启动类（被 `lqq-bootstrap` 聚合调用）。

- `controller` 包：
  - `UsersController`：
    - 用户注册、登录、登出、获取当前登录用户信息、用户管理等。
    - 依赖 `lqq-common` 中的 `UserService`。
  - `AppController`：
    - 应用（智能体）管理：
      - 创建应用（创建智能体模板，类似 `ConversationPresetService.add`）。
      - 更新 / 删除应用。
      - 查询自己的应用 / 推荐应用 / 管理员视角的应用列表等。
    - 依赖 `AppService`，操作 `App` 实体。
  - `ChatController`：
    - 核心聊天接口：
      - 普通聊天接口：接收用户消息 + appId / 会话信息，调用 LangChain4j ChatModel 给出完整回答。
      - 流式聊天接口：使用 `StreamingChatModel` + SSE 将回答按 token 流式返回。
    - 依赖 `LangChain4jConfig` 中的 ChatModel / StreamingChatModel、`AiChatMemoryService` 等。
    - 是后续「接入多智能体配置、知识库、工具」的关键入口。
  - `AgentController`：
    - 与简单智能体 / 代理相关的接口（未来可与 App / Conversation 打通或合并）。
  - `CodeGenController`：
    - 代码生成相关接口，使用 `AiCodeGeneratorFacade` / `LangChain4jCodeGeneratorService`。
  - `HealthController`：
    - 健康检查接口。

> **定位总结：**
>
> - `lqq-chat` 是对 “前台用户” 暴露的功能入口：注册登录、创建/编辑应用（智能体）、与智能体聊天、代码生成等。
> - 业务逻辑尽量放在 `lqq-common` 的 Service 中，Controller 负责：
>   - 解析 HTTP 请求 → DTO
>   - 鉴权 / 参数校验
>   - 调用 Service 并将结果包装为 `BaseResponse` 返回。

---

## 四、`lqq-admin`：管理端 API 层

`lqq-admin` 对应 aideepin 的 `adi-admin`，服务于未来的运营/管理后台：

- `admin.LqqAdminApplication`：
  - 管理端 Spring Boot 启动类（同样被 `lqq-bootstrap` 聚合）。

- `admin.controller` 包：
  - `AdminAppController`：
    - 后台应用（智能体）管理：
      - 管理员视角的应用列表、审核、上下线等（当前已有基础能力，可后续扩展细节）。
    - 直接或间接复用 `AppService`。
  - `AdminUserController`：
    - 管理员视角的用户列表、编辑、禁用等（当前能力可以按需扩展）。

> **后续规划：**
>
> - 对标 aideepin 的 `AdminConvPresetController` / `AdminMcpController` / `ModelPlatformController` 等，
>   - 在此模块逐步增加：模型平台管理、智能体高级配置（知识库、MCP、模型选择、语音）、系统配置与统计等。

---

## 五、`lqq-bootstrap`：统一启动与部署模块

- `LqqAiAgentApplication`：
  - 顶层 Spring Boot 启动类，依赖 `lqq-chat` 与 `lqq-admin` 模块。
  - 统一对外暴露所有接口，方便打包部署。

- 部署建议：
  - 通过 `mvn clean package` 在 `lqq-bootstrap/target` 生成可运行 jar。
  - 按环境参数（如 `--spring.profiles.active=dev`）启动对应配置。

---

## 六、对标 aideepin 的能力规划（下一步实现指引）

结合 langchain4j-aideepin 的架构，`lqq-ai-agent` 后续可以按以下方向演进：

1. **将 `App` 明确定位为「智能体模板」**（类似 aideepin 的 `ConversationPreset`）：
   - 在 `App` 实体中补充或确认以下字段：
     - 名称、封面、简介。
     - 核心 prompt（如 `initPrompt`，对应 `aiSystemMessage`）。
     - 选用模型（模型平台 + 模型名称）。
     - 深度思考、自动语音播放等开关（可后续增加）。
   - 在 `AppService` 中实现：
     - 创建 / 编辑 / 删除 / 查询应用的完整业务逻辑。

2. **引入会话与消息的概念（Conversation / Message）**：
   - 可在 `lqq-common.model.entity` 下新增：
     - `Conversation`：会话表，关联 `userId` 与 `appId`。
     - `ConversationMessage`：消息表，记录 role（user/assistant/system）、内容、引用信息等。
   - 在 `AiChatMemory` 的基础上重构：
     - 使用 `ConversationService` 统一管理会话与历史消息，
       而不是由 `ChatController` 直接操作记忆表。

3. **为每个 App 定义一个 AI Service 接口**（对标 LangChain4j AiServices）：
   - 在 `lqq-common.langchain4j` 下新增：

     - `AppAssistant` 接口示例：
       - `String chat(String conversationId, String systemPrompt, String userMessage)`
       - 内部由 LangChain4j 代理实现，负责：
         - 将字符串转换为 ChatMessage 列表。
         - 将 `conversationId` 作为 MemoryId 管理上下文。
   - 在 `LangChain4jConfig` 中配置好 ChatModel / StreamingChatModel，并初始化这些 AI Service。

4. **扩展聊天链路，整合 App 配置**：
   - 在 `ChatController` 的聊天接口中，改为：
     - 根据前端传入的 `appId` / `conversationId`：
       - 使用 `AppService` 读取 App（智能体模板）配置。
       - 使用 `ConversationService` 或 `AiChatMemoryService` 管理会话。
       - 调用 `AppAssistant`（或通用 `UniversalAssistant`），传入 systemPrompt + userMessage。
   - 为流式接口增加一个 `StreamingAppAssistant` 或基于 `StreamingChatModel` 的封装，输出 SSE。

5. **预留知识库 / MCP / Workflow 的扩展点**：
   - 虽然当前项目还未引入完整的知识库和 MCP 模块，但可以：
     - 在 `App` 实体中预留「知识库Id列表」「MCP 服务列表」等字段，或单独建关联表（如 `app_kb_rel`、`app_mcp_rel`）。
     - 在 `langchain4j` 包中，预留向量检索与工具调用的接口（后续可从 aideepin 迁移具体实现）。

---

## 七、后续文档与实现建议（TODO）

> 以下内容可在你逐步实现/重构时同步补充：

- 为 `App`、`Conversation`、`ConversationMessage` 绘制 ER 图，并在文档中补充字段级说明。
- 在 `LangChain4jConfig` 中固定一版「通用智能助手」的模型和参数配置示例。
- 设计并记录一条从「前端发送消息」到「AppAssistant / StreamingChatModel」的详细时序图（对标本仓库 aideepin 文档中的第六节）。
- 规划并记录知识库 / MCP / Workflow 模块在本项目中的落地位置与表结构草案。
