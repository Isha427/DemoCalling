
package com.example.democalling
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.azure.android.communication.calling.Call
import com.azure.android.communication.calling.CallAgent
import com.azure.android.communication.calling.CallClient
import com.azure.android.communication.calling.CallState
import com.azure.android.communication.calling.IncomingCall
import com.azure.android.communication.calling.PropertyChangedEvent
import com.azure.android.communication.calling.PropertyChangedListener
import com.azure.android.communication.calling.StartCallOptions
import com.azure.android.communication.common.CommunicationIdentifier
import com.azure.android.communication.common.CommunicationTokenCredential
import com.azure.android.communication.common.CommunicationUserIdentifier
import java.util.concurrent.ExecutionException


class MainActivity : Activity() {

    private var callAgent: CallAgent? = null
    private var incomingCall: IncomingCall? = null
    private var call: Call? = null
    private var onStateChangedListener: PropertyChangedListener? = null
    private var alertDialog: AlertDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getAllPermissions()
        val acceptButton: Button = findViewById(R.id.accept_button)
        acceptButton.setOnClickListener {
            createIncomingAgent()
            handleIncomingCall()
        }
        val callButton: Button = findViewById(R.id.call_button)

        callButton.setOnClickListener {
            createOutgoingAgent()
            startCall()}

        val hangButton: Button = findViewById(R.id.hang_up)
        hangButton.setOnClickListener {
            hangUpCall()
            }

