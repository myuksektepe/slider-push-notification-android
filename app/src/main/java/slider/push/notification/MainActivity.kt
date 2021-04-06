package slider.push.notification

import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import okhttp3.*
import slider.push.notification.models.SPNMainModel
import slider.push.notification.utils.SliderPushNotification
import slider.push.notification.utils.Util
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getJsonFromAPI("${getString(R.string.api_url)}${getString(R.string.json_ecommerce)}")
    }


    fun getJsonFromAPI(url: String) {

        // StrictMode
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(Util.TAG, e.message.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val result = response.body?.string()
                    //Log.i(Util.TAG, "Result: $result")

                    try {

                        // Get items from API
                        val mainModel = Gson().fromJson(result, SPNMainModel::class.java)

                        // Show notification
                        SliderPushNotification.show(applicationContext, mainModel)

                    } catch (e: Exception) {
                        Log.e(Util.TAG, e.message.toString())
                    }
                }
            }
        })
    }

}