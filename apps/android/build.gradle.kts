import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

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

        // Read DeepSeek config from local.properties (private, NEVER committed).
        // Fallback to env var, then placeholder. local.properties is gitignored.
        val localProps = Properties()
        val localFile = rootProject.file("local.properties")
        if (localFile.exists()) {
            localFile.inputStream().use { localProps.load(it) }
        }
        fun localOrEnv(key: String, envKey: String, default: String): String {
            return localProps.getProperty(key)
                ?: System.getenv(envKey)
                ?: default
        }
        val deepseekApiKey = localOrEnv("deepseek.apikey", "DEEPSEEK_API_KEY", "sk-placeholder")
        buildConfigField("String", "DEEPSEEK_API_KEY", "\"$deepseekApiKey\"")

        val deepseekBaseUrl = localOrEnv("deepseek.baseurl", "DEEPSEEK_BASE_URL", "https://api.deepseek.com")
        buildConfigField("String", "DEEPSEEK_BASE_URL", "\"$deepseekBaseUrl\"")

        val deepseekModel = localOrEnv("deepseek.model", "DEEPSEEK_MODEL", "deepseek-chat")
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
