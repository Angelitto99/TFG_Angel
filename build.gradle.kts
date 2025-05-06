// build.gradle (raíz del proyecto)
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // El plugin de Android y Kotlin que ya usas
        classpath("com.android.tools.build:gradle:8.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.10")
        // → Añadido: Google Services (Firebase)
        classpath("com.google.gms:google-services:4.3.15")
    }
}

plugins {
    // Estas líneas deben coincidir con las versiones de tu proyecto
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    // → Definimos aquí el plugin de Google Services sin activarlo en todos los módulos
    id("com.google.gms.google-services") version "4.4.0" apply false
}
