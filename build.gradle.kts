plugins {
    java
    alias(libs.plugins.spotless)
    alias(libs.plugins.blossom)
    alias(libs.plugins.idea.ext)
    alias(libs.plugins.shadow)
}

/* Project Properties */
val projectGroup    = project.property("project_group")     as String
val projectVersion  = project.property("project_version")   as String

group = projectGroup
version = projectVersion

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

spotless {
    java {
        palantirJavaFormat()
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(libs.velocity.api)
    annotationProcessor(libs.velocity.api)

    implementation(libs.configurate.yaml)
    implementation(libs.commons.lang3)
}

sourceSets {
    main {
        blossom {
            javaSources {
                property("version", project.version.toString())
            }
        }
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    shadowJar {
        archiveClassifier.set("")

        relocate("org.spongepowered.configurate", "cc.maicra999.lunalike.libs.org.spongepowered.configurate")
        relocate("org.yaml.snakeyaml", "cc.maicra999.lunalike.libs.org.yaml.snakeyaml")
        relocate("io.leangen.geantyref", "cc.maicra999.lunalike.libs.io.leangen.geantyref")
    }

    build {
        dependsOn(spotlessApply, shadowJar)
    }
}
