package com.englebertlai.multikwad;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

public class MainActivity extends AppCompatActivity {
    Context main_activity_context;                                          // For handling context
    BluetoothSPP bt;                                                        // For Bluetooth use
    SharedPreferences sharedPreferences;                                    // For configuration settings
    String LOGID;                                                           // For Logging ID
    String communication_channel;                                           // Determine bluetooth or serial port

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // For other section calling
        main_activity_context = this;

        setup();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    private void setup() {
        sharedPreferences = this.getSharedPreferences(
                "com.englebertlai.multikwad", Context.MODE_PRIVATE);      // For Saving Configurations
        bt = new BluetoothSPP(this);                                     // For Bluetooth
        LOGID = this.getResources().getString(R.string.log_id);                  // For Logging ID

        // If there is no settings on bluetooth or serial then will show this dialogue box
        // Temporary only bluetooth for now
        communication_channel = sharedPreferences.getString("communication_channel", "");
        if(communication_channel.equals("")) {
            Log.d(LOGID, "Loading communication channel activity");
            Intent intent = new Intent(main_activity_context, CommunicationChannelActivity.class);
            MainActivity.this.startActivity(intent);
            MainActivity.this.overridePendingTransition(0, 0);
            this.finish();
        }
        Log.d(LOGID, "Communication channel: " + communication_channel);

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
}
