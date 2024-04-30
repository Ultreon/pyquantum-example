import org.jetbrains.gradle.ext.Application
import org.jetbrains.gradle.ext.GradleTask
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings
import java.nio.file.Files
import java.nio.file.StandardOpenOption

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    dependencies {
        classpath("com.linkedin.pygradle:pygradle-plugin:+")
    }
}

plugins {
    idea
    `maven-publish`
    java
    `java-library`
    id("com.pswidersk.python-plugin") version "2.7.1"
    id("org.jetbrains.gradle.plugin.idea-ext") version "+"

    // Python plugin
}

group = "pyquantum"
version = "0.0.1"

pythonPlugin {
    pythonVersion = "3.10"
}

sourceSets {
    main {
        resources {
            srcDir("src/main/python")
        }
    }
}

sourceSets {
    main {

    }
}

repositories {
    mavenCentral()

    maven {
        name = "QuantumGitHub"
        url = uri("https://maven.pkg.github.com/Ultreon/quantum-voxel")
        credentials {
            username = (project.findProperty("gpr.user") ?: System.getenv("USERNAME")) as String
            password = (project.findProperty("gpr.key") ?: System.getenv("TOKEN")) as String
        }
    }

    maven {
        name = "Jitpack"
        url = uri("https://jitpack.io")

        content {
            includeGroup("space.earlygrey")
            includeGroup("com.github.mgsx-dev.gdx-gltf")
            includeGroup("com.github.Ultreon")
            includeGroup("com.github.jagrosh")
            includeGroup("com.github.JnCrMx")
        }
    }

    maven("https://maven.fabricmc.net/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("org.graalvm.polyglot:polyglot:23.1.2")
    implementation("org.graalvm.polyglot:python:23.1.2")

    implementation("io.github.ultreon.quantum:quantum-desktop:+")
}

val pyz = tasks.register<Zip>("pyz") {
    from("src/main/python")
    from(tasks.processResources.get().outputs)

    archiveFileName.set("${project.name}-${project.version}.pyz")

    destinationDirectory.set(file("${project.projectDir}/build/libs"))

    dependsOn(tasks.jar)

    doLast {
        tasks.jar.get().outputs.files.forEach { it.delete() }
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.build.configure {
    dependsOn("pyz")
}

tasks.register("prepareRun") {
    dependsOn("pyz")

    doLast {
        copy {
            from(pyz.get().outputs.files)
            into("$projectDir/run/server/mods/")
        }

        copy {
            from(pyz.get().outputs.files)
            into("$projectDir/run/client/main/mods/")
        }

        copy {
            from(pyz.get().outputs.files)
            into("$projectDir/run/client/alt/mods/")
        }
    }
}

fun setupIdea() {
    mkdir("$projectDir/build/gameutils")
    mkdir("$projectDir/run")
    mkdir("$projectDir/run/client")
    mkdir("$projectDir/run/client/alt")
    mkdir("$projectDir/run/client/main")
    mkdir("$projectDir/run/server")

    val ps = File.pathSeparator!!
    val files = configurations["runtimeClasspath"]!!

    val classPath = files.asSequence()
        .filter { it != null }
        .map { it.path }
        .joinToString(ps)

    //language=TEXT
    val conf = """
commonProperties
	fabric.development=true
	log4j2.formatMsgNoLookups=true
	fabric.log.disableAnsi=false
	log4j.configurationFile=$projectDir/log4j.xml
    """.trimIndent()
    val launchFile = file("$projectDir/build/gameutils/launch.cfg")
    Files.writeString(
        launchFile.toPath(),
        conf,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.WRITE
    )

    val cpFile = file("$projectDir/build/gameutils/classpath.txt")
    Files.writeString(
        cpFile.toPath(),
        classPath,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.WRITE
    )

    idea {
        project {
            settings {
                withIDEADir {
                    println("Callback 1 executed with: $absolutePath")
                }

                runConfigurations {
                    create(
                        "Quantum Client",
                        Application::class.java
                    ) {                       // Create new run configuration "MyApp" that will run class foo.App
                        jvmArgs =
                            "-Xmx4g -Dfabric.skipMcProvider=true -Dfabric.dli.config=${launchFile.path} -Dfabric.dli.env=CLIENT -Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotClient -Dfabric.zipfs.use_temp_file=false"
                        mainClass = "net.fabricmc.devlaunchinjector.Main"
                        moduleName = idea.module.name + ".main"
                        workingDirectory = "$projectDir/run/client/main/"
                        programParameters = "--gameDir=."

                        beforeRun {
                            create("Prepare Run", GradleTask::class.java) {
                                this.task = tasks.getByName("prepareRun")
                            }
                        }
                    }
                    create(
                        "Quantum Client Alt",
                        Application::class.java
                    ) {                       // Create new run configuration "MyApp" that will run class foo.App
                        jvmArgs =
                            "-Xmx4g -Dfabric.skipMcProvider=true -Dfabric.dli.config=${launchFile.path} -Dfabric.dli.env=CLIENT -Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotClient -Dfabric.zipfs.use_temp_file=false"
                        mainClass = "net.fabricmc.devlaunchinjector.Main"
                        moduleName = idea.module.name + ".main"
                        workingDirectory = "$projectDir/run/client/alt/"
                        programParameters = "--gameDir=."

                        beforeRun {
                            create("Prepare Run", GradleTask::class.java) {
                                this.task = tasks.getByName("prepareRun")
                            }
                        }
                    }
                    create(
                        "Quantum Server",
                        Application::class.java
                    ) {                       // Create new run configuration "MyApp" that will run class foo.App
                        jvmArgs =
                            "-Xmx4g -Dfabric.skipMcProvider=true -Dfabric.dli.config=${launchFile.path} -Dfabric.dli.env=SERVER -Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotClient -Dfabric.zipfs.use_temp_file=false"
                        mainClass = "net.fabricmc.devlaunchinjector.Main"
                        moduleName = idea.module.name + ".main"
                        workingDirectory = "$projectDir/run/server/"
                        programParameters = "--gameDir=."

                        beforeRun {
                            create("Prepare Run", GradleTask::class.java) {
                                this.task = tasks.getByName("prepareRun")
                            }
                        }
                    }
                }
            }
        }
    }
    rootProject.idea {
        module {
            isDownloadJavadoc = true
            isDownloadSources = true
        }
    }
}

this.setupIdea()

tasks.test {
    useJUnitPlatform()
}