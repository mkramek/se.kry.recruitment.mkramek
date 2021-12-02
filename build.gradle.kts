import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import com.github.gradle.node.npm.task.NpmTask

plugins {
  java
  application
  id("com.github.johnrengelman.shadow") version "7.0.0"
  id("com.github.node-gradle.node") version "3.1.1"
}

group = "se.kry.recruitment"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
}

val vertxVersion = "4.2.1"
val junitJupiterVersion = "5.8.1"
val jacksonVersion = "2.13.0"

val mainVerticleName = "se.kry.recruitment.mkramek.ApiVerticle"
val launcherClassName = "io.vertx.core.Launcher"

val watchForChange = "src/**/*"
val doOnChange = "${projectDir}/gradlew classes"

application {
  mainClass.set(launcherClassName)
}

dependencies {
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("commons-validator:commons-validator:1.7")
  implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
  implementation("io.vertx:vertx-auth-sql-client:$vertxVersion")
  implementation("io.vertx:vertx-auth-jwt:$vertxVersion")
  implementation("io.vertx:vertx-web:$vertxVersion")
  implementation("io.vertx:vertx-web-client:$vertxVersion")
  implementation("io.vertx:vertx-mysql-client:$vertxVersion")
  implementation("io.vertx:vertx-rx-java3:$vertxVersion")
  implementation("io.vertx:vertx-stomp:$vertxVersion")
  implementation("io.vertx:vertx-config:$vertxVersion")
  testImplementation("io.vertx:vertx-junit5:$vertxVersion")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  manifest {
    attributes(mapOf("Api-Verticle" to mainVerticleName))
  }
  mergeServiceFiles()
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

tasks.withType<JavaExec> {
  args = listOf("run", mainVerticleName, "--launcher-class=$launcherClassName"/*, "--redeploy=$watchForChange", "--on-redeploy=$doOnChange" */)
}

node {
  version.set("16.13.0")
  npmVersion.set("8.1.4")
  download.set(true)
  nodeProjectDir.set(File("src/main/webapp"))
}

val buildWebapp by tasks.creating(NpmTask::class) {
  args.set(listOf("run", "build-dev"))
  dependsOn("npmInstall")
}

val copyToWebroot by tasks.creating(Copy::class) {
  from("src/main/webapp/dist")
  destinationDir = File("${buildDir}/classes/java/main/webroot")
  dependsOn("buildWebapp")
}

val processResources by tasks.getting(ProcessResources::class) {
  dependsOn(copyToWebroot)
}
