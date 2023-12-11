plugins {
    id("com.android.application")
    // Add the Google services Gradle plugin
    //
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.carrotchegg"
    compileSdk = 34
    viewBinding{
        enable = true
    }

    defaultConfig {
        applicationId = "com.example.carrotchegg"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.5.0"))

    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")

    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")

    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")

    // Declare the dependency for the Cloud Firestore library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-firestore")

    // FirebaseUI for Cloud Firestore
    implementation ("androidx.paging:paging-runtime:3.2.1")

    // Add the dependency for the Cloud Storage library
    //
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-storage")

    // FirebaseUI Storage only
    implementation ("com.firebaseui:firebase-ui-storage:8.0.2")

    // For @GlideModule
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // FirebaseUI for Cloud Firestore
    implementation ("com.firebaseui:firebase-ui-firestore:8.0.0")
    implementation ("androidx.paging:paging-runtime:3.2.1")

    //원형 이미지뷰 - 안쓰고는 못만들듯
    implementation ("de.hdodenhof:circleimageview:3.1.0")

    // Firebase messaging
    implementation ("com.google.firebase:firebase-messaging:23.3.1")

    // https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.11")
}