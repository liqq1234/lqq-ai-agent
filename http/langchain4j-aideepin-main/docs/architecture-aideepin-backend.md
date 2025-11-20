# langchain4j-aideepin 后端架构说明

> 本文档说明的是 **langchain4j-aideepin-main** 仓库（后端服务）的整体架构，用于后续在其它项目（例如 `lqq-ai-agent`）中对标实现与优化。

---

## 一、工程整体结构

后端采用多模块 Maven + Spring Boot 架构：

- **根工程 `aideepin`**
  - 统一依赖管理与版本，聚合所有子模块。
- **子模块**
  - **`adi-common`**：核心域模型与业务逻辑模块。
    - 实体、DTO、Service、配置（LangChain4j、向量库、图数据库等）、工作流引擎等均在此模块中。
  - **`adi-chat`**：面向「用户端 Web」的 REST API 层。
    - 提供聊天、会话管理、知识库、绘图、MCP 工具、工作流等用户能力的 HTTP 接口。
  - **`adi-admin`**：面向「管理端 Web」的 REST API 层。
    - 提供模型平台、AI 模型、预设对话（智能体）、知识库、MCP 市场、统计、系统配置等后台管理接口。
  - **`adi-bootstrap`**：应用启动与打包模块。
    - 只有一个 Spring Boot 启动类 `BootstrapApplication`，依赖 `adi-chat` 与 `adi-admin`，最终打成可运行 jar。

依赖关系大致为：

```text
           +-----------+
           | adi-chat  |
           +-----------+
                 ^
                 |
+-----------+    |
| adi-admin |    |
+-----------+    |
      ^          |
      |          |
   +------------------+
   |    adi-common    |
   +------------------+
            ^
            |
     +--------------+
     | adi-bootstrap|
     +--------------+
```

- 所有核心领域逻辑和与 LangChain4j 相关的配置 **集中在 `adi-common`**。
- `adi-chat` / `adi-admin` 只是面向不同前端的 API 外壳，控制器逻辑较薄。
- `adi-bootstrap` 将上述两套 API 聚合到一个 Spring Boot 应用中运行。

---

## 二、`adi-common` 模块结构与职责

`adi-common` 是整个系统的「心脏」，包含：

- 领域模型（实体、枚举）
- 传输对象（DTO）
- 业务服务（Service）
- LangChain4j 集成（ChatModel、StreamingChatModel、EmbeddingStore、GraphStore 等）
- 知识库与 RAG 能力
- MCP 工具与用户配置
- 简单工作流引擎

### 2.1 主要包划分

`adi-common/src/main/java/com/moyz/adi/common` 下核心包：

- **`annotation`**
  - 自定义注解，例如 `AskReqCheck`、`CreateImageReqCheck`、`ParamsLog` 等。
  - 用于参数校验、分布式锁、统一日志记录等。

- **`aop`**
  - AOP 切面类，例如：
    - `ControllerParamsLogAspect` / `ParamsLogAspect`：统一记录 Controller 层入参/出参。
    - `DistributeLockAspect`：实现基于注解的分布式锁控制。

- **`base`**
  - 通用基础设施类：
    - `BaseResponse`、`ResponseWrapper`：统一接口响应格式。
    - `ThreadContext`：存储当前请求上下文（如当前用户）。
    - 若干 MyBatis TypeHandler：
      - `JsonNodeTypeHandler`、`ObjectNodeTypeHandler`：JSON 存储与解析。
      - `PostgresVectorTypeHandler`：Postgres pgvector 类型处理。
      - 图数据库、音频配置等其他复杂字段的 TypeHandler。

- **`config`**
  - **核心配置类所在包**，包括：
    - `AdiProperties`：系统级配置属性封装。
    - `BeanConfig`：
      - 定义 LangChain4j 的 `ChatModel` / `StreamingChatModel`。
      - 定义 EmbeddingStore（向量库）、GraphStore（图数据库）等 Bean。
      - 定义 HTTP 客户端拦截器，如 `LogClientHttpRequestInterceptor`。
    - `embeddingstore` 子包：
      - `PgVectorEmbeddingStoreConfig`：pgvector 向量库配置。
      - `Neo4jEmbeddingStoreConfig`：Neo4j 向量库配置。
    - `graphstore` 子包：
      - `ApacheAgeGraphStoreConfig`、`Neo4jGraphStoreConfig`：图存储配置。
    - `WebMvcConfig`：注册登录拦截器、日志拦截器等 WebMvc 组件。
    - `SpringdocConfig`：OpenAPI 文档配置。

- **`constant`**
  - `AdiConstant`、`RedisKeyConstant`：系统常量与 Redis Key 规范。

