package com.deerbrain.basesdcard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var isSDCardReaderConnected: Boolean = false //This needs to detect when an SD card is connected and adjust value as needed. This would preferalbly be an enum rather than a boolean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        connectedButton.setOnClickListener {
            connectedButtonPressed()
        }

        fakeConnectedSDCard.setOnClickListener {
            fakeConnecedSDCardButtonPressed()
        }

        goToMap.setOnClickListener {
            val intent = Intent(this, CardReader::class.java)
            startActivity(intent)
        }


    }


    fun connectedButtonPressed() {
        if (isSDCardReaderConnected == true) {
            val intent = Intent(this, CardReader::class.java)
            startActivity(intent)
            //pass in the the base file directory
        } else {
            Toast.makeText(this,"No SD Card Reader Connected",Toast.LENGTH_SHORT).show()
        }
    }

    fun fakeConnecedSDCardButtonPressed() {
        if (isSDCardReaderConnected == true) {
            isSDCardReaderConnected = false
            connectedButton.text = "SD Card Not Connected"
        } else {
            isSDCardReaderConnected = true
            connectedButton.text = "SD Card Connected"
        }
    }






}