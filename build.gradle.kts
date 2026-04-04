plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.15.0"
}

group = "com.jiuxiao"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    implementation("com.alibaba.fastjson2:fastjson2:2.0.32")
    implementation("org.apache.commons:commons-lang3:3.14.0")
}

intellij {
    localPath.set("D:\\Program Files\\JetBrains\\IntelliJ IDEA Community Edition 2022.3.3")
    plugins.set(listOf("java"))
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
        options.encoding = "UTF-8"
    }

    withType<JavaExec> {
        systemProperty("file.encoding", "UTF-8")
        systemProperty("sun.stdout.encoding", "UTF-8")
        systemProperty("sun.stderr.encoding", "UTF-8")
    }

    patchPluginXml {
        sinceBuild.set("221")
        untilBuild.set("999.*")
    }

    compileJava {
        options.encoding = "UTF-8"
    }

    compileTestJava {
        options.encoding = "UTF-8"
    }

    initializeIntelliJPlugin {
        selfUpdateCheck = false
        offline = true
    }
}