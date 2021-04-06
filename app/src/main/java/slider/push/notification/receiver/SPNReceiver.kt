package slider.push.notification.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import slider.push.notification.R
import slider.push.notification.utils.Util

class SPNReceiver : BroadcastReceiver() {

    var CHANNEL_ID: String? = null
    var notificationId = 1

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != null) {
            CHANNEL_ID = intent.getStringExtra("channel_id")
            when (intent.action) {
                "prev" -> {
                    Log.d(Util.TAG, "--------------------------------------")
                    Log.d(Util.TAG, "Prev button clicked")
                    Log.d(Util.TAG, "--------------------------------------")
                    updateNotification(context, "prev")
                }
                "next" -> {
                    Log.d(Util.TAG, "--------------------------------------")
                    Log.d(Util.TAG, "Next button clicked")
                    updateNotification(context, "next")
                    Log.d(Util.TAG, "--------------------------------------")
                }
            }
        } else {
            //Log.i(Util.TAG, "Intent has not Action");
        }
        if (intent.hasExtra("url")) {
            val url = intent.getStringExtra("url")
            val title = intent.getStringExtra("title")
            Log.d(Util.TAG, "--------------------------------------")
            Log.d(Util.TAG, "Item Title: $title")
            Log.d(Util.TAG, "Item Url: $url")
            Log.d(Util.TAG, "--------------------------------------")
            if (url != null) {
                if (url.startsWith("http")) {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    browserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    context.startActivity(browserIntent)
                } else {
                    Log.e(Util.TAG, "Url is not start with 'http'")
                }
            } else {
                Log.e(Util.TAG, "Url is null")
            }

            // Cancel Notification
            val notificationManager = NotificationManagerCompat.from(context)
            val notificationChannel = notificationManager.getNotificationChannel(CHANNEL_ID!!)
            if (notificationChannel != null) {
                notificationChannel.importance = NotificationManager.IMPORTANCE_HIGH
                notificationManager.createNotificationChannel(notificationChannel)
                Log.d(Util.TAG, "Notification Channel set HIGH")
            }
            notificationManager.cancel(notificationId)

            // Close Notification Bar
            val closeIntent = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            context.sendBroadcast(closeIntent)
        } else {
            Log.i(Util.TAG, "Intent has not 'url'")
        }
    }

    fun updateNotification(context: Context, action: String) {

        // RemoteView
        val expandedViewFlipper = RemoteViews(context.packageName, R.layout.notification_expanded)
        if (action == "next") {
            expandedViewFlipper.showNext(R.id.flp_notification_expanded_flipper)
        } else {
            expandedViewFlipper.showPrevious(R.id.flp_notification_expanded_flipper)
        }


        // Notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID!!)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setCustomBigContentView(expandedViewFlipper)
            .build()
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val notificationChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
        if (notificationChannel != null) {
            notificationChannel.importance = NotificationManager.IMPORTANCE_LOW
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(notificationId, notification)
    }

}