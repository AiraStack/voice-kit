buildscript {
    extra.apply {
        set("compose_version", "1.4.3")
        set("kotlin_version", "1.8.10")
    }
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${project.extra["kotlin_version"]}")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
} 