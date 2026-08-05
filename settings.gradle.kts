rootProject.name = "peek-chat"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

// ── packages ──────────────────────────────────────────────
include(":packages:model")
include(":packages:common")
include(":packages:capture")
include(":packages:database")
include(":packages:ocr")
include(":packages:ai")
include(":packages:data")
include(":packages:designsystem")
include(":packages:ui")

// ── apps ──────────────────────────────────────────────────
include(":apps:android")
