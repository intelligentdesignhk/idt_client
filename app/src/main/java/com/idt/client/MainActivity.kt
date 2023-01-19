package com.idt.client

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "Client"
    }

    lateinit var message: TextView
    lateinit var bindRes: TextView

    var requestMessenger: Messenger? = null
    var receiveMessenger: Messenger? = null
    var bound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bindBtn = findViewById<Button>(R.id.bind)
        bindBtn.setOnClickListener {
            // bind service
            bind()
        }

        val sendBtn = findViewById<Button>(R.id.send)
        sendBtn.setOnClickListener {
            if(!bound) {
                Log.e(TAG, "Service not bound: ")
                Toast.makeText(this, "Service not bound", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d(TAG, "Sending message to service")
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("key", "123")
            intent.putExtra("key2", "1234")
            val bundle:Bundle = intent.extras!!
            requestMessenger?.send(
                    Message.obtain().apply {
                        data = bundle
                        replyTo = receiveMessenger
                    }
            )
        }

        message = findViewById(R.id.message)
        bindRes = findViewById(R.id.bind_res)
    }

    inner class ReceiveHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            val received = msg.arg1
            Log.d(TAG, "incoming message: $received")
            message.text = received.toString()
        }
    }

    fun bind() {
        val intent = Intent().apply {
            component = ComponentName(
//                "com.idt.simpleservice",
//                "com.idt.simpleservice.SimpleService"
                "com.idt.test_service",
                "com.idt.test_service.SimpleService",
            )
        }
        Log.e(TAG, "bind: $intent")

        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(p0: ComponentName?, binder: IBinder) {
                requestMessenger = Messenger(binder)
                receiveMessenger = Messenger(ReceiveHandler(Looper.getMainLooper()))
                bound = true
                Log.d(TAG, "onServiceConnected: ")
            }

            override fun onServiceDisconnected(p0: ComponentName?) {
                requestMessenger = null
                receiveMessenger = null
                bound = false
                Log.d(TAG, "onServiceDisconnected: ")
            }
        }


        startForegroundService(intent)
        val res = bindService(
            intent,
            serviceConnection,
            BIND_AUTO_CREATE
        )

        Log.d(TAG, "bind(): result=$res")

        bindRes.text = if (res) "Service Bound Successfully" else "Service Not Bound"

    }

}