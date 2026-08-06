# Peek Chat（眯聊）— Product Requirements Document

> 版本: v1.0 | 2026-08-06
> 状态: MVP 阶段
>
> **本文档是所有工作的入口点。** 任何功能开发必须从这里出发：
> PRD.md → ARCHITECTURE.md → 模块划分 → 任务拆解 → 执行计划 (`docs/plans/`)

---

## 产品定位

**浮窗常驻，进微信聊天的同时，一键收录整段对话。自动 OCR 提取文字 → AI 分析 → 结构化报告。**

眯聊是一个 Android 端 AI 工具，不侵入微信、不读取数据库，用户主动截图后由 OCR + AI 完成对话分析和结构化提取。

一句话：**微信聊天的 AI 阅读助手。**

---

## 目标用户

| 用户画像 | 场景 | 核心痛点 |
|----------|------|---------|
| 职场人士 | 工作群聊中混杂待办事项和决策 | 爬楼找信息费时，容易遗漏 |
| 项目管理者 | 多群并行沟通，需汇总各方结论 | 信息分散，无结构化记录 |
| 普通用户 | 家庭/朋友群聊中的重要信息 | 聊天记录难检索，信息易淹没 |

---

## 核心功能列表

### P0 — MVP 必须

| ID | 功能 | 描述 | 对应模块 |
|----|------|------|---------|
| F1 | 浮窗常驻 | Android Overlay API 浮窗，微信上层常驻 | `packages/capture` |
| F2 | 一键采集 | 点击浮窗 → MediaProjection 截图 + AccessibilityService 自动滚动 | `packages/capture` |
| F3 | 手动导入 | 从相册选择已有截图 | `packages/ui` |
| F4 | OCR 提取 | ML Kit Text Recognition v2，离线提取中文文本 | `packages/ocr` |
| F5 | 气泡归属 | 基于位置的 Left/Right 判断 | `packages/ocr` |
| F6 | 多图去重 | 连续截图拼接、重叠文本去重 | `packages/ocr` |
| F7 | AI 分析 | DeepSeek API 分析对话：摘要 + 待办 + 情绪 + 决策 | `packages/ai` |
| F8 | 报告展示 | 结构化卡片展示分析结果 | `packages/ui` |
| F9 | 历史记录 | Room 数据库存储，列表查看历史分析 | `packages/database` + `packages/ui` |
| F10 | 隐私保护 | OCR 完全离线，不上传原始截图，AI 仅传纯文本 | 全局约束 |

### P1 — 后续迭代

| ID | 功能 | 描述 |
|----|------|------|
| F11 | iOS 端 | `apps/ios/`，Vision OCR + URLSession |
| F12 | Desktop 端 | Compose Desktop，macOS/Windows/Linux |
| F13 | 对话历史搜索 | 全文搜索历史分析结果 |
| F14 | 报告导出 | Markdown/PDF 导出 |
| F15 | 多 Provider 切换 | 支持切换不同的 AI Provider |
| F16 | Harness 编排 | DeepSeek Harness Skill 拆分+编排 |

---

## 非功能需求

| ID | 需求 | 规格 |
|----|------|------|
| NF1 | 不侵入微信 | 不解密数据库，不读进程内存，不调私有 API |
| NF2 | OCR 离线 | ML Kit 完全本地运行 |
| NF3 | AI 仅传文本 | 截图不上传任何服务器 |
| NF4 | 浮窗不打扰 | 眯着态仅 44dp 半透明圆点，不遮挡微信内容 |
| NF5 | 无账户系统 | 数据全部本地存储 |
| NF6 | 多端架构预留 | `expect`/`actual` 模式，保留 iOS/Desktop 扩展点 |

---

## MVP 范围

### 做
- Android 端（Compose Multiplatform on Android）
- 浮窗三态交互（眯着 / 睁眼 / 看一眼）
- 自动采集（MediaProjection + AccessibilityService）
- 手动导入截图
- ML Kit OCR（中文）
- 气泡归属
- 多图拼接去重
- DeepSeek AI 分析（摘要、待办、情绪、决策）
- 结构化报告展示
- 本地 Room 数据库存储
- 无网络提示

### 不做
- iOS / Desktop 端
- 账户系统
- 离线队列（自动重试）
- 聊天记录搜索
- 批量分析
- 报告导出

---

## 成功指标

| 指标 | 目标 |
|------|------|
| OCR 准确率 | 中文识别 ≥ 90% |
| 气泡归属准确率 | ≥ 95% |
| 单次采集+分析时长 | ≤ 5 秒（3 张截图以内） |
| 浮窗内存占用 | ≤ 50MB |
| AI 分析有用率 | 用户反馈 ≥ 4/5 |

---

## 核心交互流

详见 `docs/design/INTERACTION-MODEL.md`

```
用户刷微信 → 看到想记录的对话
  → 点击浮窗（眯着态）
  → 自动截图+滚动（睁眼态，1-3秒）
  → AI 分析
  → 浮窗展示迷你摘要（看一眼态）
  → 点击进入 App 查看完整报告
```

---

## 技术约束

- **Kotlin 2.1+** / Compose Multiplatform 1.7+
- **Android minSdk 26** (Overlay API 兼容性)
- **JDK 17**
- **OCR**: 完全离线（ML Kit）
- **AI**: DeepSeek API（OpenAI-compatible）
- **数据库**: Room（Android actual）
- **架构**: `apps/*` → `packages/ui` → `packages/data` → `packages/database`/`packages/ai`/`packages/capture` → `packages/model`

详见 `docs/ARCHITECTURE.md`

---

## 参考

- HARNESS-ENGINEERING.md — 本项目的构建方法论
- ARCHITECTURE.md — 技术架构详情
- docs/design/ — UI/UX 规格
- docs/plans/ — 执行计划归档
