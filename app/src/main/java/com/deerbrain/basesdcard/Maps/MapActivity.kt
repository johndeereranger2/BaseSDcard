package com.deerbrain.basesdcard.Maps

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.deerbrain.basesdcard.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener{

    lateinit var mMap: GoogleMap

    var casheTileLayerShown: Boolean = false


    var mapCacheTileOverlay: TileOverlay? = null
    var parcelTileOverlay: TileOverlay? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

            //context = applicationContext

        setContentView(R.layout.map_activity)



        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(34.9496, 81.932)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        mMap.setOnMapClickListener(this)
    }


    override fun onMapClick(p0: LatLng?) {
        Log.i("Maps", "onMapClick")

        if (casheTileLayerShown) {
                mapCacheTileOverlay?.remove()
                casheTileLayerShown = false
            } else {
                casheTileLayerShown = false
                mapCacheTileOverlay = mMap.addTileOverlay(TileOverlayOptions().tileProvider(
                    MapCacheTileProvider()
                ))
        }
    }


}