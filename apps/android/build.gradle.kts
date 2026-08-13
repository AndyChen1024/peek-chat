import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.application)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(projects.packages.ui)
            implementation(projects.packages.data)
            implementation(projects.packages.ai)
            implementation(projects.packages.designsystem)
            implementation(libs.activity.compose)
            implementation(libs.core.ktx)
            implementation(libs.ktor.client.okhttp)
        }
    }
}

android {
    namespace = "com.peekchat.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.peekchat.android"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"

        // Read DeepSeek API key from gradle.properties
        val deepseekApiKey = project.findProperty("deepseek.apikey") as? String
            ?: System.getenv("DEEPSEEK_API_KEY")
            ?: "sk-placeholder"
        buildConfigField("String", "DEEPSEEK_API_KEY", "\"$deepseekApiKey\"")

        // Read DeepSeek API base URL from gradle.properties (default: official api.deepseek.com)
        val deepseekBaseUrl = project.findProperty("deepseek.baseurl") as? String
            ?: "https://api.deepseek.com"
        buildConfigField("String", "DEEPSEEK_BASE_URL", "\"$deepseekBaseUrl\"")

        // Read DeepSeek model name (default deepseek-chat)
        val deepseekModel = project.findProperty("deepseek.model") as? String
            ?: "deepseek-chat"
        buildConfigField("String", "DEEPSEEK_MODEL", "\"$deepseekModel\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
    }
}
