plugins {
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
    id("java")
}

group = "com.silverbridge"
version = "0.0.1-SNAPSHOT"
val springAiVersion = "1.0.0-M1"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17)) // JDK 17 고정
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
    maven { url = uri("https://repo.spring.io/snapshot") }
}


dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:$springAiVersion")
    }
}

dependencies {
    // Spring Boot 기본
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Security
    implementation("org.springframework.boot:spring-boot-starter-security")

    // JPA + MariaDB
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client:3.4.0")

    // lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // dotenv
    implementation("io.github.cdimascio:dotenv-java:2.2.0")

    // jjwt
    implementation ("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly ("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly ("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // webflux
    implementation ("org.springframework.boot:spring-boot-starter-webflux")

    // coolSMS
    implementation ("net.nurigo:sdk:4.3.0")

    // [추가] Spring AI의 OpenAI 스타터
    implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter")

    // [추가] FCM 라이브러리
    implementation("com.google.firebase:firebase-admin:9.2.0")

    // 테스트
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
