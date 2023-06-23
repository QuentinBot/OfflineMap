package com.example.offlinemap

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Paint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.offlinemap.databinding.OfflineLayoutBinding
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import org.mapsforge.core.model.LatLong
import org.mapsforge.map.android.graphics.AndroidBitmap
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import org.mapsforge.map.android.util.AndroidUtil
import org.mapsforge.map.layer.overlay.Marker
import org.mapsforge.map.layer.renderer.TileRendererLayer
import org.mapsforge.map.reader.MapFile
import org.mapsforge.map.rendertheme.InternalRenderTheme
import java.io.FileInputStream

class OfflineMapActivity : AppCompatActivity(), LocationListener {
    private lateinit var b: OfflineLayoutBinding
    private lateinit var locationManager: LocationManager
    private lateinit var currentLocationMarker: Marker
    private lateinit var lastLocation: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AndroidGraphicFactory.createInstance(application)

        b = OfflineLayoutBinding.inflate(layoutInflater)
        setContentView(b.root)

        val contract = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){result->
            result?.data?.data?.let{uri->
                openMap(uri)
            }
        }
        b.loadMap.setOnClickListener {
            contract.launch(
                Intent(
                    Intent.ACTION_OPEN_DOCUMENT
                ).apply{
                    type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
            )

        }

    }

    private fun openMap(uri: Uri) {

        b.centerButton.setOnClickListener {
            moveMapToLocation(lastLocation)
        }

        b.map.mapScaleBar.isVisible= true
        b.map.setBuiltInZoomControls(true)
        val cache = AndroidUtil.createTileCache(
            this,
            "mycache",
            b.map.model.displayModel.tileSize,
            1f,
            b.map.model.frameBufferModel.overdrawFactor
        )

        val stream = contentResolver.openInputStream(uri) as FileInputStream

        val mapStore = MapFile(stream)

        val renderLayer = TileRendererLayer(
            cache,
            mapStore,
            b.map.model.mapViewPosition,
            AndroidGraphicFactory.INSTANCE
        )

        renderLayer.setXmlRenderTheme(
            InternalRenderTheme.DEFAULT
        )

        b.map.layerManager.layers.add(renderLayer)

        b.map.setCenter(LatLong(0.0, 0.0))
        b.map.setZoomLevel(10)

        currentLocationMarker = createMarker()
        b.map.layerManager.layers.add(currentLocationMarker)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        requestLocationUpdates()

        updateLocationMarker()
        moveMapToLocation(lastLocation)
    }

    override fun onLocationChanged(location: Location) {
        updateLocationMarker()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates()
            }
        }
    }

    private fun requestLocationUpdates() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), PERMISSION_REQUEST_CODE)
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this)
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationMarker() {
        val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        lastKnownLocation?.let {
            val latLong = LatLong(it.latitude, it.longitude)
            currentLocationMarker.latLong = latLong
            b.map.invalidate()
            lastLocation = it
        }
    }

    private fun createMarker(): Marker {
        val circleRadius = 15f

        val bitmap = android.graphics.Bitmap.createBitmap(
            (2 * circleRadius).toInt(),
            (2 * circleRadius).toInt(),
            android.graphics.Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)

        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = android.graphics.Color.RED
        canvas.drawCircle(circleRadius, circleRadius, circleRadius, paint)

        return Marker(LatLong(0.0, 0.0), AndroidBitmap(bitmap), 0, 0)
    }

    private fun moveMapToLocation(location: Location) {
        val latLong = LatLong(location.latitude, location.longitude)
        b.map.setCenter(latLong)
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
        private const val MIN_TIME_BETWEEN_UPDATES = 1000L
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES = 10f
    }


}