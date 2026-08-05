# Peek Chat · 眯聊

> 聊天时眯一眼，重要的就留下了。

浮窗常驻，进微信聊天的同时，一键收录整段对话。自动帮你总结、提炼待办、梳理情绪、记录决策——不用手动整理，不用事后翻截图。

<p align="center">
  <img src="docs/screenshots/preview.png" alt="preview" width="300">
</p>

---

## 为什么做这个

微信里每天有值得记住的东西——约好的时间、讨论出的决定、该做的事、一段重要的对话。但这些东西散落在几百条消息里，聊完就沉了，想找也找不到。

眯聊的思路很简单：**聊的时候顺便记下来，而不是事后补救。**

---

## 怎么用

两种方式，一个结果：

### 方式 A：浮窗采集（推荐）

眯聊在后台保持一个浮窗。进微信，打开你要收录的群聊或对话——

- 点浮窗 → 自动截图、滚动、采集整段对话
- 回到眯聊 → 分析报告已经生成好了

你只需要点一下。剩下的自动完成。

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

## 技术栈

| 层 | 选型 |
|---|---|
| 跨平台 | Kotlin Multiplatform + Compose Multiplatform |
| 浮窗 | Android Overlay API |
| 自动截图 | MediaProjection + AccessibilityService |
| 文字提取 | ML Kit Text Recognition v2 |
| AI | DeepSeek API（OpenAI-compatible） |
| 网络 | Ktor Client |
| 数据库 | Room |
| 序列化 | kotlinx.serialization |

---

## 设计思路

眯聊从一开始就不是一个"调 API 的 App"。

如果只是把对话文字发给 DeepSeek 然后展示结果，整个项目 200 行代码就够了。但那样做出来的东西跟 Chat UI 没有本质区别——你只是在另一个界面里聊天。

眯聊选择了一条更长的路：

- **不绑 Provider。** `AiProvider` 是接口，不是具体实现。今天用 DeepSeek，明天用 OpenAI，后天接入 DeepSeek Harness——改一个文件，不动架构。
- **不绑输入方式。** 浮窗自动采集和手动导入共享同一条预处理管道。将来加通知栏监听、分享扩展，也只加一个采集实现。
- **不绑 AI 用法。** "总结对话摘要"和"提取待办事项"是两个独立的能力单元。编排层出来后，用户能自己组合——先总结还是先提取待办，不用改代码。

这些设计不是为了"工程正确"，而是为了一个很实际的目的：**当 AI 编排框架（如 DeepSeek Harness）发布时，眯聊是现成的移动端 Skill，不是"需要重构才能接入"的半成品。**

---

## 架构设计

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

## 快速开始

```bash
# 前置：JDK 17+，Android Studio，Android SDK 36

./gradlew :apps:android:assembleDebug
# APK → apps/android/build/outputs/apk/debug/
```

---

## Roadmap

| 阶段 | 内容 |
|------|------|
| Phase 1 | Android MVP — 手动导入 + 分析 ✅ |
| Phase 2 | 浮窗自动采集 |
| Phase 3 | iOS 端 |
| Phase 4 | Desktop 端 |
| Phase 5 | 记录管理、搜索、导出 |
| Phase 6 | 多 AI Provider + 编排层集成 |

---

## License

[GNU General Public License v3.0](LICENSE) · Copyright © 2025 Peek Chat Contributors