- **`dto`**
  - 定义所有对外接口使用的请求/响应对象，例如：
    - 聊天相关：`AskReq`、`ConvDto`、`ConvMsgDto`、`ConvPresetAddReq`、`ConvPresetEditReq` 等。
    - 知识库相关：`KbEditReq`、`KbItemDto`、`KbSearchReq`、`KbStatDto` 等。
    - MCP 相关：`mcp` 子包中的 `McpAddOrEditReq`、`McpListReq`、`UserMcpUpdateReq` 等。
    - 用户与认证：`LoginReq`、`LoginResp`、`RegisterReq`、`UserInfoDto`、`UserQuota` 等。
    - 搜索、统计、工作流等：`AiSearchReq`、`CostStatResp`、`Workflow` 相关 DTO 等。

- **`entity`**
  - 对应数据库表的实体，是领域对象的核心表示，例如：
    - 会话与消息：`Conversation`、`ConversationMessage`、`ConversationPreset`、`ConversationPresetRel` 等。
    - 知识库：`KnowledgeBase`、`KnowledgeBaseItem`、`KnowledgeBaseGraphSegment`、`KnowledgeBaseQaRecord` 等。
    - 模型与平台：`AiModel`、`ModelPlatform`。
    - MCP：`Mcp`、`UserMcp`、`McpCustomizedParamDefinition` 等。
    - 系统配置与统计：`SysConfig`、`UserDayCost`、`Statistic` 等。
    - 工作流：`Workflow`、`WorkflowNode`、`WorkflowEdge`、`WorkflowRuntime` 等。

- **`service`**
  - **业务 Service 层的集中地，是整个系统的业务核心**：
    - 聊天相关：
      - `ConversationService`：会话与聊天核心逻辑（调用 LangChain4j）。
      - `ConversationMessageService`：消息存储与查询。
      - `ConversationPresetService`：预设对话（智能体模板）管理。
      - `ConversationPresetRelService`：用户与预设的关系（最近使用、可见列表）。
    - 知识库相关：
      - `KnowledgeBaseService`、`KnowledgeBaseItemService`：知识库与条目管理。
      - `KnowledgeBaseEmbeddingService`、`Neo4jKnowledgeEmbeddingService`：向量化与索引。
      - `KnowledgeBaseGraphService`、`KnowledgeBaseGraphSegmentService`：图谱存储与查询。
      - `KnowledgeBaseQaService` 及相关 *Ref* Service：问答与引用记录。
    - 模型与平台：
      - `ModelPlatformService`：OpenAI、DashScope、DeepSeek、Ollama 等平台配置。
      - `AiModelService`、`AiModelInitializer`：具体模型的启用/初始化。
    - MCP 与工具：
      - `McpService`：MCP 服务的管理与调用。
      - `UserMcpService`：用户级 MCP 设置管理。
    - 工作流：
      - `WorkflowService`、`WorkflowNodeService`、`WorkflowEdgeService` 等：工作流模板配置。
      - `WorkflowRuntimeService`、`WorkflowRuntimeNodeService`：工作流运行时状态管理。
    - 其它通用服务：
      - `UserService`、`SysConfigService`、`SearchService`、`StatisticService`、`FileService` 等。

- **`workflow`**
  - 实现了一个基于图的轻量工作流引擎，用于 "AI Workflow" 功能：
    - `node` 子包中定义了多种节点类型：
      - LLM 回答节点：`LLMAnswerNode`。
      - 知识检索节点：`KnowledgeRetrievalNode`。
      - Google 搜索节点：`GoogleNode`。
      - HTTP 请求节点：`HttpRequestNode`。
      - 人工反馈节点：`HumanFeedbackNode`。
      - 模板节点、绘图节点、背景生成节点等。
    - `edge` 子包定义了普通/条件/并行边：`Edge`、`ConditionalEdge`、`ParallelEdge`。
    - `WorkflowEngine`、`WorkflowStarter` 负责组合节点与边，按顺序或条件执行整个工作流。

> 小结：`adi-common` 是 **领域 + 基础设施 + LangChain4j 能力** 的集中地，所有高阶能力（RAG、MCP、Workflow、统计等）均在此模块内部实现。

---

## 三、`adi-chat`：用户端 API 层

`adi-chat` 模块主要包含 `controller` 包，对应「用户 Web」前端调用的 REST 接口：

- **会话与聊天**
  - `ConversationController`：会话列表、新建/删除会话等。
  - `ConversationMessageController`：发送消息、获取历史、流式聊天（SSE）等。
  - `ConversationPresetController` / `ConversationPresetRelController`：
    - 用户侧查询可用的预设对话（智能体）、最近使用列表等。

- **用户与认证**
  - `AuthController`：登录、注册、验证码验证。
  - `UserController`：用户信息查询、修改密码等。

