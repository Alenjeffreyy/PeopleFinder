// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        mavenCentral()
        google()
        maven { url=uri("https://jitpack.io") }
    }

    dependencies {
        classpath(libs.android.gradle)
        classpath(libs.android.apt)
        classpath(libs.android.maven.gradle)
        classpath(libs.google.services)
        classpath(libs.firebase.crashlytics)
        classpath(libs.kotlin.gradle.plugin)
    }
}
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.service) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}