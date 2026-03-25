package com.example.hanaparal.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Member 4: Handle incoming notifications here
        // Display a notification when a new member joins or there's an announcement
    }

    override fun onNewToken(token: String) {
        // Member 4: Send token to your server or save to Firestore
    }
}
