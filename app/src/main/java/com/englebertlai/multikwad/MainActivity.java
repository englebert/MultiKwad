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

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothService;
import app.akexorcist.bluetotohspp.library.BluetoothState;

public class MainActivity extends AppCompatActivity implements SimpleGestureFilter.SimpleGestureListener {
    Context main_activity_context;                                          // For handling context
    BluetoothSPP bt;                                                        // For Bluetooth use
    SharedPreferences sharedPreferences;                                    // For configuration settings
    String LOGID;                                                           // For Logging ID
    String communication_channel;                                           // Determine bluetooth or serial port
    String bluetooth_macaddr;                                               // Bluetooth MacAddress
    String bluetooth_id;                                                  // Bluetooth ID
    private SimpleGestureFilter detector;                                   // For Swipe Gesture use
    private Boolean connectionStatus = false;                               // For connection status
    private BluetoothService bt_service = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothSPP.OnDataReceivedListener mDataReceivedListener = null;

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
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
                setup();
            } else {
                // Do something if user doesn't choose any device (Pressed back)
            }
        }
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

        // Setting up Buttons
        Button button_exit = (Button) findViewById(R.id.button_exit);
        button_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
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
        // To determine etiher bluetooth or USB to connect
        // TODO: HERE!!!
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
        bt.startService(BluetoothState.DEVICE_OTHER);
        bt.autoConnect(bluetooth_id);

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

//    @SuppressLint("HandlerLeak")
//    private final Handler mHandler = new Handler() {
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case BluetoothState.MESSAGE_WRITE:
//                    break;
//                case BluetoothState.MESSAGE_READ:
//                    byte[] readBuf = (byte[]) msg.obj;
//                    String readMessage = new String(readBuf);
//                    if(readBuf != null && readBuf.length > 0) {
//                        if(mDataReceivedListener != null)
//                            mDataReceivedListener.onDataReceived(readBuf, readMessage);
//                    }
//                    break;
//                case BluetoothState.MESSAGE_DEVICE_NAME:
////                    mDeviceName = msg.getData().getString(BluetoothState.DEVICE_NAME);
////                    mDeviceAddress = msg.getData().getString(BluetoothState.DEVICE_ADDRESS);
////                    if(mBluetoothConnectionListener != null)
////                        mBluetoothConnectionListener.onDeviceConnected(mDeviceName, mDeviceAddress);
////                    isConnected = true;
//                    break;
//                case BluetoothState.MESSAGE_TOAST:
//                    Toast.makeText(main_activity_context, msg.getData().getString(BluetoothState.TOAST), Toast.LENGTH_SHORT).show();
//                    break;
//
//                case BluetoothState.MESSAGE_STATE_CHANGE:
////                    if(mBluetoothStateListener != null)
////                        mBluetoothStateListener.onServiceStateChanged(msg.arg1);
////                    if(isConnected && msg.arg1 != BluetoothState.STATE_CONNECTED) {
////                        if(mBluetoothConnectionListener != null)
////                            mBluetoothConnectionListener.onDeviceDisconnected();
////                        if(isAutoConnectionEnabled) {
////                            isAutoConnectionEnabled = false;
////                            autoConnect(keyword);
////                        }
////                        isConnected = false;
////                        mDeviceName = null;
////                        mDeviceAddress = null;
////                    }
////
////                    if(!isConnecting && msg.arg1 == BluetoothState.STATE_CONNECTING) {
////                        isConnecting = true;
////                    } else if(isConnecting) {
////                        if(msg.arg1 != BluetoothState.STATE_CONNECTED) {
////                            if(mBluetoothConnectionListener != null)
////                                mBluetoothConnectionListener.onDeviceConnectionFailed();
////                        }
////                        isConnecting = false;
////                    }
//                    break;
//            }
//        }
//    };
}