        volumeControlStream = AudioManager.STREAM_VOICE_CALL
    }

    /**
     * Request each required permission if the app doesn't already have it.
     */
    private fun getAllPermissions() {
        val requiredPermissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
        )
        val permissionsToAskFor = ArrayList<String>()
        for (permission in requiredPermissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToAskFor.add(permission)
            }
        }
        if (permissionsToAskFor.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToAskFor.toTypedArray(), 1)
        }
    }

    /**
     * Create the call agent for placing calls
     */
    private fun createOutgoingAgent() {
        val userToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjVFODQ4MjE0Qzc3MDczQUU1QzJCREU1Q0NENTQ0ODlEREYyQzRDODQiLCJ4NXQiOiJYb1NDRk1kd2M2NWNLOTVjelZSSW5kOHNUSVEiLCJ0eXAiOiJKV1QifQ.eyJza3lwZWlkIjoiYWNzOjBmNTA1ZjJkLWE5MmItNDBjMS1hNGJjLWNhZDJkYzc1NjM3OF8wMDAwMDAxOS1mOGZjLWVjMWMtODVmNC0zNDNhMGQwMDgwZDMiLCJzY3AiOjE3OTIsImNzaSI6IjE2ODk0MTcxMDciLCJleHAiOjE2ODk1MDM1MDcsInJnbiI6ImFtZXIiLCJhY3NTY29wZSI6InZvaXAiLCJyZXNvdXJjZUlkIjoiMGY1MDVmMmQtYTkyYi00MGMxLWE0YmMtY2FkMmRjNzU2Mzc4IiwicmVzb3VyY2VMb2NhdGlvbiI6InVuaXRlZHN0YXRlcyIsImlhdCI6MTY4OTQxNzEwN30.m2Slqh99e9Xm8b51r3DAZ9dc0EjD6w0at0QnksA3c0X571yEG0EgwWoVxS3zqGHeXLHWm0u7CrjiovXmv-vqEqJn9KIQpc3G4n6CMJkoiTD3UrTGcC-hX2V3VFgHU7er_BxDa5X6o5DOLPZzjw1UhRdpEeWEcuxGyzXAvCwDu0iXzyoTStsxlwvva6-TlQq1Qcqewmes9QtHpW0gFgaxxiWEG66An5HDflnjJgtoLPXgd8aPVRRk1TgzE8RTrgY3lwePo-NrP2NjqroZ_ykdGWK6u9mYz3U_JNtSw0RXhy2PukhNiTqRqUdTa6keuBPVMaV1QXklNnMzlurcuwkVmA"

        try {
            val credential = CommunicationTokenCredential(userToken)
            callAgent = CallClient().createCallAgent(applicationContext, credential).get()
        } catch (ex: Exception) {
            Toast.makeText(applicationContext, "Failed to create call agent.", Toast.LENGTH_SHORT).show()
        }
    }
    /**
     * Create the call agent for incoming calls
     */
    private fun createIncomingAgent() {
//      Callee Id - 8:acs:0f505f2d-a92b-40c1-a4bc-cad2dc756378_00000019-f8fb-183a-4ff7-343a0d0057c7
        val userToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjVFODQ4MjE0Qzc3MDczQUU1QzJCREU1Q0NENTQ0ODlEREYyQzRDODQiLCJ4NXQiOiJYb1NDRk1kd2M2NWNLOTVjelZSSW5kOHNUSVEiLCJ0eXAiOiJKV1QifQ.eyJza3lwZWlkIjoiYWNzOjBmNTA1ZjJkLWE5MmItNDBjMS1hNGJjLWNhZDJkYzc1NjM3OF8wMDAwMDAxOS1mOGZiLTE4M2EtNGZmNy0zNDNhMGQwMDU3YzciLCJzY3AiOjE3OTIsImNzaSI6IjE2ODk0MTY5ODciLCJleHAiOjE2ODk1MDMzODcsInJnbiI6ImFtZXIiLCJhY3NTY29wZSI6InZvaXAiLCJyZXNvdXJjZUlkIjoiMGY1MDVmMmQtYTkyYi00MGMxLWE0YmMtY2FkMmRjNzU2Mzc4IiwicmVzb3VyY2VMb2NhdGlvbiI6InVuaXRlZHN0YXRlcyIsImlhdCI6MTY4OTQxNjk4N30.WLnWncGk0OehykDXhNVD6277os6_i0naly4yE3b18WrnogZvbswAnGEw6iWErZBUTttg8vxrUcdXYotf4fz05wrcC1g1XyCNajK1YXNPMR_az9OTYJ36zCIOrso_9INYoKiFdM7L2LU4azk4DP4bMNWn78SVsoTZGTdB68u52YTSjR3xlhBF__85jPxdr220d_FA9i5SghVeW943IsagJqJfB84mITcCV8DIvNHNk5GD61euFlx6DH7VnQzl7h63AtAg41TujssdcV8KvcRGy8A99zLMrMjGtI3DcovCCTgZZh4LGCgI20za9cgSdC4gDTFiQEN6ZYmBF7kxtBL88w"
        try {
            val credential = CommunicationTokenCredential(userToken)
            callAgent = CallClient().createCallAgent(applicationContext, credential).get()
        } catch (ex: Exception) {
            Toast.makeText(applicationContext, "Failed to create call agent.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Place a call to the callee id provided in `callee_id` text input.
     */
    private fun startCall() {
       val calleeIdView = findViewById<EditText>(R.id.callee_id)
       val calleeId: String = calleeIdView.text.toString()
        val participants = ArrayList<CommunicationIdentifier>()
        val options = StartCallOptions()
        participants.add(CommunicationUserIdentifier(calleeId));
        call = callAgent?.startCall(
            applicationContext,
            arrayOf(CommunicationUserIdentifier(calleeId)),
            options
        )

        val onStateChangedListener = PropertyChangedListener { state ->
            handleCallOnStateChanged(state)
        }

        call?.addOnStateChangedListener(onStateChangedListener)
    }
    /**
     * Show AlertBox Function
     */
    private fun showAlertBox()
    {
        val builder = AlertDialog.Builder(this)


        builder.setMessage("Jane Doe")

        builder.setTitle("Incoming Call")

        builder.setCancelable(false)

        builder.setPositiveButton("Accept") {
                _, _ ->
                answerIncomingCall()

        }

        builder.setNegativeButton("Decline") { _, _ ->
            declineIncomingCall()
        }
        alertDialog = builder.create()
        if (alertDialog != null) {
            alertDialog!!.show()
        }
    }

    /**
     * Code for hangup Call
     */
    private fun hangUpCall() {
        try {
            call?.hangUp(null)?.get()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        val onStateChangedListener = PropertyChangedListener { state ->
            handleCallOnStateChanged(state)
        }
        call?.addOnStateChangedListener(onStateChangedListener)
    }
    /**
     * Code for ListenIncomingCall
     */
    private fun handleIncomingCall() {

        callAgent?.addOnIncomingCallListener { incomingCall ->
            val myTextView = findViewById<TextView>(R.id.myTextView)
            runOnUiThread {
                myTextView.text = "Call State is:"
            }
            this.incomingCall = incomingCall
            val onStateChangedListener = PropertyChangedListener { state ->
                handleCallOnStateChanged(state)
            }

            call?.addOnStateChangedListener(onStateChangedListener)
            runOnUiThread{
                showAlertBox()
            }
//            call?.addOnStateChangedListener { state ->
//                if (state.toString() == "DISCONNECTED") {
//                    runOnUiThread {
//                        dismissAlertBox()
//                    }
//                }
//            }
        }



    }
    private fun dismissAlertBox() {
        alertDialog?.dismiss()
    }


    /**
     * Code for Declining IncomingCall
     */
    private fun declineIncomingCall() {
        incomingCall?.reject()
    }

    /**
     * Code for Answer IncomingCall
     */
    private fun answerIncomingCall() {
        val context = applicationContext
        if (incomingCall == null) {
            return
        }

        try {
            call = incomingCall?.accept(context)?.get() as Call?
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }
        val onStateChangedListener = PropertyChangedListener { state ->
            handleCallOnStateChanged(state)
        }

        call?.addOnStateChangedListener(onStateChangedListener)


    }
    /**
     * Code for Print Call State
     */
    private fun handleCallOnStateChanged(args: PropertyChangedEvent) {
        val myTextView = findViewById<TextView>(R.id.myTextView)
        runOnUiThread {
            myTextView.text = "Call State is:" + call?.getState()

        }


    }
}


