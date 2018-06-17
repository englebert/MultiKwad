package com.englebertlai.multikwad;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;

public class CommunicationChannelActivity extends AppCompatActivity {
    Context communication_channel_activity_context;                         // For handling context
    BluetoothSPP bt;                                                        // For Bluetooth use
    SharedPreferences sharedPreferences;                                    // For configuration settings
    SharedPreferences.Editor editPreferences;                               // For editing configurations settings
    String LOGID;                                                           // For Logging ID
    String communication_channel;                                           // Determine bluetooth or serial port

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication_channel);

        // For other section calling
        communication_channel_activity_context = this;

        setup();
    }

    private void setup() {
        sharedPreferences = this.getSharedPreferences(
                "com.englebertlai.multikwad", Context.MODE_PRIVATE);      // For Saving Configurations
        bt = new BluetoothSPP(communication_channel_activity_context);           // For Bluetooth
        LOGID = this.getResources().getString(R.string.log_id);                  // For Logging ID

        Button button_bluetooth = (Button) findViewById(R.id.button_bluetooth);
        button_bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog alertDialog = new AlertDialog.Builder(communication_channel_activity_context).create();

                TextView title = new TextView(
                        communication_channel_activity_context);                // Set Custom Title

                                                                                // Title Properties
                title.setText("Bluetooth Selected");
                title.setPadding(10, 10, 10, 10);        // Set Position
                title.setGravity(Gravity.CENTER);
                title.setTextColor(Color.BLACK);
                title.setTextSize(20);
                alertDialog.setCustomTitle(title);

                TextView msg = new TextView(
                        communication_channel_activity_context);                // Set Message
                msg.setText(                                                    // Message Properties
                        "Selected bluetooth device for communicating with MultiWii FC.");
                msg.setGravity(Gravity.CENTER_HORIZONTAL);
                msg.setTextColor(Color.BLACK);
                alertDialog.setView(msg);

                // Set Button
                // you can more buttons
                alertDialog.setButton(
                        AlertDialog.BUTTON_NEUTRAL,
                        "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO: Later remove the code and put to a function
                                // Pick a channel of the bluetooth scanned data
                                // REF:
                                // 1. https://stackoverflow.com/questions/3624280/how-to-use-sharedpreferences-in-android-to-store-fetch-and-edit-values
                                // 2. https://github.com/englebert/Android-BluetoothSPPLibrary
                                sharedPreferences = communication_channel_activity_context.getSharedPreferences(
                                        "com.englebertlai.multikwad", Context.MODE_PRIVATE);
                                editPreferences = sharedPreferences.edit();
                                editPreferences.putString(
                                        "communication_channel", "BLUETOOTH");
                                editPreferences.apply();                        // Save Settings

                                Intent intent = new Intent(communication_channel_activity_context, MainActivity.class);
                                // intent.putExtra("key", value); <---- For parsing parameters in future
                                CommunicationChannelActivity.this.startActivity(intent);
                                CommunicationChannelActivity.this.overridePendingTransition(0, 0);
                                finish();
                            }
                        });

                alertDialog.setButton(
                        AlertDialog.BUTTON_NEGATIVE,
                        "CANCEL",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Perform Action on Button
                            }
                        });

                new Dialog(communication_channel_activity_context);
                alertDialog.show();

                // Set Properties for OK Button
                final Button okBT = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                LinearLayout.LayoutParams neutralBtnLP =
                        (LinearLayout.LayoutParams) okBT.getLayoutParams();
                neutralBtnLP.gravity = Gravity.FILL_HORIZONTAL;
                okBT.setPadding(50, 10, 10, 10);   // Set Position
                okBT.setTextColor(Color.BLUE);
                okBT.setLayoutParams(neutralBtnLP);

                final Button cancelBT = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                LinearLayout.LayoutParams negBtnLP =
                        (LinearLayout.LayoutParams) okBT.getLayoutParams();
                negBtnLP.gravity = Gravity.FILL_HORIZONTAL;
                cancelBT.setTextColor(Color.RED);
                cancelBT.setLayoutParams(negBtnLP);
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

