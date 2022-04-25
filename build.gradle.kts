import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.6.10"
  id("org.jetbrains.compose") version "1.0.1"
}

group = "me.hyoban"
version = "1.0"

repositories {
  google()
  mavenCentral()
  maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
  implementation(compose.desktop.currentOs)

  implementation("io.netty:netty-all:4.1.76.Final")
  implementation("org.apache.poi:poi-ooxml:5.2.2")
  implementation("org.apache.kafka:kafka-clients:3.1.0")
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "11"
}

compose.desktop {
  application {
    mainClass = "MainKt"
    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "data-playback-compose"
      packageVersion = "1.0.0"
    }
  }
}