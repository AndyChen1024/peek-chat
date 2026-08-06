# AGENTS.md

This file provides concise, agent-focused guidance for working in this monorepo. It consolidates project conventions, architecture, and best practices for both human developers and AI agents.

## Project overview

- Kotlin Multiplatform (KMP) monorepo targeting Android, with Compose Multiplatform for shared UI.
- App: `apps/android` – Android application entry point.
- Shared packages (9 modules under `packages/`):

| Package | Purpose |
|---|---|
| `model` | Domain models and entities |
| `common` | Shared utilities and extensions |
| `capture` | Screenshot capture and processing |
| `database` | Room database layer |
| `ocr` | OCR text recognition (ML Kit) |
| `ai` | AI/LLM integration |
| `data` | Repository and data access layer |
| `designsystem` | Design tokens and theme |
| `ui` | Shared Compose Multiplatform UI components and screens |

## Setup commands

```bash
# Android (from root)
./gradlew :apps:android:assembleDebug

# Install on connected device/emulator
./gradlew :apps:android:installDebug

# Run all tests
./gradlew check

# Clean build
./gradlew clean
```

## Git commit conventions

Follow [Conventional Commits](https://www.conventionalcommits.org/) (same as Folo):

```
type(scope): description
```

**Types**: `build` | `feat` | `fix` | `refactor` | `chore` | `docs` | `test` | `style`

**Scopes** (use the module/package name):
- `android` — the Android app
- `model`, `common`, `capture`, `database`, `ocr`, `ai`, `data`, `designsystem`, `ui` — one per package
- `gradle` — build system and dependency changes
- Omit scope for cross-cutting changes

**Examples**:
```
build(gradle): migrate to compilerOptions DSL
feat(capture): add floating screenshot button overlay
fix(ui): replace Float padding with Dp in Compose
refactor(android): move sources to androidMain convention
chore: configure gitignore and gitattributes
```

**Rules**:
- Use imperative mood ("add" not "added")
- Lowercase subject, no trailing period
- Keep the first line under 72 characters
- Separate body from subject with a blank line when more detail is needed

## Code style and conventions

- Kotlin official code style (`kotlin.code.style=official`).
- Comments in English.
- Prefer Kotlin idioms: data classes, sealed interfaces, extension functions.
- Target JVM 17 for Android; avoid `!!` (use safe calls or `requireNotNull` with message).
- Compose UI: use `Dp`-based values (e.g., `16.dp`), never raw `Float` for dimensions.
- Organize shared, reusable Compose components in `packages/ui/src/commonMain/`.

## Architecture quick reference

- **UI**: Compose Multiplatform (shared across targets via `packages/ui`).
- **Database**: Room 2.6+ with KSP, defined in `packages/database`.
- **DI**: Manual dependency injection via interfaces; keep it simple.
- **Networking**: Ktor Client (OkHttp engine on Android), in `packages/data`.
- **OCR**: ML Kit Text Recognition (Chinese), in `packages/ocr`.
- **Settings**: Multiplatform Settings (`com.russhwolf:multiplatform-settings`).
- **Build**: Gradle 8.11 with Kotlin 2.1, AGP 8.7, version catalog at `gradle/libs.versions.toml`.

## Module dependency flow

```
apps/android → packages/ui → packages/designsystem
            → packages/data → packages/database → packages/model
                            → packages/ai
                            → packages/capture → packages/ocr
                            → packages/common
```

## Quality gates

Run before committing:

```bash
# Compile all modules (catches Kotlin + Compose errors)
./gradlew compileKotlinMetadata

# Android-specific checks
./gradlew :apps:android:compileDebugKotlinAndroid

# Lint
./gradlew lint

# Tests (when added)
./gradlew check
```

## Git configuration

- `.gitignore`: covers Gradle, IDE, Android, Kotlin, environment files, logs, OS clutter.
- `.gitattributes`: enforces LF line endings (`* text=auto eol=lf`) for cross-platform consistency.
- Never commit: `.gradle/`, `build/`, `.idea/`, `local.properties`, `.env`, `.kotlin/`.

## Agent workflow

- Status updates: provide brief progress notes when running tool batches.
- Prefer semantic code search (`grep` by symbol/class name) to explore unfamiliar areas.
- Default to parallelizing independent searches/reads.
- Keep edits minimal and targeted; preserve existing indentation.
- When editing Kotlin, avoid `!!`; use safe calls or explicit null checks.
- For Compose UI, always use `Dp` values for dimensions, never raw numbers.
- Follow the Conventional Commits format defined above for all commits.

## Workflow (Harness Engineering)

**Every feature starts here. No code until the plan exists.**

```
docs/PRD.md → docs/ARCHITECTURE.md → 模块划分 → 任务拆解 → docs/plans/*.md
```

1. Confirm the feature exists in `docs/PRD.md` (or add it)
2. Locate the responsible module in `docs/ARCHITECTURE.md`
3. Break down into tasks in `docs/plans/<YYYY-MM-DD-feature.md>`
4. **Update docs before changing code** — design decisions go to docs first

## Docs map (progressive disclosure)

This file is the map — keep it ~100 lines. Full reference documents live under `docs/`:

| Document | Content | When to read |
|----------|---------|--------------|
| `docs/PRD.md` | Product requirements, user personas, core features, MVP scope | **Always first** — every feature starts here |
| `docs/ARCHITECTURE.md` | Full architecture, tech stack, data models, module dependency flow | Before any cross-module change |
| `docs/conventions.md` | Kotlin/Compose coding standards, naming, error handling, Git rules | Before writing any code |
| `docs/testing.md` | Testing strategy, quality gates, agent test conventions | Before writing or modifying tests |
| `docs/tech-debt.md` | Known technical debt items, severity, repayment plan | Before touching code with known debt |
| `docs/HARNESS-ENGINEERING.md` | OpenAI's agent-first engineering methodology | Understand **why** this project is structured this way |
| `docs/plans/` | Execution plans — task breakdowns per feature | Before implementing any feature |
| `docs/design/COMPONENTS.md` | UI component specs (BubbleCard, cards, FloatOverlay, screens) | Before UI work |
| `docs/design/DESIGN-TOKENS.md` | Design tokens (colors, typography, spacing) | Before UI work |
| `docs/design/INTERACTION-MODEL.md` | FloatOverlay three-state UX spec | Before floating window work |

Rule: if you need deeper context on anything mentioned here, follow the pointer into `docs/` rather than guessing. This is the article's core pattern: **a map, not a 1,000-page manual.**
