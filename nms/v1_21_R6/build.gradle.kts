import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.convention.paperweight)
}

dependencies {
    paperweight.paperDevBundle("1.21.10-R0.1-SNAPSHOT")
}

// These older dev bundles pin codebook 1.0.14/1.0.15, whose bundled ASM cannot read Java 25
// class files and dies with "Unsupported class file major version 69" when paperweight
// regenerates the remapped Minecraft jar under a JDK 25 launcher. Run paperweight's setup on
// the same JDK this module already targets via options.release / jvmTarget below.
paperweight.javaLauncher = extensions
    .getByType<org.gradle.jvm.toolchain.JavaToolchainService>()
    .launcherFor { languageVersion = JavaLanguageVersion.of(21) }

tasks {
    compileJava {
        options.release = 21
    }
    compileKotlin {
        compilerOptions.jvmTarget = JvmTarget.JVM_21
    }
}
