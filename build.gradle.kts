import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import com.github.gradle.node.pnpm.task.PnpmInstallTask
import com.github.gradle.node.pnpm.task.PnpmTask

plugins {
    id("java")
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.11.0"
    id("com.github.node-gradle.node") version "5.0.0"
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
    maven {
        url = uri("https://maven.pkg.github.com/eddie-energy/eddie")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GPR_USER")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GPR_TOKEN")
        }
    }
}

dependencies {
    implementation("energy.eddie:cim:3.1.0")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework:spring-websocket")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign:4.2.1")

    implementation("org.postgresql:postgresql:42.7.5")
    implementation("org.flywaydb:flyway-core:11.10.1")
    implementation("org.flywaydb:flyway-database-postgresql:11.10.1")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")

    implementation("org.eclipse.paho:org.eclipse.paho.mqttv5.client:1.2.5")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.22")

    implementation("org.glassfish.jaxb:jaxb-runtime")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
    implementation("com.fasterxml.jackson.module:jackson-module-jakarta-xmlbind-annotations")
    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("jakarta.annotation:jakarta.annotation-api")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:postgresql:1.21.0")
    testCompileOnly("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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
            "useSpringBoot3" to "true",
            "library" to "spring-cloud"
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

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootBuildImage>("bootBuildImage") {
    docker {
        host.set("npipe:////./pipe/docker_engine")
    }
}

tasks.named("processResources") {
    dependsOn(":pnpmBuild")
}

tasks.named("compileJava").configure {
    dependsOn(":generateDataNeedsClient")
    dependsOn(":pnpmBuild")
}
