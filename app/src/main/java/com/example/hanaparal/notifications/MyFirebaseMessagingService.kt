package com.example.hanaparal.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.hanaparal.MainActivity
import com.example.hanaparal.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle both data and notification payloads
        remoteMessage.notification?.let {
            showNotification(it.title ?: "HanapAral Update", it.body ?: "")
        }

        // If you send data payloads, you can handle them here
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "HanapAral Update"
            val message = remoteMessage.data["message"] ?: ""
            showNotification(title, message)
        }
    }

    override fun onNewToken(token: String) {
        Log.d("FCM", "Refreshed token: $token")
        // Ideally, you would save this token to Firestore under the user's profile
        // so you can send targeted notifications to this specific user.
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "study_group_notifications"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Study Group Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for group updates and reminders"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Using default launcher icon for now
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}
