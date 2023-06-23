package com.example.offlinemap

import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class OnlineMapActivity : AppCompatActivity() {
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.online_layout)
        Configuration.getInstance().load(applicationContext, getPreferences(MODE_PRIVATE))
        mapView = findViewById(R.id.map)
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        mapView.setMultiTouchControls(true)
        mapView.controller.zoomTo(12.0)
        mapView.controller.setCenter(GeoPoint(52.0, 9.0))

    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}