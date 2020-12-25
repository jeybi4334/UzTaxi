package me.jeybi.uztaxi.services

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import me.jeybi.uztaxi.utils.Constants.Companion.APPLICATION_PREFERENCES
import me.jeybi.uztaxi.utils.Constants.Companion.FIREBASE_TOKEN

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Log.d("UZTAXI_FIREBASE", "NEW TOKEN : "+s)
        getSharedPreferences("_", MODE_PRIVATE).edit().putString(FIREBASE_TOKEN, s).apply()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("UZTAXI_FIREBASE", "NEW MESSAGE : "+remoteMessage)
    }

    companion object {
        fun getToken(context: Context): String? {
            return context.getSharedPreferences(APPLICATION_PREFERENCES, MODE_PRIVATE).getString(
                FIREBASE_TOKEN, ""
            )
        }
    }
}