package com.englebertlai.multikwad;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothService;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.*;

public class MainActivity extends AppCompatActivity implements SimpleGestureFilter.SimpleGestureListener {
    Context main_activity_context;                                          // For handling context
    BluetoothSPP bt;                                                        // For Bluetooth use
    SharedPreferences sharedPreferences;                                    // For configuration settings
    String LOGID;                                                           // For Logging ID
    String communication_channel;                                           // Determine bluetooth or serial port
    String bluetooth_macaddr;                                               // Bluetooth MacAddress
    String bluetooth_id;                                                    // Bluetooth ID
    private SimpleGestureFilter detector;                                   // For Swipe Gesture use
    private Boolean connectionStatus = false;                               // For connection status

    // For MultiWii Serial Protocol Use
    private static final int
            MSP_IDENT            = 100,
            MSP_STATUS           = 101,
            MSP_RAW_IMU          = 102,
            MSP_SERVO            = 103,
            MSP_MOTOR            = 104,
            MSP_RC               = 105,
            MSP_RAW_GPS          = 106,
            MSP_COMP_GPS         = 107,
            MSP_ATTITUDE         = 108,
            MSP_ALTITUDE         = 109,
            MSP_ANALOG           = 110,
            MSP_RC_TUNING        = 111,
            MSP_PID              = 112,
            MSP_BOX              = 113,
            MSP_MISC             = 114,
            MSP_MOTOR_PINS       = 115,
            MSP_BOXNAMES         = 116,
            MSP_PIDNAMES         = 117,
            MSP_SERVO_CONF       = 120,
            MSP_SET_RAW_RC       = 200,
            MSP_SET_RAW_GPS      = 201,
            MSP_SET_PID          = 202,
            MSP_SET_BOX          = 203,
            MSP_SET_RC_TUNING    = 204,
            MSP_ACC_CALIBRATION  = 205,
            MSP_MAG_CALIBRATION  = 206,
            MSP_SET_MISC         = 207,
            MSP_RESET_CONF       = 208,
            MSP_SELECT_SETTING   = 210,
            MSP_SET_HEAD         = 211, // Not used
            MSP_SET_SERVO_CONF   = 212,
            MSP_SET_MOTOR        = 214,
            MSP_BIND             = 240,
            MSP_EEPROM_WRITE     = 250,
            MSP_DEBUGMSG         = 253,
            MSP_DEBUG            = 254;

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
        bt = new BluetoothSPP(main_activity_context);                            // For Bluetooth
        LOGID = this.getResources().getString(R.string.log_id);                  // For Logging ID

        // If there is no settings on bluetooth or serial then will show this dialogue box
        // Temporary only bluetooth for now
        communication_channel = sharedPreferences.getString("communication_channel", "");
        bluetooth_macaddr = sharedPreferences.getString("bluetooth_macaddr", "");
        bluetooth_id = sharedPreferences.getString("bluetooth_id", "");

        if(communication_channel.equals("")) {
            selectCommunicationChannel();
            this.finish();
        }

        Log.d(LOGID, "Communication channel: " + communication_channel);
        Log.d(LOGID, "Bluetooth Mac Addr: " + bluetooth_macaddr);
        Log.d(LOGID, "Bluetooth ID: " + bluetooth_id);

