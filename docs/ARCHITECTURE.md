# Peek Chat（眯聊）— Architecture

> 浮窗常驻，进微信聊天的同时，一键收录整段对话。自动 OCR 提取文字 → AI 分析 → 结构化报告。
>
> 定位：AI Skill 模式的移动端参考实现。架构预留 DeepSeek Harness 接入点。

---

## 参考项目

| 项目 | 仓库 | 借鉴 |
|------|------|------|
| **Folo** | [RSSNext/Folo](https://github.com/RSSNext/Folo) | 工程组织：monorepo 顶层划分、`packages/` vs `apps/` 分层、CI/CD 思路 |
| **JetBrains CMP 模板** | [JetBrains/compose-multiplatform-ios-android-template](https://github.com/JetBrains/compose-multiplatform-ios-android-template) | CMP 模块写法：`commonMain` / `androidMain` / `iosMain`、`expect`/`actual` 模式 |

> **注**：[Now in Android](https://github.com/android/nowinandroid) 最初被列为参考，但它是为 Jetpack Compose（`androidx.compose.*`，Android-only）设计的，与 Compose Multiplatform（`org.jetbrains.compose.*`，跨平台）是两条不同的技术路线。NIA 使用的 Nav3、Hilt、ViewModel、Room 均无法用于 CMP，故不作为本项目参考。

---

## 核心原则

### 不侵入微信

不解密数据库，不读取微信进程内存，不调用微信私有 API。用户主动提供截图，纯 OCR 提取文本。

### 本地优先

OCR 完全离线（ML Kit）。AI 分析需要网络。无账户系统，数据存本地。

### AI 只传文本

原始截图不上传任何服务器。OCR 提取的纯文本发给 AI Provider。

### 多端架构

第一期 Android MVP，架构从第一天起支持 iOS、Desktop（macOS/Windows/Linux）。

---

## 项目结构

```
peek-chat/
│
├── packages/                           # 多包共享层（Folo 式工程组织）
│   │
│   ├── model/                          # KMP — 纯数据模型，最底层
│   │   ├── build.gradle.kts            # kotlin("multiplatform") → jvm() target
│   │   └── src/commonMain/kotlin/com/peekchat/model/
│   │       ├── BubblePosition.kt       # LEFT / RIGHT
│   │       ├── ChatMessage.kt          # speaker, content, timestamp
│   │       ├── Conversation.kt         # List<ChatMessage> + metadata
│   │       ├── AnalysisReport.kt       # summary, todos, sentiment, decisions
│   │       └── OcrResult.kt            # ML Kit 原始输出封装
│   │
│   ├── capture/                         # KMP — 对话采集层
│   │   ├── build.gradle.kts
│   │   ├── src/commonMain/kotlin/com/peekchat/capture/
│   │   │   ├── CaptureEngine.kt                  # expect 采集引擎接口
│   │   │   └── CaptureSource.kt                  # 采集来源：浮窗 / 手动导入
│   │   ├── src/androidMain/kotlin/com/peekchat/capture/
│   │   │   ├── FloatingWindowCapture.kt          # actual: Overlay API 浮窗采集
│   │   │   ├── AutoScroller.kt                   # actual: AccessibilityService 自动滚动
│   │   │   └── MediaProjectionCapture.kt         # actual: MediaProjection 截图
│   │   └── src/iosMain/                          # actual: (future)
│   │
│   ├── database/                       # KMP — 持久化层
│   │   ├── build.gradle.kts
│   │   ├── src/commonMain/kotlin/com/peekchat/database/
│   │   │   ├── ConversationRepository.kt       # expect interface
│   │   │   └── AnalysisReportRepository.kt     # expect interface
│   │   ├── src/androidMain/kotlin/com/peekchat/database/
│   │   │   ├── RoomConversationRepository.kt   # actual: Room
│   │   │   ├── RoomAnalysisReportRepository.kt
│   │   │   └── PeekChatDatabase.kt             # Room database
│   │   └── src/iosMain/                        # actual: SQLDelight (future)
│   │
│   ├── ocr/                             # KMP — OCR 引擎
│   │   ├── build.gradle.kts
│   │   ├── src/commonMain/kotlin/com/peekchat/ocr/
│   │   │   ├── OcrEngine.kt                    # expect class
│   │   │   ├── BubbleClassifier.kt             # 气泡归属（位置判断）
│   │   │   └── ConversationStitcher.kt         # 多图拼接去重
│   │   ├── src/androidMain/kotlin/com/peekchat/ocr/
│   │   │   └── MlKitOcrEngine.kt               # actual: ML Kit Text Recognition v2
│   │   └── src/iosMain/                        # actual: Vision framework (future)
│   │
│   ├── ai/                              # KMP — AI Provider
│   │   ├── build.gradle.kts
│   │   ├── src/commonMain/kotlin/com/peekchat/ai/
│   │   │   ├── AiProvider.kt                   # expect interface
│   │   │   └── PromptBuilder.kt                # 构造中文 prompt
│   │   ├── src/androidMain/kotlin/com/peekchat/ai/
│   │   │   └── DeepSeekProvider.kt             # actual: OkHttp + OpenAI-compatible API
│   │   └── src/iosMain/                        # actual: URLSession (future)
│   │
│   ├── data/                            # KMP — Repository 实现
│   │   ├── build.gradle.kts
│   │   ├── src/commonMain/kotlin/com/peekchat/data/
│   │   │   ├── ConversationRepository.kt       # expect (如有跨平台差异)
│   │   │   └── AnalysisRepository.kt
│   │   ├── src/androidMain/
│   │   └── src/iosMain/
│   │
│   ├── designsystem/                    # KMP — Material 3 主题 + 基础组件
│   │   ├── build.gradle.kts
│   │   └── src/commonMain/kotlin/com/peekchat/designsystem/
│   │       ├── theme/
│   │       │   ├── Theme.kt
│   │       │   ├── Color.kt
│   │       │   └── Type.kt
│   │       └── component/
│   │           ├── PeekChatScaffold.kt
│   │           └── LoadingIndicator.kt
│   │
│   ├── ui/                              # KMP — 屏幕 + 业务组件
│   │   ├── build.gradle.kts
│   │   └── src/commonMain/kotlin/com/peekchat/ui/
│   │       ├── screen/
│   │       │   ├── capture/             # 选图 → OCR → 预览
│   │       │   │   ├── CaptureScreen.kt
│   │       │   │   └── CaptureViewModel.kt
│   │       │   ├── analysis/            # AI 分析报告展示
│   │       │   │   ├── AnalysisScreen.kt
│   │       │   │   └── AnalysisViewModel.kt
│   │       │   └── history/             # 历史记录
│   │       │       ├── HistoryScreen.kt
│   │       │       └── HistoryViewModel.kt
│   │       └── component/
│   │           ├── BubbleCard.kt        # 聊天气泡（左/右两种样式）
│   │           ├── ReportCard.kt
│   │           ├── SummaryCard.kt       # 对话摘要
│   │           ├── TodoListCard.kt      # 待办提取
│   │           ├── SentimentIndicator.kt # 情绪判断
│   │           └── DecisionListCard.kt  # 关键决策
│   │
│   └── common/                          # KMP — 工具函数、扩展
│       ├── build.gradle.kts
│       └── src/commonMain/kotlin/com/peekchat/common/
│           └── ...
│
├── apps/                               # 薄壳应用层（每个 app 极简）
│   │
│   ├── android/                        # Android 壳
│   │   ├── build.gradle.kts
│   │   └── src/main/
│   │       ├── AndroidManifest.xml
│   │       ├── kotlin/com/peekchat/android/
│   │       │   ├── MainActivity.kt              # setContent { PeekChatApp() }
│   │       │   └── PeekChatApp.kt               # 组装 ui 包里的 Screen + 导航
│   │       └── res/
│   │
│   ├── ios/                            # iOS 壳 (future)
│   │   └── ...
│   │
│   └── desktop/                        # Compose Desktop (future)
│       ├── build.gradle.kts
│       └── src/main/kotlin/com/peekchat/desktop/
│           └── Main.kt                          # fun main() = Window { PeekChatApp() }
│
├── build.gradle.kts                    # 根 build
├── settings.gradle.kts                 # includeBuild + include all modules
├── gradle.properties
├── gradle/
│   └── libs.versions.toml             # 版本目录（对标 Folo pnpm catalog）
│
├── .github/workflows/                  # CI/CD
│   ├── build.yml
│   └── release.yml
│
├── docs/
│   └── ARCHITECTURE.md
│
├── LICENSE                             # MIT
└── README.md
```

---

## 包依赖图

```
apps/android ─┐
apps/ios ─────┤                        # 壳：只依赖 ui 包
apps/desktop ─┘
         │
         ▼
packages/ui ────────────────────────┐
packages/designsystem ──────────────┤
         │                          │
         ▼                          ▼
packages/data ──→ packages/database ──→ packages/model
packages/capture ─────────────────────→ packages/model
packages/ocr ─────────────────────────→ packages/model
packages/ai ──────────────────────────→ packages/model
packages/common ──────────────────────→ (独立)
```

**规则**：

- `model` 不依赖任何其他包
- 所有包依赖 `model`
- `capture`、`database`、`ocr`、`ai` 互相不依赖
- `data` 聚合 `database` + `ocr` + `ai`
- `ui` 依赖 `data`、`designsystem`
- `apps/*` 仅依赖 `ui`（及平台所需依赖）

---

## 技术栈

### 共享层 (packages/*)

| 类别 | 选型 | 说明 |
|------|------|------|
| 语言 | Kotlin | JVM 17+ |
| 跨平台框架 | Compose Multiplatform (KMP) | `org.jetbrains.compose` |
| UI | Compose Multiplatform | 一套 Compose 代码覆盖所有端 |
| 状态管理 | Kotlin Flow + MutableStateFlow | Compose 端 `collectAsState()` |
| 序列化 | kotlinx.serialization | JSON、模型序列化 |
| 网络 | Ktor Client (KMP) | 跨平台 HTTP 客户端 |
| 依赖注入 | expect/actual 手动 DI | MVP 阶段保持简单，未来可上 Koin KMP |
| 键值存储 | multiplatform-settings | 用户偏好 |

### Android platform (apps/android)

| 类别 | 选型 | 说明 |
|------|------|------|
| 浮窗 | Android Overlay API | 微信上层绘制浮动按钮 |
| 自动采集 | MediaProjection + AccessibilityService | 截图 + 自动滚动微信页面 |
| OCR | ML Kit Text Recognition v2 | Google 官方，离线可用 |
| 数据库 | Room | expect/actual 的 Android 实现 |
| 构建 | AGP + Gradle | |

### AI Provider

| 类别 | 选型 | 说明 |
|------|------|------|
| 首期 | DeepSeek API | OpenAI-compatible，中文能力强 |
| 协议 | REST API, JSON | 纯文本 prompt，不上传图片 |
| 安全 | EncryptedSharedPreferences | API Key 本地加密存储 |

---

## 核心技术方案

### 1. 气泡归属判断

**方案**：位置推断（主） + 颜色辅助（辅）

- ML Kit Text Recognition v2 为每个文本块返回 bounding box 坐标
- 水平中心点 < 屏幕宽度 50% → 对方（左侧白色气泡）
- 水平中心点 > 屏幕宽度 50% → 自己（右侧绿色气泡）
- 颜色采样仅作为置信度加权，非主要判断依据

**优势**：不依赖主题色、深色模式、截图压缩，鲁棒性高。

### 2. 多图拼接去重

用户可能连续截 3-5 张来覆盖长聊天，截图间有重叠区域。

**方案**：文本相似度 + 时序推断

- 相邻截图分别 OCR → 比较尾部/顶部消息的文本相似度
- 重叠部分去重 → 形成连续对话时间线
- 保证 AI 输入无重复

### 3. AI 分析流程

```
截图 → OCR (离线) → 气泡归属 → 拼接去重 → 结构化纯文本 → PromptBuilder → AiProvider → AnalysisReport
```

- Prompt 使用中文
- 仅传递纯文本，不上传图片
- 结构：摘要 + 待办提取 + 情绪判断 + 关键决策

### 4. 离线策略

- OCR（ML Kit）：完全离线
- AI 分析：需要网络
- MVP 阶段：无网络 → 提示用户，不做任务队列
- Future：网络恢复后自动发送

### 5. 导航

CMP 生态无统一导航方案。MVP 阶段项目规模小，采用**自建简单 back stack**：

```kotlin
// packages/ui/src/commonMain/kotlin/com/peekchat/ui/navigation/
data class NavState(
    val stack: List<Screen> = listOf(Screen.CaptureHome)
)

sealed class Screen {
    data object CaptureHome : Screen()
    data class OcrPreview(val imageCount: Int) : Screen()
    data class AnalysisReport(val conversationId: String) : Screen()
    data object History : Screen()
}
```

不需要引入 Voyager、Decompose 等第三方导航框架。

## Harness 接入设计

### 设计原则

眯聊的 AI 层遵循 **Provider-agnostic** 原则。所有 AI 调用通过 `AiProvider` 接口进行，具体实现——无论是直接调 DeepSeek API，还是通过 DeepSeek Harness 编排——都只是接口的一个实现类。

### AiProvider 接口

```kotlin
// packages/ai/src/commonMain/kotlin/com/peekchat/ai/AiProvider.kt

interface AiProvider {
    suspend fun analyze(conversation: Conversation): AnalysisReport
}
```

这个接口刻意设计得很薄：
- **输入**：`Conversation`——已预处理好的结构化对话（非原始截图，非原始 prompt string）
- **输出**：`AnalysisReport`——已解析好的结构化结果（非 raw JSON）
- **不含 prompt 构造**：`PromptBuilder` 是独立组件，不在 `AiProvider` 中

这样设计的原因是：无论 Provider 是直连 API 还是接 Harness，输入和输出格式都不变。唯一变化的是 `analyze()` 内部怎么把对话发给 AI 再拿回结果。

### 当前实现 vs Harness 实现

**当前（直连 DeepSeek API）**：

```kotlin
class DeepSeekProvider(private val httpClient: HttpClient) : AiProvider {

    override suspend fun analyze(conversation: Conversation): AnalysisReport {
        val prompt = PromptBuilder.build(conversation)
        val response = httpClient.post("https://api.deepseek.com/v1/chat/completions") {
            setBody(buildJsonRequest(prompt))
        }
        return parseResponse(response)
    }
}
```

**Harness 接入后**：

```kotlin
class HarnessProvider(private val harnessClient: DshClient) : AiProvider {

    override suspend fun analyze(conversation: Conversation): AnalysisReport {
        return harnessClient.skills.run(
            skillId = "peek-chat",
            input = conversation.toSkillInput(),
            outputType = AnalysisReport::class
        )
    }
}
```

接入前后对比：

| | 现在 | Harness 后 |
|---|---|---|
| 改动的文件 | — | 1 个（替换 `DeepSeekProvider`） |
| 改动的包 | — | 仅 `packages/ai` |
| 其余模块 | — | **不动** |
| 前端 UI | — | **不动** |
| OCR / 采集 / 数据库 | — | **不动** |

### 为什么不需要改动其他模块

因为 `AiProvider` 接口隔绝了所有 AI 调用细节：

```
packages/ai/commonMain/
├── AiProvider.kt        ← 接口（不变）
├── PromptBuilder.kt      ← Prompt 构造（初期共享，Harness 后可交由编排层管理）
├── androidMain/
│   └── DeepSeekProvider.kt   ← 替换为 HarnessProvider.kt
└── iosMain/
    └── DeepSeekProvider.kt   ← 替换为 HarnessProvider.kt
```

上层 `packages/data` 依赖的是 `AiProvider` 接口，不知道也不关心背后是直连 API 还是 Harness。这是最经典的依赖反转——眯聊的架构从第一天就为这个替换做好了准备。

### Skill 拆分设计

眯聊当前在一个 AI 调用中完成四项分析（摘要 + 待办 + 情绪 + 决策）。架构上，这些分析可以拆分为独立 Skill：

```
眯聊 (当前：单 Skill)
  │
  ├── Skill: summarize      # 对话摘要
  ├── Skill: extract-todos  # 待办提取
  ├── Skill: analyze-sentiment  # 情绪分析
  └── Skill: extract-decisions  # 关键决策
```

拆分后，Harness 的编排层可以：
- 四个 Skill 并行执行（更快）
- 用户自定义分析组合（"这次只要摘要和待办"）
- 不同 Skill 可选不同模型（摘要用小模型省钱，情绪分析用大模型保质量）

PromptBuilder 当前是将四个任务合并为一个 prompt，拆分后每个 Skill 各自维护自己的 prompt template，Harness 调度执行。

---

## 数据模型

```kotlin
// packages/model/src/commonMain/kotlin/com/peekchat/model/

enum class BubblePosition { LEFT, RIGHT }

enum class Speaker {
    SELF,       // 自己说的
    OTHER,      // 对方说的
    UNKNOWN     // 无法判断（如系统消息）
}

data class ChatMessage(
    val speaker: Speaker,
    val content: String,
    val timestamp: String?,          // OCR 提取的原始时间文本
    val bubblePosition: BubblePosition
)

data class Conversation(
    val id: String,
    val messages: List<ChatMessage>,
    val imageCount: Int,             // 来源截图数量
    val createdAt: Long              // epoch millis
)

data class AnalysisReport(
    val conversationId: String,
    val summary: String,             // 对话摘要
    val todos: List<TodoItem>,       // 待办提取
    val sentiment: Sentiment,        // 情绪判断
    val decisions: List<Decision>,   // 关键决策
    val createdAt: Long
)

data class TodoItem(
    val content: String,
    val assignee: Speaker            // 谁需要做这件事
)

data class Sentiment(
    val overall: String,             // 整体情绪标签
    val positive: List<String>,      // 正面话题
    val negative: List<String>       // 负面话题
)

data class Decision(
    val content: String,
    val participants: List<Speaker>  // 参与决策的人
)
```

---

## MVP 范围 (Phase 1)

### 做

- Android 端（Compose Multiplatform on Android）
- **浮窗采集**（Overlay API）：微信上层常驻浮动按钮，一键触发采集
- **自动截图**（MediaProjection）：自动滚动微信页面并截图
- **手动导入**：从相册选择已有截图
- ML Kit OCR 提取文字
- 气泡归属（位置判断）
- 多图拼接去重
- DeepSeek AI 分析
- 结构化报告展示（摘要卡片、待办列表、情绪指示器、关键决策卡片）
- 本地保存分析记录（Room）
- 无网络提示

### 不做

- iOS 端
- Desktop 端
- 账户系统
- 离线队列（自动重试）
- 聊天记录搜索
- 批量分析优化
- 导出报告

---

## Future (Phase 2+)

| 阶段 | 内容 |
|------|------|
| Phase 2 | iOS 端（`apps/ios/`，Vision OCR + URLSession） |
| Phase 3 | Desktop 端（`apps/desktop/`，macOS/Windows/Linux） |
| Phase 4 | 对话历史管理（搜索、删除、导出） |
| Phase 5 | 多 Provider 切换 + DeepSeek Harness 编排层集成 |

---

## 开发环境

| 工具 | 版本 |
|------|------|
| Kotlin | 2.1+ |
| Compose Multiplatform | 1.7+ |
| AGP | 8.7+ |
| Gradle | 8.10+ |
| JDK | 17 |
| Android minSdk | 26 |
| Android targetSdk | 36 |
| Android compileSdk | 36 |

---

## License

GNU General Public License v3.0 (GPL-3.0)
