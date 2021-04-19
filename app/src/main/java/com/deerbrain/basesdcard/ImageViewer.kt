package com.deerbrain.basesdcard

import android.media.Image
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class ImageViewer: AppCompatActivity() {
    //var shownImage: Image?
    var imageIndex = 0
//    var imageIndex = 3 {
//        didSet {
//            updateDisplayedImage()
//        }
//    }  //not sure how to do this in kotlin but in swift when the imageIndex is chanaged then the 'didset' gets called

    //this was probably made in the wrong manner
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_viewer)


//        nextButton.SetOnClickListener{
//            nextImageButtonPressed()
//        }
//
//        previousButton.SetOnClickListener{
//            previousImageButtonPressed()
//        }
    }

    fun updateDisplayedImage(){
        //shownImage.image = getImageFrom(imageIndex)
    }

    fun nextImageButtonPressed() {
        imageIndex = imageIndex + 1
    }

    fun previousImageButtonPressed(){
        imageIndex = imageIndex - 1
    }






}