plugins {
  kotlin("multiplatform") version "1.6.10" apply true
  id("io.kotest.multiplatform") version "5.0.2" apply true
}

group "org.example"
version "1.0"

repositories {
  mavenCentral()
}

kotlin {
  jvm()
  js(IR) {
    browser()
    nodejs()
  }
  linuxX64()
  mingwX64()

  sourceSets {
    all {
      languageSettings.optIn("kotlin.time.ExperimentalTime")
    }
    commonMain {
      dependencies {
        implementation(kotlin("stdlib-common"))
        implementation("io.arrow-kt:arrow-core:1.0.1")
        implementation("io.arrow-kt:arrow-optics:1.0.1")
        implementation("io.arrow-kt:arrow-fx-coroutines:1.0.1")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-RC2")
      }
    }
    commonTest {
      dependencies {
        implementation("io.kotest:kotest-property:5.0.2")
        implementation("io.kotest:kotest-framework-engine:5.0.2")
        implementation("io.kotest:kotest-assertions-core:5.0.2")
      }
    }
  }
}
