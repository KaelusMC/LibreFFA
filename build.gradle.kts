plugins {
    `java-library`
    `maven-publish`
    //id("com.gradleup.shadow") version "9.0.0-beta15"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "dev.darkxx"
version = "1.0.4"
description = "LibreFFA"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

repositories {
    mavenLocal()
    gradlePluginPortal()
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    
    maven {
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    
    maven {
        url = uri("https://repo.dmulloy2.net/nexus/repository/public/")
    }
    
    maven {
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
    
    maven {
        url = uri("https://maven.enginehub.org/repo/")
    }
}

dependencies {
    compileOnly("com.zaxxer:HikariCP:5.1.0")
    compileOnly("org.apache.httpcomponents:httpmime:4.5.6")
    paperweight.paperDevBundle("1.21.5-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.8")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core:2.7.0")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit:2.7.0")
}

//tasks.build {
//    dependsOn(tasks.shadowJar)
//}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
        artifact(tasks["jar"])
    }
}

//tasks.shadowJar {
//    minimize()
//    archiveFileName.set("${project.name}-${project.version}.jar")
//    relocate("com.zaxxer:HikariCP", "dev.darkxx.ffa.shaded.com.zaxxer:HikariCP")
//}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks {
    compileJava {
        options.release = 21
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
}