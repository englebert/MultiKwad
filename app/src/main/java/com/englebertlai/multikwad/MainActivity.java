package com.englebertlai.multikwad;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

public class MainActivity extends AppCompatActivity implements SimpleGestureFilter.SimpleGestureListener {
    Context main_activity_context;                                          // For handling context
    BluetoothSPP bt;                                                        // For Bluetooth use
    SharedPreferences sharedPreferences;                                    // For configuration settings
    String LOGID;                                                           // For Logging ID
    String communication_channel;                                           // Determine bluetooth or serial port
    String bluetooth_macaddr;                                               // Bluetooth MacAddress
    private SimpleGestureFilter detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // For other section calling
        main_activity_context = this;

        // Detect touched area for different type of swipes
        detector = new SimpleGestureFilter(this,this);
        detector.setSwipeMinDistance(100);
        detector.setSwipeMaxDistance(3000);
        detector.setSwipeMinVelocity(1000);

        setup();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent me){
        // Call onTouchEvent of SimpleGestureFilter class
        this.detector.onTouchEvent(me);
        Log.d(LOGID, "Movement: " + me.toString());
        return super.dispatchTouchEvent(me);
    }

    @Override
    public void onDoubleTap() {
        Toast.makeText(this, "Double Tap", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSwipe(int direction) {
        String str = "";

        switch (direction) {
            case SimpleGestureFilter.SWIPE_RIGHT : str = "Swipe Right";
                // Toast.makeText(this, "Swipe Right", Toast.LENGTH_SHORT).show();
                break;
            case SimpleGestureFilter.SWIPE_LEFT :  str = "Swipe Left";
                // Toast.makeText(this, "Swipe Left", Toast.LENGTH_SHORT).show();
                loadFlightModeActivity();
                break;
            case SimpleGestureFilter.SWIPE_DOWN :  str = "Swipe Down";
                // Toast.makeText(this, "Swipe Down", Toast.LENGTH_SHORT).show();
                break;
            case SimpleGestureFilter.SWIPE_UP :    str = "Swipe Up";
                // Toast.makeText(this, "Swipe Up", Toast.LENGTH_SHORT).show();
                break;
        }
        // Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    private void setup() {
        sharedPreferences = this.getSharedPreferences(
                "com.englebertlai.multikwad", Context.MODE_PRIVATE);      // For Saving Configurations
        bt = new BluetoothSPP(this);                                     // For Bluetooth
        LOGID = this.getResources().getString(R.string.log_id);                  // For Logging ID

        // If there is no settings on bluetooth or serial then will show this dialogue box
        // Temporary only bluetooth for now
        communication_channel = sharedPreferences.getString("communication_channel", "");
        bluetooth_macaddr = sharedPreferences.getString("bluetooth_macaddr", "");

        if(communication_channel.equals("")) {
            Log.d(LOGID, "Loading communication channel activity");
            Intent intent = new Intent(main_activity_context, CommunicationChannelActivity.class);
            MainActivity.this.startActivity(intent);
            MainActivity.this.overridePendingTransition(0, 0);
            this.finish();
        }
        Log.d(LOGID, "Communication channel: " + communication_channel);
        Log.d(LOGID, "Bluetooth Mac Addr: " + bluetooth_macaddr);

        // Setting up Buttons
        Button button_exit = (Button) findViewById(R.id.button_exit);
        button_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

/*
        Button button_flight_mode = (Button) findViewById(R.id.button_flight_mode);
        button_flight_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(main_activity_context, FlightModeActivity.class);
                // intent.putExtra("key", value); <---- For parsing parameters in future
                MainActivity.this.startActivity(intent);
                MainActivity.this.overridePendingTransition(0, 0);
            }
        });
        */
    }

    private void loadFlightModeActivity() {
        Intent intent = new Intent(main_activity_context, FlightModeActivity.class);
        // intent.putExtra("key", value); <---- For parsing parameters in future
        MainActivity.this.startActivity(intent);
        // MainActivity.this.overridePendingTransition(0, 0);
    }
}
