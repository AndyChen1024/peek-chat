# Component Specification — Peek Chat (眯聊)

> 版本: v1.0 | Iris | 2026-08-06
> 本文档定义眯聊所有 UI 组件的规格，作为 `packages/ui` 和 `packages/designsystem` 的实现参考。

---

## 组件总览

| 组件 | 文件位置 | 用途 | 状态 |
|------|---------|------|------|
| BubbleCard | `ui/component/BubbleCard.kt` | 聊天气泡（对话展示） | ✅ 已实现 |
| SummaryCard | `ui/component/SummaryCard.kt` | 对话摘要卡片 | 🔲 待实现 |
| TodoCard | `ui/component/TodoCard.kt` | 待办列表卡片 | 🔲 待实现 |
| SentimentCard | `ui/component/SentimentCard.kt` | 情绪洞察卡片 | 🔲 待实现 |
| DecisionCard | `ui/component/DecisionCard.kt` | 关键决策卡片 | 🔲 待实现 |
| ConversationCard | `ui/component/ConversationCard.kt` | 历史记录列表项 | 🔲 待实现 |
| PeekChatScaffold | `designsystem/component/PeekChatScaffold.kt` | 页面骨架 | 🔲 待实现 |
| LoadingIndicator | `designsystem/component/LoadingIndicator.kt` | 加载指示器 | 🔲 待实现 |
| PermissionGuideDialog | `ui/component/PermissionGuideDialog.kt` | 浮窗权限首次引导弹窗 | 🔲 待实现 |
| DeniedFallbackScreen | `ui/screen/fallback/DeniedFallbackScreen.kt` | 权限未开启降级页面 | 🔲 待实现 |
| FloatOverlay | `apps/android/` | 浮窗三态组件 | 🔲 待实现 |

---

## BubbleCard

**用途**: 展示采集到的微信对话气泡，区分"自己"和"对方"。

