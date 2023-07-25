package java.com.backgroundlocationtracking

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import org.greenrobot.eventbus.EventBus

class LocationService : Service() {

    companion object{
        private val CHENNAL_ID = "12345"
        private val NOTIFICATION_ID = 12345
    }

    var fusedLocationProviderClient : FusedLocationProviderClient?= null
    var locationCallback : LocationCallback? = null
    var locationRequest : LocationRequest?= null

    var notificationManager : NotificationManager? = null

    var location : Location? = null

    override fun onCreate() {
        super.onCreate()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setIntervalMillis(500)
            .build()
        locationCallback = object : LocationCallback(){
            override fun onLocationAvailability(p0: LocationAvailability) {
                super.onLocationAvailability(p0)
            }

            override fun onLocationResult(location: LocationResult) {
                super.onLocationResult(location)

                onNewLocation(location)
            }
        }

        notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(CHENNAL_ID, "Locations", NotificationManager.IMPORTANCE_HIGH)
            notificationManager!!.createNotificationChannel(notificationChannel)


        }

    }


    @SuppressLint("MissingPermission")
    fun requestLocationUpdates(){

        try {
            fusedLocationProviderClient?.requestLocationUpdates(
                locationRequest!!,
                locationCallback!!, null)
        }
        catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun removeLocationUpdates(){

        locationCallback?.let{
            fusedLocationProviderClient?.removeLocationUpdates(it)
        }

        stopForeground(true)
        stopSelf()
    }

    private fun onNewLocation(resultLocation: LocationResult) {

        location = resultLocation.lastLocation
        EventBus.getDefault().post(LocationEvent(
            latitude = location!!.latitude,
            longitude = location!!.longitude
        ))
        startForeground(NOTIFICATION_ID, getNotification())

    }

    fun getNotification() : Notification {
        val notification = NotificationCompat.Builder(this, CHENNAL_ID)
            .setContentTitle("Location Updates")
            .setContentText(
                "Latitude : ${location?.latitude} \n Longitude : ${location?.longitude}"
            )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setChannelId(CHENNAL_ID)
            .build()
        return notification

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        requestLocationUpdates()
        return START_STICKY

    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onDestroy() {
        super.onDestroy()

        removeLocationUpdates()
    }
}