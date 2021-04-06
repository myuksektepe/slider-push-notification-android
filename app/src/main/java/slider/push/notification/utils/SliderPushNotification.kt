package slider.push.notification.utils

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import slider.push.notification.R
import slider.push.notification.models.SPNItemModel
import slider.push.notification.models.SPNMainModel
import slider.push.notification.receiver.SPNReceiver
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

object SliderPushNotification {

    private const val notificationId = 1
    private var notificationUniqueId = 0
    var CHANNEL_ID: String? = null
    private var CHANNEL_NAME: String? = "Slider Push Notification"
    private var soundUri: Uri? = null
    private var audioAttributes: AudioAttributes? = null
    private var notificationChannel: NotificationChannel? = null

    @SuppressLint("StaticFieldLeak")
    var notificationManager: NotificationManager? = null
    private var notification: Notification? = null
    private var spnItemModel: List<SPNItemModel>? = null
    private const val Glide = false

    @RequiresApi(Build.VERSION_CODES.M)
    fun show(context: Context, spnMainModel: SPNMainModel) {

        // Notification Manager
        notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager?.cancelAll()
        spnItemModel = spnMainModel.items
        CHANNEL_ID = getRandomString(4)

        try {

            // RemoteView for Collapsed & Expanded View
            val collapsedView = RemoteViews(context.packageName, R.layout.notification_collapsed)
            val expandedViewFlipper = RemoteViews(context.packageName, R.layout.notification_expanded)

            // Notification
            notification = NotificationCompat.Builder(context, CHANNEL_ID!!)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setCustomContentView(collapsedView)
                .setCustomBigContentView(expandedViewFlipper) //.setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .build()


            // Collapsed Heading
            collapsedView.setTextViewText(R.id.text_view_collapsed_1, spnMainModel.heading)

            // Collapsed Call To Action
            collapsedView.setTextViewText(R.id.text_view_collapsed_2, spnMainModel.call_to_action)


            // Set Click Event to Next & Prev Buttons
            val buttonIntent = Intent(context, SPNReceiver::class.java)
            buttonIntent.putExtra("channel_id", CHANNEL_ID)
            buttonIntent.action = "prev"

            val prevPendingIntent = PendingIntent.getBroadcast(context, 0, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            expandedViewFlipper.setOnClickPendingIntent(R.id.btn_notification_expanded_prev, prevPendingIntent)

            buttonIntent.action = "next"
            val nextPendingIntent = PendingIntent.getBroadcast(context, 0, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            expandedViewFlipper.setOnClickPendingIntent(R.id.btn_notification_expanded_next, nextPendingIntent)

            // Loop For Adding Campings to Flipper
            for (i in spnItemModel!!.indices) {
                notificationUniqueId = System.currentTimeMillis().toInt()

                // Campaing Model
                val campaing: SPNItemModel = spnItemModel!![i]

                // Init
                val title: String = campaing.title
                val image: String = campaing.image
                val url: String = campaing.url
                Log.i(Util.TAG, "$i. Camping: $title - $image - $url")

                // Create Item for Campings
                val item = RemoteViews(context.packageName, R.layout.notification_expanded_item)

                // Set Click Event
                val clickIntent = Intent(context, SPNReceiver::class.java)
                clickIntent.putExtra("title", title)
                clickIntent.putExtra("url", url)
                val clickPendingIntent = PendingIntent.getBroadcast(context, notificationUniqueId, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                item.setOnClickPendingIntent(R.id.img_notification_expanded_image, clickPendingIntent)

                // Set Camping Title
                item.setTextViewText(R.id.txt_notification_expanded_title, title)

                // Set Camping Image
                item.setImageViewBitmap(R.id.img_notification_expanded_image, urlToBitmap(image))

                // Add Camping Item to Expanded Notification
                expandedViewFlipper.addView(R.id.flp_notification_expanded_flipper, item)
            }
            Log.d(Util.TAG, "--------------------------------------")

            // Custom Notification Sound
            soundUri = Uri.parse("android.resource://" + context.packageName + "/" + R.raw.custom_notification_sound)
            audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            
            // Create Notification Channel
            notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel!!.setSound(soundUri, audioAttributes)
            notificationChannel!!.importance = NotificationManager.IMPORTANCE_HIGH

            // Set Notification Channel to NotificationManager
            notificationManager!!.createNotificationChannel(notificationChannel!!)

            notificationManager!!.createNotificationChannelGroup(NotificationChannelGroup("1371", "SPN"))

            // Set Notification
            notificationManager!!.notify(notificationId, notification)


            // Delete Notification Olds Channel
            for (i in notificationManager!!.notificationChannels) {
                if (!i.id.equals(CHANNEL_ID)) {
                    notificationManager!!.deleteNotificationChannel(i.id);
                    Log.i(Util.TAG, "Deleted Notification Channel: ${i.id}")
                }
            }

        } catch (e: Exception) {
            Log.e(Util.TAG, e.message!!)
        }
    }

    private fun getRandomString(sizeOfRandomString: Int): String {
        val random = Random()
        val sb = StringBuilder(sizeOfRandomString)
        for (i in 0 until sizeOfRandomString) sb.append(Util.ALLOWED_CHARACTERS[random.nextInt(Util.ALLOWED_CHARACTERS.length)])
        return sb.toString()
    }

    private fun urlToBitmap(url: String): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val url1 = URL(url)
            val conn = url1.openConnection() as HttpURLConnection
            conn.doInput = true
            conn.connect()
            val stream = conn.inputStream
            bitmap = BitmapFactory.decodeStream(stream)

            //InputStream in = new java.net.URL(url).openStream();
            //bitmap = BitmapFactory.decodeStream(in);
        } catch (e: Exception) {
            Log.e(Util.TAG, e.message!!)
            e.printStackTrace()
        }
        return bitmap
    }
}