        // Setting up Exit Button
        Button button_exit = (Button) findViewById(R.id.button_exit);
        button_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bt.stopService();
                finish();
            }
        });

        // Setting up Read Button
        Button button_read = (Button) findViewById(R.id.button_read_config);
        button_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readMultiWiiConfig();
            }
        });

        // If there is no connection had made, lets trigger it here
        if(!connectionStatus) {
            connectDevice();
        }
    }

    private void selectCommunicationChannel() {
        Log.d(LOGID, "Loading communication channel activity");
        Intent intent = new Intent(main_activity_context, CommunicationChannelActivity.class);
        MainActivity.this.startActivity(intent);
        MainActivity.this.overridePendingTransition(0, 0);
    }

    private void connectDevice() {
        AlertDialog alertDialog = new AlertDialog.Builder(main_activity_context).create();

        TextView title = new TextView(
                main_activity_context);                // Set Custom Title

        // Title Properties
        title.setText("Connect MultiWii FC Device");
        title.setPadding(10, 10, 10, 10);        // Set Position
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);
        title.setTextSize(20);
        alertDialog.setCustomTitle(title);

        TextView msg = new TextView(
                main_activity_context);                // Set Message
        msg.setText(                                                    // Message Properties
                "Connect to MultiWii FC via bluetooth?");
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
                        startConnectDevice();
                    }
                });

        alertDialog.setButton(
                AlertDialog.BUTTON_NEGATIVE,
                "CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Perform Action on Button
                        selectCommunicationChannel();
                        finish();
                    }
                });

        new Dialog(main_activity_context);
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

    private void startConnectDevice() {
        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            @Override
            public void onDataReceived(byte[] data, String message) {
                Log.d(LOGID, "RECEIVED: " + data + " ### " + message);
            }
        });

        // To determine etiher bluetooth or USB to connect
        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                // Do something when successfully connected
                Log.d(LOGID, "Bluetooth connected: [" + name + "][" + address + "]");
            }

            public void onDeviceDisconnected() {
                // Do something when connection was disconnected
                Log.d(LOGID, "Bluetooth disconnected.");
            }

            public void onDeviceConnectionFailed() {
                // Do something when connection failed
                Log.d(LOGID, "Bluetooth connection failed.");
            }
        });

        // Setting up bluetooth events
        bt.setBluetoothStateListener(new BluetoothSPP.BluetoothStateListener() {
            @Override
            public void onServiceStateChanged(int state) {
                switch (state) {
                    case BluetoothState.STATE_CONNECTED:
                        Toast.makeText(main_activity_context, "BT Connected", Toast.LENGTH_SHORT).show();
                        break;

                    case BluetoothState.STATE_CONNECTING:
                        Toast.makeText(main_activity_context, "BT Connecting", Toast.LENGTH_SHORT).show();
                        break;

                    case BluetoothState.STATE_LISTEN:
                        Toast.makeText(main_activity_context, "BT Listening", Toast.LENGTH_SHORT).show();
                        break;

                    case BluetoothState.STATE_NONE:
                        Toast.makeText(main_activity_context, "BT not Listening.", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        bt.setAutoConnectionListener(new BluetoothSPP.AutoConnectionListener() {
            public void onNewConnection(String name, String address) {
                // Do something when earching for new connection device
                Log.d(LOGID, "Bluetooth onNewConnection: [" + name + "][" + address + "]");
            }

            public void onAutoConnectionStarted() {
                // Do something when auto connection has started
                Log.d(LOGID, "Autoconnection started");
            }
        });

        bt.enable();
        bt.setupService();
        bt.startService(false);
        bt.connect(bluetooth_macaddr);


        // Logging section can be disabled. It is just for debugging purposes.
        Log.d(LOGID, "Bluetooth details: " + bluetooth_macaddr + " ### " + bluetooth_id );
        Log.d(LOGID, "Is Bluetooth adapter enable: " + bt.getBluetoothAdapter().isEnabled());
        Log.d(LOGID, "IS BLUETOOTH ENABLE: " + bt.isBluetoothEnabled());
        Log.d(LOGID, "IS BLUETOOTH AVAILABLE: " + bt.isBluetoothAvailable());
        Log.d(LOGID, "IS SERVICE AVAILABLE: " + bt.isServiceAvailable());

    }

    private void loadFlightModeActivity() {
        Intent intent = new Intent(main_activity_context, FlightModeActivity.class);
        // intent.putExtra("key", value); <---- For parsing parameters in future
        MainActivity.this.startActivity(intent);
        // MainActivity.this.overridePendingTransition(0, 0);
    }

    private void readMultiWiiConfig() {
        // bt.send(new byte[] { 0x30, 0x38, ....}, false);
        // bt.send("Message", true);
        // TODO:
        // Try to connect using the header and data
        // Testing with version

        // Send Message and Wait for replies
        requestWii();
    }

    /*
     * Request Message to MultiWii:
     * $M<[data length][code][data][checksum]
     * 1 octet '$'
     * 1 octet 'M'
     * 1 octet '<'
     * 1 octet [data length]
     * 1 octet [code]
     * several octet [data]
     * 1 octet [checksum]
     *
     * data length can be 0 in case no param command
     *
     * The general format of an MSP message is:
     * <preamble>,<direction>,<size>,<command>,,<crc>
     *     Where:
     *     preamble = the ASCII characters '$M'
     *     direction = the ASCII character '<' if to the MWC or '>' if from the MWC
     *     size = number of data bytes, binary. Can be zero as in the case of a data request to the MWC
     *     command = message_id as per the table below
     *     data = as per the table below. UINT16 values are LSB first.
     *     crc = XOR of <size>, <command> and each data byte into a zero'ed sum
     */

    private void requestWii() {
        int[] requests = {MSP_IDENT ,MSP_BOXNAMES, MSP_RC_TUNING, MSP_PID, MSP_MOTOR_PINS,MSP_BOX,MSP_MISC};
        tx2Wii(MSP_IDENT, null);
        tx2Wii(MSP_BOXNAMES, null);
        tx2Wii(MSP_RC_TUNING, null);
    }

    private void tx2Wii(int code, String data) {
        List<Byte> total_data = new LinkedList<Byte>();
        String MSP_HEADER = "$M<";

        for(byte c : MSP_HEADER.getBytes()) {
            total_data.add(c);
        }

        byte checksum = 0;
        byte dataLength = (byte)(((data == null) ? 0: data.length()) & 0xff);

        total_data.add(dataLength);
        checksum ^= (dataLength & 0xff);

        total_data.add((byte)(code & 0xff));
        checksum ^= (code & 0xff);

        if(data != null) {
            for(byte c : data.getBytes()) {
                total_data.add(c);
                checksum ^= (c & 0xff);
            }
        }

        total_data.add(checksum);
        Log.d(LOGID, "TX2WII: " + total_data.toString());
        Log.d(LOGID, "TX2WII checksum: " + checksum);

        // Convert to byte from Bytes
        byte[] command = new byte[total_data.size()];
        int i = 0;
        for(byte b : total_data) {
            command[i++] = b;
        }

        bt.send(command, false);
    }
}
