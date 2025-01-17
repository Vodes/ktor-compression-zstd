import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "pw.vodes"
version = "0.0.1-SNAPSHOT"

kotlin {
    jvm()
    jvmToolchain(17)
    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.ktor.utils)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.encoding)
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.compression)
            }
        }

        val commonJvmMain by sourceSets.creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.zstd.jni)
            }
        }

        val jvmMain by getting {
            dependsOn(commonJvmMain)
            dependencies {
                implementation(libs.zstd.jni)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.ktor.client.java)
            }
        }

        val androidMain by getting {
            dependsOn(commonJvmMain)
            dependencies {
                implementation(libs.zstd.jni)
            }
        }
    }
}

android {
    namespace = "pw.vodes.ktor-compression-zstd"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    coordinates(group.toString(), "ktor-compression-zstd", version.toString())

    pom {
        name = "ktor-compression-zstd"
        description = "A library to add Zstd compression to your JVM/Android Ktor Client/Server projects."
        inceptionYear = "2024"
        url = "https://github.com/Vodes/ktor-compression-zstd"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "Vodes"
                name = "Alex H."
                url = "https://github.com/Vodes"
            }
        }
        scm {
            url = "https://github.com/Vodes/ktor-compression-zstd"
            connection = "scm:git:git://github.com/Vodes/ktor-compression-zstd.git"
            developerConnection = "scm:git:ssh://git@github.com/Vodes/ktor-compression-zstd.git"
        }
    }
}
