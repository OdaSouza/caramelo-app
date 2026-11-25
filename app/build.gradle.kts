plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)

}

android {
    namespace = "com.draxio.mapa_teste"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.draxio.mapa_teste"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Assinatura do release por variáveis de ambiente (a keystore nunca fica no projeto).
    // Sem CARAMELO_KEYSTORE, o release é gerado sem assinatura.
    val keystorePath = providers.environmentVariable("CARAMELO_KEYSTORE").orNull
    signingConfigs {
        if (keystorePath != null) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = providers.environmentVariable("CARAMELO_KEYSTORE_PASSWORD").orNull
                keyAlias = providers.environmentVariable("CARAMELO_KEY_ALIAS").orNull
                keyPassword = providers.environmentVariable("CARAMELO_KEY_PASSWORD").orNull
            }
        }
    }

    buildTypes {
        release {
            // R8: reduz e ofusca o código; regras em src/main/keepRules
            optimization {
                enable = true
            }
            if (keystorePath != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.androidx.security.crypto)
    testImplementation(libs.junit)
    // O BOM tambem precisa valer para os testes instrumentados; sem ele o ui-test-junit4 fica sem versao
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}