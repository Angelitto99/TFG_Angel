// build.gradle (raíz del proyecto)
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Plugin de Android y Kotlin actualizados
        classpath("com.android.tools.build:gradle:8.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
        // Google Services (Firebase)
        classpath("com.google.gms:google-services:4.3.15")
    }
}

plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
}
