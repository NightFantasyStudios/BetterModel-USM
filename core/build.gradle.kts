plugins {
    alias(libs.plugins.convention.publish)
}

dependencies {
    api(project(":bettermodel-api"))

    compileOnly(libs.bundles.minecraft)
    compileOnly("com.mojang:authlib:7.0.61")

    compileOnly(libs.bundles.core)
    compileOnly(libs.cloud.core)
    testImplementation(libs.bundles.minecraft)
    testImplementation(kotlin("test-junit5"))
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
}
