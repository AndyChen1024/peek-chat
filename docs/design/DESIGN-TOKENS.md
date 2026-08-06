# Design Tokens — Peek Chat (眯聊)

> 版本: v1.0 | Iris | 2026-08-06
> 本文档定义眯聊的完整 Design Token，作为 `packages/designsystem` 的实施参考。

---

## 设计哲学

眯聊的视觉语言传达三个核心感受：
- **轻量不打扰** — 它是一个工具，不是另一个社交 app
- **信任感** — 隐私是核心卖点，UI 要让人觉得"数据安全在我手机里"
- **一眼看懂** — 分析报告的信息密度高，卡片布局让用户秒懂

基调：Material 3 Expressive + 眯聊独有的"静谧感"。

---

## 品牌色板

主色选用 Slate Blue（暖灰蓝）— 安静、可信、不抢戏。

| Token | 色值 | 用途 |
|-------|------|------|
| `brand-700` | `#475569` | 主色：主要按钮、品牌标识 |
| `brand-500` | `#64748B` | 次色：次要元素、图标 |
| `brand-400` | `#94A3B8` | 辅助：分割线、占位符 |
| `brand-50` | `#F1F5F9` | 背景：页面底色 |
| `surface` | `#FFFFFF` | 卡片：所有卡片容器 |
| `surface-border` | `#E2E8F0` | 卡片描边 |

---

## 语义色板

每个分析维度有独立的色彩身份，扫一眼就知道是什么。

| Token | 色值 | 语义 | 用途 |
|-------|------|------|------|
| `semantic-todo` | `#F59E0B` (Amber) | 待办 | TodoBadge, TodoCard 边框 |
| `semantic-sentiment-positive` | `#10B981` (Green) | 正面情绪 | SentimentBar 正面段 |
| `semantic-sentiment-negative` | `#F97316` (Orange) | 负面情绪 | SentimentBar 负面段 |
| `semantic-decision` | `#3B82F6` (Blue) | 决策/动作 | DecisionBadge, DecisionCard 边框 |
| `semantic-summary` | `#64748B` (Slate 500) | 摘要 | SummaryCard 图标 |

语义色背景（用于卡片内标签/角标）：

| Token | 色值 |
|-------|------|
| `semantic-todo-bg` | `#FFFBEB` |
| `semantic-positive-bg` | `#ECFDF5` |
| `semantic-negative-bg` | `#FFF7ED` |
| `semantic-decision-bg` | `#EFF6FF` |

---

## 间距系统

| Token | 值 | 用途 |
|-------|-----|------|
| `space-xs` | 4px | 微型间距：icon-label 间距 |
| `space-sm` | 8px | 小间距：卡片内元素间距 |
| `space-md` | 12px | 中间距：卡片 padding |
| `space-lg` | 16px | 大间距：页面 padding、卡片间距 |
| `space-xl` | 20px | 超大间距：section 间距 |
| `space-2xl` | 24px | 区块间距 |

---

## 圆角系统

| Token | 值 | 用途 |
|-------|-----|------|
| `radius-sm` | 8px | Chip/Tag, Badge |
| `radius-md` | 12px | Card, Dialog |
| `radius-full` | 999px | Button, Pill |

---

## 阴影系统

| Token | 值 | 用途 |
|-------|-----|------|
| `shadow-sm` | `0 1px 3px rgba(0,0,0,.08)` | 卡片（页面内） |
| `shadow-md` | `0 4px 12px rgba(0,0,0,.1)` | 浮窗、弹出层 |

---

## 字体系统

使用系统默认中文字体（Android: Noto Sans CJK / system fallback），不引入额外字体包。

| Token | Size / Weight | 用途 |
|-------|---------------|------|
| `text-title-lg` | 22px / 700 | 页面大标题 |
| `text-title` | 18px / 600 | Section 标题 |
| `text-heading` | 14px / 600 | 卡片标题 |
| `text-body` | 13px / 400 | 正文 |
| `text-caption` | 11px / 400 | 辅助文字、角标 |

---

## 暗色模式

所有 Token 在暗色模式下有对应值（通过 Material 3 darkColorScheme 实现）：
- `brand-50` → 深色表面 `#1E293B`
- `surface` → `#0F172A`
- `surface-border` → `#334155`
- `text` → `#E2E8F0`
- 语义色保持色相，降低饱和度
