package com.englebertlai.multikwad;

import android.app.Activity;
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

import app.akexorcist.bluetotohspp.library.*;

public class CommunicationChannelActivity extends AppCompatActivity {
    Context communication_channel_activity_context;                         // For handling context
    BluetoothSPP bt;                                                        // For Bluetooth use
    SharedPreferences sharedPreferences;                                    // For configuration settings
    SharedPreferences.Editor editPreferences;                               // For editing configurations settings
    String LOGID;                                                           // For Logging ID
    String communication_channel;                                           // Determine bluetooth or serial port
    String bluetooth_macaddr;                                               // Bluetooth MacAddress
    String bluetooth_id;                                                    // Bluetooth ID

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
                loadBluetoothScan();
            }
        });

        Button button_otg_serial_port = (Button) findViewById(R.id.button_otg_serialport);
        button_otg_serial_port.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadOTGSerialPort();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK){
                String result = data.getStringExtra("BLUETOOTH_DEVICE");
                Log.d(LOGID, "BLUETOOTH_DEVICE: " + result);

                bluetooth_macaddr = result.split("\n")[1];
                bluetooth_id = result.split("\n")[0];

                Log.d(LOGID, "CHOSEN BLUETOOTH_MAC: " + bluetooth_macaddr);
                Log.d(LOGID, "CHOSEN BLUETOOTH_ID: " + bluetooth_id);

                showBluetoothSelected();
            }

            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }
    private void loadBluetoothScan() {
        Intent intent = new Intent(getApplicationContext(), BluetoothScanActivity.class);
        startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
    }

    private void loadOTGSerialPort() {
        AlertDialog alertDialog = new AlertDialog.Builder(communication_channel_activity_context).create();

        TextView title = new TextView(
                communication_channel_activity_context);                // Set Custom Title

        // Title Properties
        title.setText("OTG Serial Port Selected");
        title.setPadding(10, 10, 10, 10);        // Set Position
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);
        title.setTextSize(20);
        alertDialog.setCustomTitle(title);

        TextView msg = new TextView(
                communication_channel_activity_context);                // Set Message
        msg.setText(                                                    // Message Properties
                "Selected OTG Serial Port for communicating with MultiWii FC. Unfortunately is not available for now. In progress developing.");
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

    private void showBluetoothSelected() {
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
                        editPreferences
                                .putString("communication_channel", "BLUETOOTH")
                                .putString("bluetooth_macaddr", bluetooth_macaddr)
                                .putString("bluetooth_id", bluetooth_id);
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
}