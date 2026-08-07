# Permission Guide Card — Design Spec

> 版本: v1.0 | Iris | 2026-08-07
> 眯聊首次浮窗权限引导的 UI 规格。

---

## 概述

Android 的 `SYSTEM_ALERT_WINDOW` 权限需要用户手动在系统设置中开启，App 无法静默授予。本规格定义首次权限引导的两个状态：

1. **引导卡片** — 用户首次打开 App，权限未开启时的说明弹窗
2. **降级页面** — 用户拒绝权限后，App 内展示的手动导入入口

---

## 1. 引导卡片 (PermissionGuideCard)

### 触发条件
- App 首次启动
- 检测 `SYSTEM_ALERT_WINDOW` 未开启
- 用户尚未被引导过（SharedPreferences 记录）

### 布局规格

```
┌──────────────────────────────────┐
│                                  │
│      [插图：微信聊天界面 +      │
│       屏幕边缘浮窗小圆点]        │
│                                  │
│     在微信上面放一个小圆点       │
│     点一下就能记录这段对话       │
│                                  │
│   眯聊需要在其他应用上层显示     │
│   浮窗，系统要求你手动开启一次   │
│                                  │
│     ┌──────────────────────┐     │
│     │      去开启           │     │
│     └──────────────────────┘     │
│         以后再说                 │
└──────────────────────────────────┘
```

### 元素规格

| 元素 | 规格 |
|------|------|
| 卡片 | `surface` 背景，`radius-md` 圆角，`shadow-md` 阴影 |
| 插图 | 顶部居中，展示微信对话 + 浮窗 pill 的效果预览 |
| 主标题 | "在微信上面放一个小圆点，点一下就能记录这段对话"，18px/600 |
| 副标题 | "眯聊需要在其他应用上层显示浮窗，系统要求你手动开启一次"，13px/400，`text-secondary` |
| 主按钮 | "去开启"，`brand-700` 背景，白色文字，`radius-full` pill 按钮，点击跳 `ACTION_MANAGE_OVERLAY_PERMISSION` |
| 次按钮 | "以后再说"，`text-secondary` 文字链接，无背景，点击关闭弹窗并记录已引导 |

### 交互
- 点击"去开启" → 跳转系统设置页
- 从设置页返回 → 重新检测权限 → 开启则启动浮窗 / 未开启则显示降级页面
- 点击"以后再说" → 关闭弹窗，显示降级页面
- 用户拒绝 2 次后不再自动弹出引导卡片（Android 限频），改为常驻降级页面

---

## 2. 降级页面 (DeniedFallbackPage)

### 触发条件
- 用户拒绝或未开启浮窗权限
- 用户点击"以后再说"

### 布局规格

```
┌──────────────────────────────────┐
│  ← 眯聊                          │
│──────────────────────────────────│
│                                  │
│         [图标：浮窗禁用态]       │
│                                  │
│      浮窗采集功能未开启           │
│                                  │
│   你可以手动导入微信截图，        │
│   或者在设置中开启浮窗权限        │
│                                  │
│    ┌──────────────────────────┐  │
│    │   从相册选择截图          │  │
│    └──────────────────────────┘  │
│    ┌──────────────────────────┐  │
│    │   开启浮窗权限            │  │
│    └──────────────────────────┘  │
│                                  │
│    ───── 历史记录 ─────          │
│    [历史记录列表...]             │
└──────────────────────────────────┘
```

### 元素规格

| 元素 | 规格 |
|------|------|
| 顶部栏 | "眯聊" + 返回 |
| 图标 | 浮窗禁用态插画（灰色、半透明 pill + X） |
| 标题 | "浮窗采集功能未开启"，18px/600 |
| 说明 | "你可以手动导入微信截图，或在设置中开启浮窗权限"，13px/400，`text-secondary` |
| 主按钮 | "从相册选择截图"，`brand-700` 背景，白色文字，`radius-full` |
| 次按钮 | "开启浮窗权限"，outlined，`brand-700` 描边，`radius-full`，点击跳系统设置 |
| 历史区域 | 分割线 + "历史记录"，下方 LazyColumn 历史列表（同 HistoryScreen） |

### 交互
- "从相册选择截图" → 打开系统相册选择器（手动导入路径，P0 功能）
- "开启浮窗权限" → 重新跳转系统设置页
- 权限开启后 → 自动切换到浮窗模式，降级页面替换为正常的采集页面

---

## 3. Compose 实现要点

```kotlin
// PermissionGuideDialog — 引导卡片（Modal Dialog）
@Composable
fun PermissionGuideDialog(
    onGoToSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    // Card + Column 布局
    // 插图 → 主标题 → 副标题 → 按钮组
    // onGoToSettings: context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, ...))
    // onDismiss: 记录引导状态，显示降级页面
}

// DeniedFallbackScreen — 降级页面
@Composable
fun DeniedFallbackScreen(
    onPickImages: () -> Unit,
    onGoToSettings: () -> Unit,
    onViewHistory: (String) -> Unit,
    historyList: List<Conversation>
) {
    // Scaffold + Column
    // 图标 + 标题 + 说明 + 按钮组 + 分割线 + 历史列表
}
```

### 检测逻辑（Bram 实现）

```
App 启动 → 检测 SYSTEM_ALERT_WINDOW 已开？
  YES → 正常显示采集页 + 启动 OverlayService
  NO  → 检测是否已被引导过？
           YES → 直接显示降级页面（不再弹引导卡片）
           NO  → 显示引导卡片 → 用户选择
                    去开启 → 跳系统设置 → onResume 重检
                    以后再说 → 记录已引导 → 显示降级页面
```
