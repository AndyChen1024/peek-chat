<div align="center">
  <h1>Peek Chat · 眯聊</h1>
  <p><i>聊天时眯一眼，重要的就留下了。</i></p>
  <p>
    <img src="https://img.shields.io/badge/license-GPL%203.0-blue?style=flat-square" alt="License" />
    <img src="https://img.shields.io/badge/platform-Android-green?style=flat-square&logo=android" alt="Android" />
    <img src="https://img.shields.io/badge/kotlin-multiplatform-purple?style=flat-square&logo=kotlin" alt="Kotlin" />
  </p>
</div>

---

## 这是什么

浮窗常驻，进微信聊天的同时，**一键收录整段对话**。自动帮你总结、提炼待办、梳理情绪、记录决策——不用手动整理，不用事后翻截图。

微信里每天有值得记住的东西：约好的时间、讨论出的决定、该做的事、一段重要的对话。但它们散落在几百条消息里，聊完就沉了。眯聊的思路很简单：**聊的时候顺便记下来，而不是事后补救。**

---

## 怎么用

### 方式 A：浮窗采集（推荐）

眯聊在后台保持一个浮窗。进微信，打开你要收录的群聊或对话——

- 点浮窗 → 自动截图、滚动、采集整段对话
- 回到眯聊 → 分析报告已经生成好了

你只需要点一下。

### 方式 B：手动导入

截图已经在相册里了？选几张丢进来，一样能分析。

---

## 能帮你做什么

- **对话摘要** — 这段聊了什么，一段话讲清楚
- **待办提取** — 谁要做什么，自动标注负责人
- **情绪洞察** — 整体基调 + 正面和负面话题分别列出来
- **关键决策** — 定了什么事，谁参与的
- **历史留存** — 所有记录在本地，随时翻出来看

---

## 隐私

> 截图不离开你的手机。

| 步骤 | 方式 | 隐私保障 |
|------|------|---------|
| 截图采集 | 系统截图 API | 不调用微信任何接口 |
| 文字提取 | ML Kit 离线 OCR | 图片不出设备 |
| AI 分析 | 仅发送纯文本 | 不传原图，只传结构化文字 |
| 数据存储 | Room 本地数据库 | 无账户系统，全在手机上 |

---

## 架构

眯聊的架构体现了 **AI Skill 模式**——把 AI 能力封装为独立、职责清晰的分层模块。

```
采集层（浮窗/手动）→ 预处理（OCR/拼接）→ 编排层（AI）→ 渲染层（报告）
                           ↓
                      持久层（本地存储）
```

| 层 | 职责 | 眯聊实现 |
|------|------|---------|
| 采集层 | 获取对话数据 | 浮窗自动截图 + 手动导入双通道 |
| 预处理 | 提取文字、识别双方、拼接去重 | ML Kit OCR → 气泡归属 → 多图拼接 |
| 编排层 | 构造上下文，调用模型 | PromptBuilder → AiProvider |
| 渲染层 | 结构化呈现 | 摘要 / 待办 / 情绪 / 决策卡片 |
| 持久层 | 本地留存 | Room，随时回溯 |

Provider 可替换，编排层可对接——架构保持开放。

### 主要技术选型

| 层 | 选型 |
|---|---|
| 跨平台 | Kotlin Multiplatform + Compose Multiplatform |
| 浮窗 | Android Overlay API |
| 自动采集 | MediaProjection + AccessibilityService |
| OCR | ML Kit Text Recognition v2 |
| AI | DeepSeek API（OpenAI-compatible） |
| 网络 | Ktor Client |
| 数据库 | Room |
| 序列化 | kotlinx.serialization |

> 详见 [ARCHITECTURE.md](docs/ARCHITECTURE.md)

---

## 项目结构

```
peek-chat/
├── packages/
│   ├── model/          # 数据模型
│   ├── database/       # 持久化
│   ├── capture/        # 浮窗 + 自动采集 + 手动导入
│   ├── ocr/            # 文字提取 + 气泡归属 + 拼接
│   ├── ai/             # AI Provider + Prompt
│   ├── data/           # Repository 聚合
│   ├── designsystem/   # Material 3 主题
│   ├── ui/             # Screen + 业务组件
│   └── common/         # 工具扩展
├── apps/
│   ├── android/        # Android（浮窗 + 自动采集）
│   ├── ios/            # iOS（future）
│   └── desktop/        # Desktop（future）
├── docs/
│   └── ARCHITECTURE.md
└── .github/workflows/
```

---

## Roadmap

| 阶段 | 内容 |
|------|------|
| Phase 1 | Android MVP — 浮窗采集 + 手动导入 + 分析 |
| Phase 2 | iOS 端（Vision OCR + URLSession） |
| Phase 3 | Desktop 端（macOS / Windows / Linux） |
| Phase 4 | 记录管理、搜索、导出 |
| Phase 5 | 多 AI Provider + 编排层集成 |

---

## 快速开始

```bash
# 前置：JDK 17+，Android Studio，Android SDK 36

./gradlew :apps:android:assembleDebug
# APK → apps/android/build/outputs/apk/debug/
```

---

## License

[GNU General Public License v3.0](LICENSE) · Copyright © 2025 Peek Chat Contributors
