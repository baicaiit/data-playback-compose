import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.6.10"
  id("org.jetbrains.compose") version "1.1.1"
}

group = "cc.hyoban"
version = "1.0"

repositories {
  google()
  mavenCentral()
  maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  maven("https://jitpack.io")
}

dependencies {
  implementation(compose.desktop.currentOs)

  implementation("org.apache.poi:poi-ooxml:5.2.2")
  implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.3.0")
  implementation("io.netty:netty-all:4.1.76.Final")
  implementation("org.apache.kafka:kafka-clients:3.1.0")
  implementation("com.github.Dansoftowner:jSystemThemeDetector:3.6")
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "17"
}

compose.desktop {
  application {
    mainClass = "MainKt"
    jvmArgs += listOf("-Dapple.awt.application.appearance=system")
    nativeDistributions {
      includeAllModules = true
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "DataPlayback"
      packageVersion = "1.0.0"
      windows {
        dirChooser = true
        perUserInstall = true
        menuGroup = "DataPlayback"
      }
    }
  }
}