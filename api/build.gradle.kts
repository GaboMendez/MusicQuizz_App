import org.jetbrains.kotlin.cli.jvm.main

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.openapi.generator")
}

buildscript {
    dependencies {
        classpath("org.openapitools:openapi-generator-gradle-plugin:7.0.1")
    }
}

android {
    namespace = "com.usj.musicquizz.api"
    compileSdk = 34

    defaultConfig {
        minSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    sourceSets.getByName("main") {
        java.srcDir("client/src/main/java")
        java.srcDir("client/src/main/kotlin")
    }

    sourceSets.getByName("test") {
        java.srcDir("client/src/test/java")
        java.srcDir("client/src/test/kotlin")
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
    implementation("io.gsonfire:gson-fire:1.8.5")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.swagger:swagger-annotations:1.6.11")
    implementation("io.github.openfeign:feign-core:10.2.3")
    implementation("io.github.openfeign:feign-jackson:10.0.1")
    implementation("io.github.openfeign:feign-slf4j:10.0.1")
    implementation("io.github.openfeign.form:feign-form:2.1.0")
    implementation("io.github.openfeign:feign-gson:10.2.3")
    implementation("com.fasterxml.jackson.core:jackson-core:2.14.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.14.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")
    implementation("org.apache.oltu.oauth2:org.apache.oltu.oauth2.client:1.0.1") {
        exclude(group = "org.apache.oltu.oauth2", module = "org.apache.oltu.oauth2.common")
    }
    implementation("com.github.joschi.jackson:jackson-datatype-threetenbp:2.14.3")
    implementation("org.threeten:threetenbp:1.6.8")
    implementation("com.brsanthu:migbase64:2.2")
    implementation("org.openapitools:jackson-databind-nullable:0.2.2")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("androidx.annotation:annotation:1.7.0")
    implementation("androidx.core:core-ktx:1.12.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

val kotlin = Action<org.openapitools.generator.gradle.plugin.extensions.OpenApiGeneratorGenerateExtension> {
    inputSpec.set("${rootDir}/app/src/main/res/raw/api.yaml")
    outputDir.set("${project.projectDir}/client")
    apiPackage.set("${android.namespace}.api")
    invokerPackage.set("${android.namespace}.invoker")
    modelPackage.set("${android.namespace}.model")
    generatorName.set("java")
    configOptions.apply {
        mapOf(
            "hideGenerationTimestamp" to "true",
            "library" to "feign",
            "feignVersion" to "10.x",
            "dateLibrary" to "threetenbp",
            "packageName" to "${android.namespace}.api",
            "enumPropertyNaming" to "original",
            "collectionType" to "list",
            "useCoroutines" to true,
            "groupId" to "${project.group}",
            "artifactId" to "client"
        )
    }
}

openApiGenerate(kotlin)