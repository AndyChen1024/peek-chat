# Coding Conventions — Peek Chat

> 版本: v1.0 | 2026-08-06
> 本文档从 AGENTS.md 提取并扩展，Agent 和人类开发者必须遵守。

---

## Kotlin 代码风格

- **官方风格**: `kotlin.code.style=official`（IntelliJ 默认）
- **注释**: 英文
- **惯用法优先**: data class、sealed interface、extension function
- **禁止 `!!`**: 使用 `?.`、`requireNotNull()`、`checkNotNull()` 并附带错误消息
- **不可变性优先**: 参数和属性默认 `val`，集合偏好不可变类型

```kotlin
// ✅ 好
fun findUser(id: String): User {
    return userRepo.findById(id) ?: throw NotFoundException("User not found: $id")
}

// ❌ 差
fun findUser(id: String): User {
    return userRepo.findById(id)!!
}
```

---

## Compose UI 规范

- **尺寸单位**: 始终使用 `Dp`（`16.dp`），禁止 raw `Float`
- **主题色**: 通过 `PeekChatTheme` / `MaterialTheme` 访问，禁止硬编码颜色
- **组件组织**: 共享组件放在 `packages/ui/src/commonMain/`，业务组件放在对应 `screen/` 目录
- **预览**: 每个组件/屏幕写 `@Preview`（带 `PeekChatTheme` wrapper）

```kotlin
// ✅ 好
@Composable
fun BubbleCard(message: ChatMessage, modifier: Modifier = Modifier) {
    Card(modifier = modifier.padding(12.dp)) { ... }
}

// ❌ 差
@Composable
fun BubbleCard(message: ChatMessage, modifier: Modifier = Modifier) {
    Card(modifier = modifier.padding(12f)) { ... }  // Float padding!
}
```

---

## 架构约定

### 包依赖方向（不可违反）

```
apps/* → packages/ui → packages/designsystem
       → packages/data → packages/database → packages/model
                       → packages/ai → packages/model
                       → packages/capture → packages/ocr → packages/model
                       → packages/common
```

**规则**:
- `model` 不依赖任何其他包
- 所有包依赖 `model`
- `capture`、`database`、`ocr`、`ai` 互相不依赖
- `data` 聚合 `database` + `ocr` + `ai`
- `ui` 依赖 `data`、`designsystem`
- `apps/*` 仅依赖 `ui`（及平台所需依赖）

### AI Provider 接口

所有 AI 调用通过 `AiProvider` 接口。不允许在 `packages/ui` 或 `packages/data` 中直接调用 AI API。

```kotlin
// packages/ai/src/commonMain/kotlin/com/peekchat/ai/AiProvider.kt
interface AiProvider {
    suspend fun analyze(conversation: Conversation): AnalysisReport
}
```

---

## Git 规范

遵循 [Conventional Commits](https://www.conventionalcommits.org/):

```
type(scope): description
```

**Types**: `build` | `feat` | `fix` | `refactor` | `chore` | `docs` | `test` | `style`

**Scopes**: `android` | `model` | `common` | `capture` | `database` | `ocr` | `ai` | `data` | `designsystem` | `ui` | `gradle`

**规则**:
- 祈使语气（"add" not "added"）
- 小写开头，不用句号
- 首行 ≤ 72 字符
- 正文与标题间空一行

**细粒度 commit**: 每个逻辑单元独立 commit，禁止 force push。每功能一个分支: `agent/feature-name`

---

## 命名约定

| 类型 | 约定 | 示例 |
|------|------|------|
| 文件 | PascalCase | `BubbleCard.kt` |
| 类/接口 | PascalCase | `class ConversationRepository` |
| 函数/方法 | camelCase | `fun findById(id: String)` |
| 常量 | UPPER_SNAKE_CASE | `const val MAX_IMAGE_COUNT = 10` |
| 包 | lowercase | `com.peekchat.model` |

---

## 错误处理

- **可恢复错误**: 使用 `Result<T>` 或 sealed class 返回值
- **不可恢复错误**: 使用 `requireNotNull` / `checkNotNull` 附带描述性消息
- **异常传播**: 在 Android actual 实现中正确处理平台异常

```kotlin
sealed class AnalysisResult {
    data class Success(val report: AnalysisReport) : AnalysisResult()
    data class Error(val message: String, val cause: Throwable? = null) : AnalysisResult()
}
```

---

## 测试约定

- 测试文件与源文件同目录结构，放在 `src/commonTest/` 或 `src/androidUnitTest/`
- 测试命名: `类名Test.kt`（如 `BubbleClassifierTest.kt`）
- 详见 `docs/testing.md`

---

## 文档约定

- **代码改动前先更新文档**: 任何功能变更必须先反映到 `docs/PRD.md` → `docs/ARCHITECTURE.md`（如涉及）
- **组件变更**: 更新 `docs/design/COMPONENTS.md`
- **新设计决策**: 记录在对应包目录的 `DESIGN_NOTES.md` 中
- **执行计划**: 放在 `docs/plans/` 目录下
