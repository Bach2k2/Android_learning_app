plugins {
    id("com.android.application") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "2.1.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.10" apply false
}
val compileSdkVersion by extra(36)
val targetCompatibility by extra(JavaVersion.VERSION_21)
val targetCompatibility1 by extra(targetCompatibility)
