// app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Ativando os plugins de poder
    alias(libs.plugins.ksp)  // Processamento rápido de anotações
    alias(libs.plugins.hilt) // Injeção de Dependência
    // NOVO PLUGIN: Adicionado para o Firebase
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.jefferson.antenas"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.jefferson.antenas"
        minSdk = 26 // Recomendado para apps modernas (Android 8.0+)
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // Stripe key de TESTE para desenvolvimento
            buildConfigField("String", "STRIPE_PUBLIC_KEY", "\"pk_test_51SeGq12KTyysG2CrIXAKEOXFUhAksYfc637g5TyMLibBGNvxQtnIcQ74tJ18jvvJ6Oni6ev63PpzcnaonN74fgDs00sImMPrl0\"")
        }
        release {
            isMinifyEnabled = true
            // ⚠️ IMPORTANTE: Troque pela sua chave de PRODUÇÃO antes de fazer release
            buildConfigField("String", "STRIPE_PUBLIC_KEY", "\"pk_live_SUBSTITUA_PELA_SUA_CHAVE_PRODUCAO\"")
            // Usa as regras do ficheiro proguard-rules.pro
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true // ✅ Permite usar BuildConfig.STRIPE_PUBLIC_KEY
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Habilitar processamento de schemas do Room (opcional, bom para debug)
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Core & UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7") // <-- DEPENDÊNCIA ADICIONADA
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Networking (API)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor) // Para ver os logs da API no console

    // Imagens
    implementation(libs.coil.compose)

    // Hilt (Injeção de Dependência) - O Motor do App
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.compose.runtime.saveable)
    implementation(libs.androidx.compose.foundation)
    ksp(libs.hilt.compiler) // KSP é mais rápido que KAPT

    // Room (Banco de Dados Local)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx) // Extensões Kotlin (Coroutines)
    ksp(libs.androidx.room.compiler)

    // FIREBASE - NOSSO NOVO PORTEIRO E TESOUREIRO
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)

    // Testes
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // SDK do Stripe para Android
    implementation("com.stripe:stripe-android:22.5.0")

    // NOVA DEPENDÊNCIA: Efeito Shimmer
    implementation("com.valentinilk.shimmer:compose-shimmer:1.2.0")
    // Lottie para animações
    implementation("com.airbnb.android:lottie-compose:6.4.0")
}


// Adicionado para forçar o JDK 17 na compilação Kotlin
// Isso evita o erro "jlink" em algumas versões do AGP
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}