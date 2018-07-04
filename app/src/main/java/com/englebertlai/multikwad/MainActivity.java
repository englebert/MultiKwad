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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

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

    static int CHECKBOXITEMS=0;
    static int PIDITEMS=10;

    private static final int
            IDLE = 0,
            HEADER_START = 1,
            HEADER_M = 2,
            HEADER_ARROW = 3,
            HEADER_SIZE = 4,
            HEADER_CMD = 5,
            HEADER_ERR = 6;

    private int c_state = IDLE;
    boolean err_rcvd = false;
    byte checksum=0;
    byte cmd;
    int offset=0, dataSize=0;
    byte[] inBuf = new byte[256];
    int p;
    int read32() {return (inBuf[p++]&0xff) + ((inBuf[p++]&0xff)<<8) + ((inBuf[p++]&0xff)<<16) + ((inBuf[p++]&0xff)<<24); }
    int read16() {return (inBuf[p++]&0xff) + ((inBuf[p++])<<8); }
    int read8()  {return  inBuf[p++]&0xff;}

    int mode;

    // MultiWii Parameters
    int byteRC_RATE,byteRC_EXPO, byteRollPitchRate,byteYawRate, byteDynThrPID,byteThrottle_EXPO, byteThrottle_MID, byteSelectSetting,
            cycleTime, i2cError, version, versionMisMatch,horizonInstrSize, GPS_distanceToHome, GPS_directionToHome, GPS_numSat, GPS_fix,
            GPS_update, GPS_altitude, GPS_speed, GPS_latitude, GPS_longitude, init_com, graph_on, pMeterSum, intPowerTrigger, bytevbat;

    int msp_version      = 0;
    int multiCapability  = 0;           // Bitflags stating what capabilities are/are not present in the compiled code.
    int byteMP[]         = new int[8];  // Motor Pins.  Varies by multiType and Arduino model (pro Mini, Mega, etc).
    int MConf[]          = new int[10]; // Min/Maxthro etc
    int byteP[]          = new int[PIDITEMS], byteI[] = new int[PIDITEMS], byteD[] = new int[PIDITEMS];
    int activation[];
    int ServoMID[]       = new int[8];  // Plane,ppm/pwm conv,heli
    int servoRATE[]      = new int[8];
    int servoDirection[] = new int[8];
    int ServoMIN[]       = new int[8];
    int ServoMAX[]       = new int[8];
    int wingDir[]        = new int[8];  // Flying wing
    int wingPos[]        = new int[8];
    int In[]             = new int[8];


    boolean toggleRead = false,
            toggleReset = false,
            toggleCalibAcc = false,
            toggleCalibMag = false,
            toggleWrite = false,
            toggleRXbind = false,
            toggleSetSetting = false,
            toggleVbat=true,
            toggleMotor=false,
            motorcheck=true;

    int multiType;  // 1 for tricopter, 2 for quad+, 3 for quadX, ...

    // Alias for multiTypes
    private static final int 
            TRI           = 1,
            QUADP         = 2,
            QUADX         = 3,
            BI            = 4,
            GIMBAL        = 5,
            Y6            = 6,
            HEX6          = 7,
            FLYING_WING   = 8,
            Y4            = 9,
            HEX6X         = 10,
            OCTOX8        = 11,
            OCTOFLATX     = 12,
            OCTOFLATP     = 13,
            AIRPLANE      = 14,
            HELI_120_CCPM = 15,
            HELI_90_DEG   = 16,
            VTAIL4        = 17,
            HEX6H         = 18,
            PPM_TO_SERVO  = 19,
            DUALCOPTER    = 20,
            SINGLECOPTER  = 21;

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

    // All the labels in this UI
    TextView
            labelVersion = null,
            labelQuadType = null;
    
    EditText
            editRollPValue, editRollIValue, editRollDValue,
            editPitchPValue, editPitchIValue, editPitchDValue,
            editYawPValue, editYawIValue, editYawDValue,
            editAltPValue, editAltIValue, editAltDValue,
            editPosPValue, editPosIValue,
            editPosRPValue, editPosRIValue, editPosRDValue,
            editNavRPValue, editNavRIValue, editNavRDValue,
            editLevelPValue, editLevelIValue, editLevelDValue,
            editMagPValue,
            editThrottleMidValue, editThrottleExpoValue,
            editRCRateValue, editRCExpoValue,
            editRollPitchRateValue, editYawRateValue, editTPARateValue;
    
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

        // Setting up Labels and EditViews
        labelVersion = (TextView) findViewById(R.id.labelVersion);
        labelQuadType = (TextView) findViewById(R.id.labelQuadType);

        // Roll
        editRollPValue = (EditText) findViewById(R.id.editRollPValue);
        editRollIValue = (EditText) findViewById(R.id.editRollIValue);
        editRollDValue = (EditText) findViewById(R.id.editRollDValue);

        // Pitch
        editPitchPValue = (EditText) findViewById(R.id.editPitchPValue);
        editPitchIValue = (EditText) findViewById(R.id.editPitchIValue);
        editPitchDValue = (EditText) findViewById(R.id.editPitchDValue);
        
        // Yaw
        editYawPValue = (EditText) findViewById(R.id.editYawPValue);
        editYawIValue = (EditText) findViewById(R.id.editYawIValue);
        editYawDValue = (EditText) findViewById(R.id.editYawDValue);
        
        // Alt
        editAltPValue = (EditText) findViewById(R.id.editAltPValue);
        editAltIValue = (EditText) findViewById(R.id.editAltIValue);
        editAltDValue = (EditText) findViewById(R.id.editAltDValue);

        // Pos
        editPosPValue = (EditText) findViewById(R.id.editPosPValue);
        editPosIValue = (EditText) findViewById(R.id.editPosIValue);

        // PosR
        editPosRPValue = (EditText) findViewById(R.id.editPosRPValue);
        editPosRIValue = (EditText) findViewById(R.id.editPosRIValue);
        editPosRDValue = (EditText) findViewById(R.id.editPosRDValue);

        // NavR
        editNavRPValue = (EditText) findViewById(R.id.editNavRPValue);
        editNavRIValue = (EditText) findViewById(R.id.editNavRIValue);
        editNavRDValue = (EditText) findViewById(R.id.editNavRDValue);

        // Level
        editLevelPValue = (EditText) findViewById(R.id.editLevelPValue);
        editLevelIValue = (EditText) findViewById(R.id.editLevelIValue);
        editLevelDValue = (EditText) findViewById(R.id.editLevelDValue);

        // Mag
        editMagPValue = (EditText) findViewById(R.id.editMagPValue);

        // ThrottleMid & ThrottleExpo
        editThrottleMidValue = (EditText) findViewById(R.id.editThrottleMidValue);
        editThrottleExpoValue = (EditText) findViewById(R.id.editThrottleExpoValue);

        // RC Rate & RC Expo
        editRCRateValue = (EditText) findViewById(R.id.editRCRateValue);
        editRCExpoValue = (EditText) findViewById(R.id.editRCExpoValue);

        // RollPitch, Yaw and TPA
        editRollPitchRateValue = (EditText) findViewById(R.id.editRollPitchRateValue);
        editYawRateValue = (EditText) findViewById(R.id.editYawRateValue);
        editTPARateValue = (EditText) findViewById(R.id.editTPARateValue);

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

    // Receiving data...
    private void startConnectDevice() {
        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            @Override
            public void onDataReceived(byte[] data, String message) {
                for(byte c: data) {
                    if(c_state == IDLE) {
                        c_state = (c == '$') ? HEADER_START : IDLE;
                    } else if(c_state == HEADER_START) {
                        c_state = (c == 'M') ? HEADER_M : IDLE;
                    } else if (c_state == HEADER_M) {
                        if (c == '>') {
                            c_state = HEADER_ARROW;
                        } else if (c == '!') {
                            c_state = HEADER_ERR;
                        } else {
                            c_state = IDLE;
                        }
                    } else if (c_state == HEADER_ARROW || c_state == HEADER_ERR) {
                        // is this an error message?
                        err_rcvd = (c_state == HEADER_ERR);             // Expecting payload size
                        dataSize = (c & 0xff);

                        // Reset index variables
                        p = 0;
                        offset = 0;
                        checksum = 0;
                        checksum ^= (c & 0xff);

                        // The command is to follow
                        c_state = HEADER_SIZE;
                    } else if (c_state == HEADER_SIZE) {
                        cmd = (byte)(c & 0xff);
                        checksum ^= (c & 0xff);
                        c_state = HEADER_CMD;
                    } else if (c_state == HEADER_CMD && offset < dataSize) {
                        checksum ^= (c & 0xff);
                        inBuf[offset++] = (byte)(c & 0xff);
                    } else if (c_state == HEADER_CMD && offset >= dataSize) {
                        //  compare calculated and transferred checksum
                        if ((checksum & 0xff) == (c & 0xff)) {
                            if (err_rcvd) {
                                // MultiWii Unable to understand
                                Log.d(LOGID, "MultiWii unable to understand the request");
                            } else {
                                // Process the command
                                processCommand(cmd, (int)dataSize);
                            }

                        } else {
                            Log.d(LOGID,
                                    "Invalid checksum for command " +
                                            ((int)(cmd & 0xff)) + ": " +
                                            (checksum & 0xff) + " expected, got " +
                                            (int)(c & 0xff));
                            Log.d(LOGID,
                                    "<" + (cmd & 0xff) + " " + (dataSize&0xff) + "> {");

                            for(int i = 0; i < dataSize; i++) {
                                if(i != 0) {
                                    Log.d(LOGID, "ERROR");
                                }
                                Log.d(LOGID, "DEBUG: " + (inBuf[i] & 0xFF));
                            }
                            Log.d(LOGID, ("} ["+c+"]"));
                            Log.d(LOGID, (new String(inBuf, 0, dataSize)));
                        }

                        c_state = IDLE;
                    }
                }
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

    @Override
    public void onDestroy() {
        bt.disconnect();
        bt.stopAutoConnect();
        bt.stopService();

        super.onDestroy();
    }

    private void updateUI() {
        // Updating version
        labelVersion.setText("V: " + version);

        // Updating MW Type
        String mw_type = null;
        switch(multiType) {
            case TRI:
                mw_type = "TRI";
                break;

            case QUADP:
                mw_type = "QUADP";
                break;

            case QUADX:
                mw_type = "QUADX";
                break;

            case BI:
                mw_type = "BI";
                break;

            case GIMBAL:
                mw_type = "GIMBAL";
                break;

            case Y6:
                mw_type = "Y6";
                break;

            case HEX6:
                mw_type = "HEX6";
                break;

            case FLYING_WING:
                mw_type = "FLYING WING";
                break;

            case Y4:
                mw_type = "Y4";
                break;

            case HEX6X:
                mw_type = "HEX6";
                break;

            case OCTOX8:
                mw_type = "OCTOX8";
                break;

            case OCTOFLATX:
                mw_type = "OCTOFLATX";
                break;

            case OCTOFLATP:
                mw_type = "OCTOFLATP";
                break;

            case AIRPLANE:
                mw_type = "AIRPLANCE";
                break;

            case HELI_120_CCPM:
                mw_type = "HELI_120_CCPM";
                break;

            case HELI_90_DEG:
                mw_type = "HELI_90_DEG";
                break;

            case VTAIL4:
                mw_type = "VTAIL4";
                break;

            case HEX6H:
                mw_type = "HEX6H";
                break;

            case PPM_TO_SERVO:
                mw_type = "PPM TO SERVO";
                break;

            case DUALCOPTER:
                mw_type = "DUALCOPTER";
                break;

            case SINGLECOPTER:
                mw_type = "SINGLE COPTER";
                break;

            default:
                mw_type = "UNKNOWN";
                break;
        }

        labelQuadType.setText(mw_type);
    }

    private void loadFlightModeActivity() {
        Intent intent = new Intent(main_activity_context, FlightModeActivity.class);
        // intent.putExtra("key", value); <---- For parsing parameters in future
        MainActivity.this.startActivity(intent);
        // MainActivity.this.overridePendingTransition(0, 0);
    }

    private void readMultiWiiConfig() {
        // TODO:
        // Continue with different MSP Protocol and starts integrating to the UI
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
        tx2Wii(MSP_PID, null);
//        tx2Wii(MSP_RC_TUNING, null);
    }

    // Incoming commands from Bluetooth / Serial cable
    private void processCommand(byte cmd, int dataSize) {
        int icmd = (int)(cmd & 0xff);
        switch (icmd) {
            case MSP_IDENT:
                version = read8();
                multiType = read8();
                msp_version = read8(); // MSP version
                multiCapability = read32(); // capability
                /* Temporary remark. Later see how to convert it
                if ((multiCapability&1)>0) {buttonRXbind = controlP5.addButton("bRXbind",1,10,yGraph+205-10,55,10); buttonRXbind.setColorBackground(blue_);buttonRXbind.setLabel("RX Bind");}
                if ((multiCapability&4)>0) controlP5.addTab("Motors").show();
                if ((multiCapability&8)>0) flaps=true;
                if (!GraphicsInited)  create_ServoGraphics();
                */
                // Log.d(LOGID, "Version: " + version + ", MultiType: " + multiType + ", MultiCapability: " + multiCapability + ", MSP: " + msp_version);
                updateUI();
                break;

            case MSP_BOXNAMES:
                Log.d(LOGID, "MSP_BOXNAMES: " + new String(inBuf, 0, dataSize));
                updateUI();
                break;

            case MSP_MOTOR_PINS:
                for(int i = 0; i < 8; i++) {
                    byteMP[i] = read8();
                }
                break;

            case MSP_RC_TUNING:
                byteRC_RATE = read8();
                byteRC_EXPO = read8();
                byteRollPitchRate = read8();
                byteYawRate = read8();
                byteDynThrPID = read8();
                byteThrottle_MID = read8();
                byteThrottle_EXPO = read8();

                // RC Throttle Rate
                editThrottleMidValue.setText(String.valueOf(byteThrottle_MID/100.0));
                editThrottleExpoValue.setText(String.valueOf(byteThrottle_EXPO/100.0));

                // RC RATE
                editRCRateValue.setText(String.valueOf(byteRC_RATE/100.0));
                editRCExpoValue.setText(String.valueOf(byteRC_EXPO/100.0));

                // Roll Pitch Yaw Throttle Rate
                editRollPitchRateValue.setText(String.valueOf(byteRollPitchRate/100.0));
                editYawRateValue.setText(String.valueOf(byteYawRate/100.0));
                editTPARateValue.setText(String.valueOf(byteDynThrPID/100.0));

                break;

            case MSP_PID:
                for(int i = 0; i < PIDITEMS; i++) {
                    byteP[i] = read8();
                    byteI[i] = read8();
                    byteD[i] = read8();

                    switch (i){
                        // PID: Roll
                        case 0:
                            editRollPValue.setText(String.valueOf(byteP[i]/10.0));
                            editRollIValue.setText(String.valueOf(byteI[i]/1000.0));
                            editRollDValue.setText(String.valueOf(byteD[i]));
                            break;
                            
                        // PID: Pitch
                        case 1:
                            editPitchPValue.setText(String.valueOf(byteP[i]/10.0));
                            editPitchIValue.setText(String.valueOf(byteI[i]/1000.0));
                            editPitchDValue.setText(String.valueOf(byteD[i]));
                            break;
                         
                        // PID: Yaw
                        case 2:
                            editYawPValue.setText(String.valueOf(byteP[i]/10.0));
                            editYawIValue.setText(String.valueOf(byteI[i]/1000.0));
                            editYawDValue.setText(String.valueOf(byteD[i]));
                            break;

                        // PID: Alt
                        case 3:
                            editAltPValue.setText(String.valueOf(byteP[i]/10.0));
                            editAltIValue.setText(String.valueOf(byteI[i]/1000.0));
                            editAltDValue.setText(String.valueOf(byteD[i]));
                            break;

                        // PID: Level
                        case 7:
                            editLevelPValue.setText(String.valueOf(byteP[i]/10.0));
                            editLevelIValue.setText(String.valueOf(byteI[i]/1000.0));
                            editLevelDValue.setText(String.valueOf(byteD[i]));
                            break;
                            
                        case 8:
                            editMagPValue.setText(String.valueOf(byteP[i]/10.0));
                            break;

                        case 9:
                            Log.d(LOGID, "PID DEBUG: P[" + String.valueOf(byteP[i]/10.0) + "] I[" + String.valueOf(byteI[i]/1000.0) + "] D[" + String.valueOf(byteP[i]) + "]");
                            break;

                        // Different rates for POS-4 POSR-5 NAVR-6
                        // PID: Pos
                        case 4:
                            editPosPValue.setText(String.valueOf(byteP[i]/100.0));
                            editPosIValue.setText(String.valueOf(byteI[i]/100.0));
                            break;
                        
                        // PID: PosR
                        case 5:
                            editPosRPValue.setText(String.valueOf(byteP[i]/10.0));
                            editPosRIValue.setText(String.valueOf(byteI[i]/100.0));
                            editPosRDValue.setText(String.valueOf(byteD[i]/1000.0));
                            break;
                            
                        // PID: NavR
                        case 6:
                            editNavRPValue.setText(String.valueOf(byteP[i]/10.0));
                            editNavRIValue.setText(String.valueOf(byteI[i]/100.0));
                            editNavRDValue.setText(String.valueOf(byteD[i]/1000.0));
                            break;
                    }
                }
                updateUI();
                break;

            default:
                Log.d(LOGID, "I don't know how to handle this -> " + icmd);
        }
    }

    private void tx2Wii(int code, String data) {
        if(code == 0) return;

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
//        Log.d(LOGID, "TX2WII: " + total_data.toString());
//        Log.d(LOGID, "TX2WII checksum: " + checksum);

        // Convert to byte from Bytes
        byte[] command = new byte[total_data.size()];
        int i = 0;
        for(byte b : total_data) {
            command[i++] = b;
            // Log.d(LOGID, "OUT: " + b);
        }

        bt.send(command, false);
    }
}