buildscript {
    dependencies {
        classpath("org.flywaydb:flyway-database-postgresql:13.3.0")
    }
}

plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.spring") version "2.3.21"
    id("com.diffplug.spotless") version "7.2.1"
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
    implementation("org.springframework.boot:spring-boot-starter-flyway:4.1.1")
    implementation("org.springframework.boot:spring-boot-starter-jooq:4.1.1")
    implementation("org.springframework.boot:spring-boot-starter-web:4.1.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.21.5")
    implementation("org.flywaydb:flyway-database-postgresql:12.4.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.3.21")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose:4.1.1")
    runtimeOnly("org.postgresql:postgresql:42.7.13")

    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test:4.1.1")
    testImplementation("org.springframework.boot:spring-boot-starter-jooq-test:4.1.1")
    testImplementation("org.springframework.boot:spring-boot-testcontainers:4.1.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.3.21")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter:2.0.5")
    testImplementation("org.testcontainers:testcontainers-postgresql:2.0.5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.0.3")

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
                inputSchema = "public"
                includes = ".*"
            }
            target {
                packageName = "com.ttsham6.bookmanager.jooq"
            }
        }
    }
}

sourceSets {
    main {
        java {
            srcDir("build/generated-sources/jooq")
        }
    }
}

tasks.named<org.jooq.codegen.gradle.CodegenTask>("jooqCodegen") {
    dependsOn("flywayMigrate")
}

tasks.named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileKotlin") {
    dependsOn("jooqCodegen")
}

spotless {
    kotlin {
        target("src/**/*.kt")
        ktlint("1.5.0")
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint("1.5.0")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
