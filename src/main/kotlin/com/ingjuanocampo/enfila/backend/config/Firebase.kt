package com.ingjuanocampo.enfila.backend.config

// Firebase configuration temporarily disabled for basic version
/*
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.Firestore
import com.google.firebase.firestore.FirestoreOptions
*/
import io.ktor.server.application.*
import java.io.FileInputStream

fun Application.configureFirebase() {
    // Firebase configuration temporarily disabled for basic version
    /*
    val credentialsPath = environment.config.propertyOrNull("firebase.credentials")?.getString()
    val projectId = environment.config.property("firebase.projectId").getString()
    
    if (credentialsPath != null) {
        val serviceAccount = FileInputStream(credentialsPath)
        val credentials = GoogleCredentials.fromStream(serviceAccount)
        
        val options = FirebaseOptions.builder()
            .setCredentials(credentials)
            .setProjectId(projectId)
            .build()
        
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
        }
    } else {
        log.warn("Firebase credentials not provided - migration from Firebase will be disabled")
    }
    */
    println("Firebase configuration disabled - basic version")
}
