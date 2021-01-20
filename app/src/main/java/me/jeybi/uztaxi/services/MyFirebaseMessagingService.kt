package me.jeybi.uztaxi.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.ui.main.MainActivity
import me.jeybi.uztaxi.utils.Constants
import me.jeybi.uztaxi.utils.Constants.Companion.APPLICATION_PREFERENCES
import me.jeybi.uztaxi.utils.Constants.Companion.FIREBASE_TOKEN

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Log.d("UZTAXI_FIREBASE", "NEW TOKEN : " + s)
        getSharedPreferences("_", MODE_PRIVATE).edit().putString(FIREBASE_TOKEN, s).apply()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("UZTAXI_FIREBASE", "NEW MESSAGE : " + remoteMessage.data)


        var smallText = ""

        val intent = Intent(Constants.ORDER_STATUS_RECIEVER)
        if (remoteMessage.data["orderId"]!=null)
        intent.putExtra(Constants.ORDER_ID,remoteMessage.data["orderId"]?.toLong())

        when (remoteMessage.data["kind"]) {
            Constants.ORDER_STATUS_CREATED -> {
                smallText = "Заказ создан!"
                intent.putExtra(Constants.ORDER_STATUS,Constants.ORDER_STATUS_CREATED )
            }
            Constants.ORDER_STATUS_CHANGED -> {
                smallText = "Статус заказа изменен!"
                intent.putExtra(Constants.ORDER_STATUS,Constants.ORDER_STATUS_CHANGED )
            }
            Constants.ORDER_STATUS_DRIVER_ASSIGNED -> {
                smallText = "Водитель уже идет к вам!"
                intent.putExtra(Constants.ORDER_STATUS,Constants.ORDER_STATUS_DRIVER_ASSIGNED )
            }
            Constants.ORDER_STATUS_DRIVER_DELAY -> {
                smallText = "Водитель может немного задержаться!"
                intent.putExtra(Constants.ORDER_STATUS,Constants.ORDER_STATUS_DRIVER_DELAY )
            }
            Constants.ORDER_STATUS_DRIVER_ARRIVED -> {
                smallText = "Водитель ждет вас!"
                intent.putExtra(Constants.ORDER_STATUS,Constants.ORDER_STATUS_DRIVER_ARRIVED )
            }
            Constants.ORDER_STATUS_EXECUTING -> {
                smallText = "Маршрут запущен!"
                intent.putExtra(Constants.ORDER_STATUS,Constants.ORDER_STATUS_EXECUTING )
            }
            Constants.ORDER_STATUS_DRIVER_UNASSIGNED -> {
                smallText = "Мы не назначили водителя!"
                intent.putExtra(Constants.ORDER_STATUS,Constants.ORDER_STATUS_DRIVER_UNASSIGNED )
            }
            Constants.ORDER_STATUS_ORDER_COMPLETED -> {
                smallText = "Маршрут завершен!"
                intent.putExtra(Constants.ORDER_STATUS,Constants.ORDER_STATUS_ORDER_COMPLETED )
            }
            Constants.ORDER_STATUS_BONUS_ADDED -> {
                smallText = "Бонусы добавлены на ваш счет!"
                intent.putExtra(Constants.ORDER_STATUS,Constants.ORDER_STATUS_BONUS_ADDED )
            }
            Constants.ORDER_STATUS_BONUS_WITHDRAWN -> {
                smallText = "Сняли бонусы со своего аккаунта!"
                intent.putExtra(Constants.ORDER_STATUS,Constants.ORDER_STATUS_BONUS_WITHDRAWN )
            }
            Constants.ORDER_STATUS_CANCELLED -> {
                smallText = "Заказ отменен!"
                intent.putExtra(Constants.ORDER_STATUS,Constants.ORDER_STATUS_CANCELLED )
            }
            Constants.ORDER_STATUS_PAID_WAITING_BEGAN -> {
                smallText = "Время ожидания начало оплачиваться!"
                intent.putExtra(Constants.ORDER_STATUS,Constants.ORDER_STATUS_PAID_WAITING_BEGAN )
            }
            Constants.ORDER_STATUS_CHAT_REQUEST -> {
                smallText = "Запрос в чат"
                intent.putExtra(Constants.ORDER_STATUS,Constants.ORDER_STATUS_CHAT_REQUEST )
            }

        }

        sendBroadcast(intent)
        sendNotification(smallText, "${remoteMessage.data.get("text")}")


    }


    private fun sendNotification(smallText: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = "uz_taxi_notification"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo)

            .setContentTitle("UZ TAXI™")
            .setContentText(smallText)
            .setAutoCancel(true)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(messageBody)
            )
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

}