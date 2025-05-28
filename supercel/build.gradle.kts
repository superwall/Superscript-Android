import groovy.json.JsonBuilder

buildscript {
    extra["awsAccessKeyId"] = System.getenv("AWS_ACCESS_KEY_ID") ?: findProperty("aws_access_key_id")
    extra["awsSecretAccessKey"] = System.getenv("AWS_SECRET_ACCESS_KEY") ?: findProperty("aws_secret_access_key")
    extra["sonatypeUsername"] = System.getenv("SONATYPE_USERNAME") ?: findProperty("sonatype_username")
    extra["sonatypePassword"] = System.getenv("SONATYPE_PASSWORD") ?: findProperty("sonatype_password")
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("maven-publish")
    id("signing")
}

version = "0.2.6"
android {
    namespace = "com.superwall.supercel"
    compileSdk = 34

    defaultConfig {
        minSdk = 22

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86_64", "x86")
        }

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            consumerProguardFiles("proguard-rules.pro")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation("net.java.dev.jna:jna:5.17.0@aar")
    implementation(libs.coroutines)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.superwall.supercel"
                artifactId = "supercel"
                version = project.version.toString() // Set your library version

                pom {
                    name.set("SuperCEL")
                    description.set("Superwall CEL Evaluator")
                    url.set("https://superwall.com")

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://github.com/superwall/Superwall-Android?tab=MIT-1-ov-file#")
                        }
                    }
                    developers {
                        developer {
                            id.set("ianrumac")
                            name.set("Ian Rumac")
                            email.set("ian@superwall.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:git@github.com:superwall/SuperCEL-Android.git")
                        developerConnection.set("scm:git:ssh://github.com:superwall/SuperCEL-Android.git")
                        url.set("scm:git:https://github.com/superwall/SuperCEL-Android.git")
                    }
                }
            }
        }

        repositories {
            mavenLocal()

            val sonatypeUsername: String? by extra
            val sonatypePassword: String? by extra

            if (sonatypeUsername != null && sonatypePassword != null) {
                maven {
                    url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                    credentials(PasswordCredentials::class.java) {
                        username = sonatypeUsername
                        password = sonatypePassword
                    }
                }
            }
        }
    }


    signing {
        val signingKeyId: String? = System.getenv("SIGNING_KEY_ID")
        val signingPassword: String? = System.getenv("SIGNING_PASSWORD")
        val signingSecretKeyRingFile: String? = System.getenv("SIGNING_SECRET_KEY_RING_FILE")
        useInMemoryPgpKeys(signingKeyId, signingSecretKeyRingFile, signingPassword )
        sign(publishing.publications["release"])
    }
}

tasks.register("generateBuildInfo") {
    doLast {
        var buildInfo = mapOf("version" to version)
        val jsonOutput = JsonBuilder(buildInfo).toPrettyString()
        val outputFile = File("${getLayout().buildDirectory}/version.json")
        outputFile.writeText(jsonOutput)
    }
}

