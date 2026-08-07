package com.peekchat.designsystem.theme

import androidx.compose.ui.graphics.Color

// ── Brand palette: Slate Blue ───────────────────────────────────────
// "静谧感" — quiet, trustworthy, doesn't compete for attention

val Brand700 = Color(0xFF475569)   // primary: main buttons, brand identity
val Brand500 = Color(0xFF64748B)   // secondary: icons, secondary elements
val Brand400 = Color(0xFF94A3B8)   // auxiliary: dividers, placeholders
val Brand50 = Color(0xFFF1F5F9)    // page background (light)

// Surfaces
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceBorder = Color(0xFFE2E8F0)

// ── Semantic palette ────────────────────────────────────────────────
// Each analysis dimension gets a distinct color identity

val SemanticTodo = Color(0xFFF59E0B)               // Amber — todo badge, TodoCard border
val SemanticSentimentPositive = Color(0xFF10B981)  // Green — positive sentiment bar
val SemanticSentimentNegative = Color(0xFFF97316)  // Orange — negative sentiment bar
val SemanticDecision = Color(0xFF3B82F6)            // Blue — decision badge, DecisionCard border
val SemanticSummary = Color(0xFF64748B)             // Slate 500 — SummaryCard icon

// Semantic backgrounds (for tags/badges inside cards)
val SemanticTodoBg = Color(0xFFFFFBEB)
val SemanticPositiveBg = Color(0xFFECFDF5)
val SemanticNegativeBg = Color(0xFFFFF7ED)
val SemanticDecisionBg = Color(0xFFEFF6FF)

// ── Dark mode overrides ─────────────────────────────────────────────

val Brand50Dark = Color(0xFF1E293B)   // dark surface
val SurfaceDark = Color(0xFF0F172A)
val SurfaceBorderDark = Color(0xFF334155)
val TextDark = Color(0xFFE2E8F0)

// ── WeChat bubble colors (kept for BubbleCard component) ───────────

val WeChatGreenPrimary = Color(0xFF95EC69)
val WeChatGreenDark = Color(0xFF5B9E3E)
val WeChatWhite = Color(0xFFFFFFFF)
val WeChatGrayLight = Color(0xFFF5F5F5)
