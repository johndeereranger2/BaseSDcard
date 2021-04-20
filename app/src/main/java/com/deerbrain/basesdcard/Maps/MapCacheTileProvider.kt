package com.deerbrain.basesdcard.Maps

import com.google.android.gms.maps.model.Tile

class MapCacheTileProvider {
    override fun getTile(x: Int, y: Int, z: Int): Tile {
        return MapCacheManager.getTile(x,y,z)
    }

}