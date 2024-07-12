// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
}

// Add this block to define the Compose version
buildscript {
    extra.apply {
        set("compose_version", "1.5.4")
    }
    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.43.2")
    }
}