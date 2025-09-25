import groovy.json.JsonBuilder
import com.vanniktech.maven.publish.AndroidSingleVariantLibrary


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.vanniktech.maven.publish)
}

version = "1.0.3"
android {
    namespace = "com.superwall.supercel"
    compileSdk = 34

    defaultConfig {
        minSdk = 21

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

}

dependencies {
    implementation("net.java.dev.jna:jna:5.17.0@aar")
    implementation(libs.coroutines)
}

mavenPublishing {
    coordinates("com.superwall.supercel", "supercel", project.version.toString())

    pom {
        name.set("SuperCEL")
        description.set("Superwall CEL Evaluator")
        inceptionYear.set("2024")
        url.set("https://superwall.com")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://github.com/superwall/Superwall-Android?tab=MIT-1-ov-file#")
                distribution.set("https://github.com/superwall/Superwall-Android?tab=MIT-1-ov-file#")
            }
        }
        developers {
            developer {
                id.set("ianrumac")
                name.set("Ian Rumac")
                email.set("ian@superwall.com")
                url.set("https://superwall.com")
            }
        }
        scm {
            url.set("https://github.com/superwall/SuperCEL-Android")
            connection.set("scm:git:git://github.com/superwall/SuperCEL-Android.git")
            developerConnection.set("scm:git:ssh://git@github.com/superwall/SuperCEL-Android.git")
        }
    }

    configure(AndroidSingleVariantLibrary())
    
    publishToMavenCentral()
    
    signAllPublications()
}

tasks.register("generateBuildInfo") {
    doLast {
        var buildInfo = mapOf("version" to version)
        val jsonOutput = JsonBuilder(buildInfo).toPrettyString()
        val outputFile = File("${getLayout().buildDirectory}/version.json")
        outputFile.writeText(jsonOutput)
    }
}

