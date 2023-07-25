package java.com.backgroundlocationtracking

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.com.backgroundlocationtracking.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private  var _binding : ActivityMainBinding? = null
    private val binding: ActivityMainBinding
    get() = _binding!!

    private var service: Intent? = null



    private val backgroundLocation = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        if(it){

        }
    }

    private val locationPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
         when{
             it.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false) ->{

                 if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                     if(ActivityCompat.checkSelfPermission(
                             this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                         != PackageManager.PERMISSION_GRANTED
                     ){
                        backgroundLocation.launch(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                     }
                 }

             }
             it.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false)->{
             }
         }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        service = Intent(this, LocationService::class.java)

        _binding?.apply {
            startButton.setOnClickListener {
                checkPermission()
            }
            stopButton.setOnClickListener {
                stopService(service)
            }
        }

    }

    fun checkPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            if(ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ){
                locationPermission.launch(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            else{
                //perform back ground location permission
                startService(service)
            }

        }
    }

    override fun onStart() {
        super.onStart()
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        stopService(service)
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this)
        }
    }
    @Subscribe
    fun receiveLocationEvent(locationEvent : LocationEvent){
        binding.latitude.text = "Latitude : ${locationEvent.latitude}"
        binding.longitude.text = "Longitude : ${locationEvent.latitude}"
    }
}