- **知识库相关**
  - `KnowledgeBaseController`、`KnowledgeBaseItemController`：知识库和条目管理（用户维度）。
  - `KnowledgeBaseEmbeddingController`：向量索引相关操作。
  - `KnowledgeBaseGraphController`：图谱相关接口。
  - `KnowledgeBaseQAController`、`KnowledgeBaseStarController`：问答与收藏等。

- **绘图与多模态**
  - `DrawController`：AI 绘图（文生图、图生图等）。
  - `DrawCommentController`：绘图评论。
  - `DrawStarController`：绘图收藏/点赞。

- **MCP 与模型**
  - `McpController`：用户侧 MCP 服务调用/配置接口。
  - `UserMcpController`：用户个人 MCP 设置管理。
  - `ModelController`：可选模型列表。

- **搜索与系统配置**
  - `SearchController`、`SearchRecordController`：Web 搜索（RAG）与搜索记录。
  - `SysConfigController`：系统配置信息对用户侧的曝光。

- **工作流**
  - `WorkflowController`、`WorkflowRuntimeController`：用户侧 AI Workflow 调度与运行时查询。

> 特点：`adi-chat` 的 Controller 层非常「薄」，只负责：
>
> - 将 HTTP 请求转换为 DTO；
> - 做少量权限与参数校验（结合注解和 AOP）；
> - 调用 `adi-common.service` 中的对应 Service；
> - 将 Service 返回值包装为统一响应结构返回前端。

---

## 四、`adi-admin`：管理端 API 层

`adi-admin` 模块也以 Controller 为主，但面向的是「管理后台」前端：

- **会话与智能体管理**
  - `AdminConvController`：会话管理。
  - `AdminConvPresetController`：预设对话（智能体模板）管理，包括：
    - 列表查询、新增、编辑、删除预设。
    - 管理通用智能助手以及其它角色。

- **知识库管理**
  - `AdminKbController`：知识库与条目管理（全局维度）。

- **MCP 与模型平台**
  - `AdminMcpController`：MCP 服务市场管理（添加/启用/下线 MCP 服务）。
  - `AdminModelController`、`ModelPlatformController`：
    - 管理各大模型平台与具体模型配置（OpenAI / DashScope / DeepSeek / Ollama 等）。

- **用户与工作流**
  - `AdminUserController`：用户管理（列表、编辑、禁用等）。
  - `AdminWorkflowController`、`AdminWfComponentController`：工作流模板与组件管理。

- **系统配置与统计**
  - `SystemConfigController`：系统级配置项编辑（邮件、搜索引擎、存储配置等）。
  - `StatisticController`：请求量、Token 消耗、绘图次数、用户统计等。

> 管理端 API 与用户端 API 共用 **同一套 `adi-common` 的实体与 Service**，仅在权限、过滤条件等方面做差异化处理。

---

## 五、`adi-bootstrap`：启动与打包模块

- 入口类：
  - `BootstrapApplication`：`@SpringBootApplication` 注解，扫描 `adi-chat` 与 `adi-admin` 两个模块中的控制器与配置。

- `pom.xml`：
  - 依赖 `adi-chat` 与 `adi-admin`。
  - 使用 `spring-boot-maven-plugin`：
    - `mainClass = com.moyz.adi.BootstrapApplication`。
    - `includeSystemScope = true`：打包时包含 system scope 依赖（如本地 jar）。

- 运行形式：
  - 通过 `mvn clean package`，在 `adi-bootstrap/target` 生成 fat-jar。
  - 可直接 `java -jar adi-bootstrap-0.0.1-SNAPSHOT.jar --spring.profiles.active=[dev|prod]` 启动整个后端系统。

---

## 六、聊天与 LangChain4j 的典型调用链

以「用户在通用智能助手中发送一条消息」为例，完整调用链如下：

1. **前端（User Web）**
   - 页面：通用智能助手聊天页（多角色聊天界面之一）。
   - 调用接口：`POST /conversation/message/ask` 或 `/conversation/message/stream`（具体路径以实际代码为准）。
   - 请求参数（封装在 `AskReq` 等 DTO 中）：
     - `conversationId`：会话 ID（若为空则由后端新建会话）。
     - `presetUuid`：选中的预设对话/智能体（如「通用智能助手」的 uuid）。
     - `message`：用户输入内容。
     - 其它配置：是否启用深度思考、是否启用知识库、是否启用 MCP、回复形式（文本/语音）等。

2. **`adi-chat`：`ConversationMessageController`**
   - 接收 HTTP 请求并反序列化为 DTO（`AskReq`）。
   - AOP：
     - `ControllerParamsLogAspect` 等切面记录请求日志。
     - 自定义校验注解（如 `@AskReqCheck`）对入参进行校验。
   - 从安全上下文中获取当前用户信息（如 userId）。
   - 调用 `adi-common` 中的 `ConversationService`，例如：
     - `conversationService.ask(askReq, currentUser)`。

