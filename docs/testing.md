# Testing Strategy — Peek Chat

> 版本: v1.0 | 2026-08-06
> 测试策略遵循 Harness Engineering 原则：将测试作为 Agent 可执行的行为契约。

---

## 测试金字塔

```
         ╱ E2E ╲         Playwright / UI Automator（少量，关键路径）
        ╱集成测试╲        Android instrumented tests
       ╱──────────╲
      ╱  单元测试   ╲      commonTest (KMP) + androidUnitTest
     ╱──────────────╲
```

---

## 层次定义

### 单元测试 (commonTest / androidUnitTest)

**覆盖**: 每个 `commonMain` 包的核心逻辑

| 包 | 测试重点 | 优先级 |
|----|---------|-------|
| `model` | 数据类序列化/反序列化、模型转换 | P0 |
| `ocr` | BubbleClassifier 归属逻辑、ConversationStitcher 去重算法 | P0 |
| `ai` | PromptBuilder 输出格式、AiProvider 接口契约 | P0 |
| `database` | Repository 接口契约 | P1 |
| `data` | Repository 编排逻辑 | P1 |
| `common` | 扩展函数正确性 | P2 |

**框架**: `kotlin.test` (KMP 内置) + 手动 DI mock（通过接口）

```kotlin
// packages/ocr/src/commonTest/kotlin/com/peekchat/ocr/BubbleClassifierTest.kt
class BubbleClassifierTest {
    @Test
    fun `LEFT bubble when horizontal center below 50 percent of screen width`() {
        val result = BubbleClassifier.classify(
            boundingBox = Rect(x = 100, width = 720),
            screenWidth = 1080
        )
        assertEquals(BubblePosition.LEFT, result)
    }

    @Test
    fun `RIGHT bubble when horizontal center above 50 percent of screen width`() {
        val result = BubbleClassifier.classify(
            boundingBox = Rect(x = 600, width = 720),
            screenWidth = 1080
        )
        assertEquals(BubblePosition.RIGHT, result)
    }
}
```

### 集成测试 (android instrumented)

**覆盖**: Android actual 实现

| 包 | 测试重点 |
|----|---------|
| `database` | Room DAO 查询、迁移 |
| `ocr` | ML Kit 真实 OCR 引擎（样本图片） |
| `capture` | MediaProjection 采集流程 |

**通过 "先手动、后自动化" 策略逐步构建**:
1. 手动验证功能正常
2. 将验证脚本化（Android instrumented tests）
3. 将测试编入 CI

### E2E 测试 (optional, Phase 2)

覆盖完整用户流程: 采集 → OCR → AI 分析 → 报告展示

---

## 质量门

### 本地开发门

```bash
# 编译全量元数据
./gradlew compileKotlinMetadata

# Android 编译
./gradlew :apps:android:compileDebugKotlinAndroid

# 运行所有测试
./gradlew check

# Lint
./gradlew lint
```

### CI 门 (`.github/workflows/build.yml`)

```yaml
# CI 阶段: build → test → lint
# 所有阶段通过才允许合并
```

---

## Agent 测试约定

### 当 Agent 写测试时

1. **测试即规范**: 测试描述业务行为，不是实现细节
2. **一个概念一个断言**: 每个 `@Test` 只验证一件事
3. **命名**: `` `应该xxx当yyy时` `` 格式（中文/英文统一）
4. **有修改必有测试**: Agent 修改了代码逻辑 → 同时提供测试

### 当 Agent 运行测试时

```bash
# 运行特定包的测试
./gradlew :packages:ocr:check

# 运行所有测试
./gradlew check
```

---

## 当前测试覆盖状态

| 包 | 测试文件 | 状态 |
|----|---------|------|
| `model` | — | ❌ 未开始 |
| `ocr` | — | ❌ 未开始 |
| `ai` | — | ❌ 未开始 |
| `database` | — | ❌ 未开始 |
| `data` | — | ❌ 未开始 |
| `capture` | — | ❌ 未开始 |
| `common` | — | ❌ 未开始 |
| `designsystem` | — | ❌ 未开始 |
| `ui` | — | ❌ 未开始 |

**下一步**: 从 P0 包开始补充测试（`model` → `ocr` → `ai`）