**已实现**。当前规格保持不变：
- LEFT (对方): 左对齐，白色背景
- RIGHT (自己): 右对齐，微信绿 (#95EC69) 背景
- 可选时间戳（右下角，caption 字号）

**注意**: 这是对话回放组件，非采集交互。采集完成后在 App 内展示对话历史时使用。

---

## SummaryCard

**用途**: 展示 AI 分析后的对话摘要（一段话讲清楚这段聊了什么）。

| 属性 | 规格 |
|------|------|
| 语义色 | Blue (`#3B82F6`) |
| 布局 | 卡片顶部 icon + 标题行 → 正文段落 |
| 标题 | "对话摘要"，14px / 600 |
| 正文 | 13px / 400，最大 4 行，超出截断 |
| 卡片样式 | surface 背景，radius-md 圆角，shadow-sm 阴影 |

**Compose 伪代码**:
```kotlin
Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = Surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
) {
    Column(modifier = Modifier.padding(12.dp)) {
        Row(verticalAlignment = CenterVertically) {
            Icon(/* summary icon */, tint = Blue)
            Spacer(4.dp)
            Text("对话摘要", style = heading)
        }
        Spacer(8.dp)
        Text(summary, style = body, maxLines = 4)
    }
}
```

---

## TodoCard

**用途**: 展示从对话中提取的待办事项列表。

| 属性 | 规格 |
|------|------|
| 语义色 | Amber (`#F59E0B`) |
| 布局 | 图标 + 标题 → 待办列表（每项：空心圆 + 文字 + 负责人标签） |
| 标题 | "待办事项"，14px / 600 |
| 待办项 | 13px / 400，前置空心圆 checkbox |
| 负责人标签 | caption 字号，amber-bg 背景圆角标签 |

**每条待办包含**:
- 待办文字
- 负责人（从对话中提取，以 tag 形式显示，如"@小李"）

**Compose 伪代码**:
```kotlin
Card(...) {
    Column {
        // Header row with Amber icon + "待办事项"
        // For each todo:
        Row {
            Icon(/* empty circle checkbox */)
            Text(todo.description)
            Spacer(weight = 1f)
            Text("@${todo.owner}", style = caption, background = AmberBg)
        }
    }
}
```

---

## SentimentCard

**用途**: 展示对话的整体情绪基调 + 正负面话题分布。

| 属性 | 规格 |
|------|------|
| 语义色 | Green (`#10B981`) / Orange (`#F97316`) |
| 布局 | 图标 + 标题 → 情绪分布条 → 百分比标注 |
| 标题 | "情绪洞察"，14px / 600 |
| 分布条 | 3 段色条：Green (正面) / Slate 400 (中性) / Orange (负面) |
| 标注 | caption 字号，标注各段百分比 |

**情绪分布条**: 水平条，各段宽度按百分比分配。下方标注如"正面 75% · 中性 15% · 负面 10%"。

---

## DecisionCard

**用途**: 展示对话中识别出的关键决策。

| 属性 | 规格 |
|------|------|
| 语义色 | Blue (`#3B82F6`) |
| 布局 | 图标 + 标题 → 决策描述文本 |
| 标题 | "关键决策"，14px / 600 |
| 正文 | 13px / 400 |

---

## ConversationCard

**用途**: 历史记录列表中的每一项。

| 属性 | 规格 |
|------|------|
| 布局 | Row: 群组头像（圆形 placeholder） + 中间文字列 + 右侧角标 |
| 文字列 | 群名/对话名（13px / 600）+ 日期 + 消息数 （11px / caption） |
| 角标 | 待办数（amber 圆角标签）或决策数（blue 圆角标签） |

---

## FloatOverlay

**用途**: Android 浮窗三态组件。这是眯聊最独特的设计组件。

详见 `INTERACTION-MODEL.md`。

**三态**:
1. **眯着**: 44dp 半透明圆点，Slate #475569 @ 60% alpha，右边缘吸附
2. **睁眼**: 120dp 浮窗卡片，环形进度 + "采集对话中..."
3. **看一眼**: 180dp 迷你摘要卡片，红点 badge + 一句话摘要 + 统计数据

**Android 实现约束**:
- 使用 `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`
- 最小触摸区域 48dp (Android accessibility 要求)
- 拖拽使用 `OnTouchListener` + `WindowManager.updateViewLayout`
- 边缘吸附动画使用 `ValueAnimator`

---

## Screen 级别组件

### CaptureScreen
- 截图选择网格（3 列）
- 缩略图 item：aspectRatio 1:1，选中态蓝色边框
- "开始分析"主按钮（slate-dark 背景，圆角 pill）
- "查看历史记录"次按钮（outlined）

---

## PermissionGuideDialog

**用途**: 首次启动时引导用户开启浮窗权限。

详见 `PERMISSION-GUIDE.md`。

| 属性 | 规格 |
|------|------|
| 布局 | 居中 Modal Card: 插图 → 主标题 → 副标题 → 主按钮 → 次文字按钮 |
| 卡片 | surface 背景，radius-md，shadow-md |
| 插图 | 微信聊天界面 + 屏幕边缘浮窗 pill 预览（场景化展示） |
| 主标题 | "在微信上面放一个小圆点，点一下就能记录这段对话"，18px/600 |
| 副标题 | "眯聊需要在其他应用上层显示浮窗，系统要求你手动开启一次"，13px/400，text-secondary |
| 主按钮 | "去开启"，brand-700 bg，white text，radius-full |
| 次按钮 | "以后再说"，text-secondary 文字链接，无背景 |

---

## DeniedFallbackScreen

**用途**: 用户拒绝浮窗权限后的降级页面，提供手动导入入口。

详见 `PERMISSION-GUIDE.md`。

| 属性 | 规格 |
|------|------|
| 布局 | Scaffold: 图标 + 标题 + 说明 + 双按钮 + 分割线 + 历史列表 |
| 图标 | 浮窗禁用态插画（灰色 pill + X） |
| 标题 | "浮窗采集功能未开启"，18px/600 |
| 说明 | "你可以手动导入微信截图，或在设置中开启浮窗权限"，13px/400 |
| 主按钮 | "从相册选择截图"，brand-700 bg，white text，radius-full |
| 次按钮 | "开启浮窗权限"，outlined，brand-700 描边，radius-full |
| 历史列表 | 分割线 + "历史记录" + LazyColumn

### AnalysisScreen
- SummaryCard → TodoCard → SentimentCard → DecisionCard 垂直排列
- 页面 padding: 16dp
- 卡片间距: 12dp

### HistoryScreen
- LazyColumn + ConversationCard
- 每项间距 8dp