3. **`adi-common`：`ConversationService`（聊天核心逻辑）**

   主要职责分为四块：会话管理、预设与配置加载、RAG/MCP 组装、LangChain4j 调用与落库。

   1. **会话管理**
      - 若 `conversationId` 为空：
        - 新建一条 `Conversation` 记录（关联 `userId` 与 `presetUuid`）。
      - 若 `conversationId` 不为空：
        - 校验会话归属（是否属于当前用户）。

   2. **加载预设与配置**
      - 调用 `ConversationPresetService`，根据 `presetUuid` 查询：
        - `ConversationPreset`（名称、备注、`aiSystemMessage` 等）。
      - 查询与该预设关联的：
        - 知识库（通过知识库关联表）。
        - MCP 服务（通过 MCP 关联表）。
        - 使用的模型及模型平台（通过 `AiModel`、`ModelPlatform`）。
        - 语音 / 音频配置等。

   3. **RAG / MCP / Prompt 组装**
      - 若启用知识库：
        - 使用 `KnowledgeBaseEmbeddingService` 等组件对用户问题进行向量检索。
        - 从对应 `KnowledgeBaseItem` / 图谱等中取出相关内容，拼接到 prompt 中。
      - 若启用图谱：
        - 使用 `KnowledgeBaseGraphService` / Apache AGE / Neo4j 等做结构化查询，将结果作为上下文。
      - 若启用 MCP：
        - 使用 `McpService` 找到可用 MCP 工具，并包装为 LangChain4j 的 Tool。
      - 生成最终 Prompt：
        - system：预设中的 `aiSystemMessage` + 系统级说明。
        - history：从 `ConversationMessageService` 或 ChatMemory 中取出历史消息。
        - user：本轮用户消息。

   4. **调用 LangChain4j 与持久化**
      - 通过 `BeanConfig` 中配置好的 `ChatModel` / `StreamingChatModel`：
        - 非流式：`chatModel.generate(messages)`，得到 `AiMessage`。
        - 流式：`streamingChatModel.doChat(chatRequest, handler)`，在 `StreamingChatResponseHandler` 中分片处理 token（可直接推送 SSE）。
      - 将本轮 user/assistant 消息写入 `ConversationMessage` 表。
      - 记录本轮对话关联的向量检索 / 图谱 / MCP 调用结果（对应各类 *Ref* 表）。
      - 将结果封装为 DTO 返回给 Controller。

4. **`adi-chat`：Controller 响应**
   - Controller 获取 `ConversationService` 返回的 DTO：
     - 普通模式：用 `BaseResponse` 包装为 JSON 返回。
     - 流式模式：将 handler 的输出转换为 SSE 数据格式，边生成边推送。

5. **前端展示**
   - 普通模式：一次性展示完整回答。
   - 流式模式：按 SSE 数据流逐步追加内容，实现「打字机效果」。

---

## 七、关键设计要点（可在其它项目中对标实现）

在其它项目（例如 `lqq-ai-agent`）中参考本架构时，值得重点借鉴的设计包括：

1. **清晰的多模块分层**
   - 将「领域能力（common）」与「用户/管理端 API（chat/admin）」及「启动模块（bootstrap）」解耦。

2. **集中式的 LangChain4j 集成**
   - 所有模型、向量库、图数据库等配置集中在 `adi-common.config.BeanConfig` 等配置类中统一管理。

3. **预设对话（智能体模板）+ 会话 + 消息 三层结构**
   - 通过 `ConversationPreset`（智能体模板） + `Conversation`（会话实例） + `ConversationMessage`（具体消息）管理多角色、多会话、多轮对话。

4. **RAG / MCP / Workflow 的可插拔设计**
   - 知识库、MCP 服务、工作流等能力都通过 Service 与配置表实现松耦合，可按需开启或扩展。

5. **Controller 薄、Service 厚**
   - `adi-chat` / `adi-admin` 的 Controller 基本只做入参处理与权限校验，大部分业务逻辑都下沉到 `adi-common.service` 中，便于复用与测试。

---

## 八、后续可以补充的内容（TODO）

> 以下内容留给后续优化与补充时书写：

- 各主要实体（如 `Conversation`、`ConversationPreset`、`KnowledgeBase` 等）的字段说明与 ER 图。
- 具体的 LangChain4j 配置示例（`BeanConfig` 中各 ChatModel / EmbeddingStore 定义）。
- 典型的 RAG 调用流程示意图（文本 → 向量检索 → 组装 prompt → LLM）。
- MCP 工具的注册/调用流程说明。
- AI Workflow 的节点/边配置样例与可视化示意。
