buildscript {
    dependencies {
        classpath("org.flywaydb:flyway-database-postgresql:13.3.0")
    }
}

plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.spring") version "2.3.21"
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.flywaydb.flyway") version "13.4.0"
    id("org.jooq.jooq-codegen-gradle") version "3.19.37"
}

group = "com.ttsham6"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    runtimeOnly("org.postgresql:postgresql:42.7.13")

    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
    testImplementation("org.springframework.boot:spring-boot-starter-jooq-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    jooqCodegen("org.postgresql:postgresql:42.7.13")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

flyway {
    url = "jdbc:postgresql://localhost:15432/bookdb"
    user = "app"
    password = "secret"
}

jooq {
    configuration {
        jdbc {
            driver = "org.postgresql.Driver"
            url = "jdbc:postgresql://localhost:15432/bookdb"
            user = "app"
            password = "secret"
        }
        generator {
            database {
                name = "org.jooq.meta.postgres.PostgresDatabase"
                inputSchema = "public"
                includes = ".*"
            }
            target {
                packageName = "com.ttsham6.bookmanager.jooq"
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

