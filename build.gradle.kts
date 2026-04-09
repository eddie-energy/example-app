import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import com.github.gradle.node.pnpm.task.PnpmInstallTask
import com.github.gradle.node.pnpm.task.PnpmTask

plugins {
    alias(libs.plugins.java)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.openapi.generator)
    alias(libs.plugins.node)
}

group = "energy.eddie"
version = "0.0.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.cim)

    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:${libs.versions.spring.cloud.get()}"))

    // --- Spring ---
    implementation(libs.bundles.spring.boot.starters)
    implementation(libs.spring.cloud.context)
    implementation(libs.spring.cloud.starter.openfeign)
    implementation(libs.spring.websocket)

    // --- Feign ---
    implementation(libs.feign.form)

    // --- Database ---
    implementation(libs.postgresql)
    implementation(libs.flyway.core)
    implementation(libs.flyway.postgresql)

    // --- OpenAPI ---
    implementation(libs.springdoc.openapi)
    implementation(libs.jackson.databind.nullable)
    implementation(libs.swagger.annotations)

    // --- Messaging ---
    implementation(libs.mqtt.client)

    // --- Jackson ---
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.jackson.datatype.jdk8)
    implementation(libs.jackson.dataformat.xml)
    implementation(libs.jackson.module.jakarta.xmlbind)

    // --- Jakarta / JAXB ---
    implementation(libs.jakarta.annotation)
    implementation(libs.jaxb.runtime)

    // --- Lombok ---
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // --- Testing ---
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.kafka.test)

    testCompileOnly(libs.lombok)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

var generateDataNeedsClientDir = "${project.layout.buildDirectory.asFile.get()}/generated/data-needs-api"

sourceSets {
    main {
        java {
            srcDir("$generateDataNeedsClientDir/src/main/java")
        }
    }
}


tasks.register<GenerateTask>("generateDataNeedsClient") {
    generatorName = "spring"
    inputSpec = "$projectDir/api-spec/eddie-data-needs-api.yml"
    outputDir = generateDataNeedsClientDir
    apiPackage = "energy.eddie.data-needs.generated.api"
    modelPackage = "energy.eddie.data-needs.generated.model"
    artifacts {
        id = "data-needs-client"
        groupId = "energy.eddie.data-needs.generated"
    }
    additionalProperties.set(
        mapOf(
            "useTags" to "true",
            "useJakartaEe" to "true",
            "library" to "spring-cloud",
            "useFeignClientContextId" to "true",
            "feignClientConfig" to "false"
        )
    )
}


node {
    version.set("22.19.0")
    pnpmVersion.set("10.15.0")
    download.set(true)

    nodeProjectDir.set(file("ui"))
}

tasks.withType<PnpmInstallTask> {
    args.set(listOf("--frozen-lockfile"))
}

tasks.register<PnpmTask>("pnpmBuild") {
    group = "build"
    description = "Builds the Example-App UI"

    dependsOn("pnpmInstall")

    args.set(listOf("run", "build"))
    environment.set(System.getenv())

    inputs.dir("ui")
    outputs.dir("src/main/resources/public")
    outputs.file("src/main/resources/templates/index.html")

    doLast {
        copy {
            from("ui/dist")
            into("src/main/resources/public")
        }
        copy {
            from("ui/dist/index.html")
            into("src/main/resources/templates")
        }
    }
}

tasks.register<Exec>("dockerBuild") {
    group = "build"
    description = "Builds docker image of the Example-App"
    commandLine("docker", "build", "-t", "example-app:latest", ".")
}

tasks.named("processResources") {
    dependsOn(":pnpmBuild")
}

tasks.named("compileJava").configure {
    dependsOn(":generateDataNeedsClient")
    dependsOn(":pnpmBuild")
}
