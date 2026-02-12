// build.gradle.kts (Project: JeffersonAntenasApp)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // Adicionamos estes dois para permitir Hilt e Room em todo o projeto
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    // NOVO PLUGIN: Adicionado para comunicação com os serviços do Google (Firebase)
    alias(libs.plugins.google.services) apply false
}