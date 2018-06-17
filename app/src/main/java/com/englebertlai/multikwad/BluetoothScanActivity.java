package com.englebertlai.multikwad;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.companion.AssociationRequest;
import android.companion.BluetoothDeviceFilter;
import android.companion.CompanionDeviceManager;
import android.content.BroadcastReceiver;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import app.akexorcist.bluetotohspp.library.*;

public class BluetoothScanActivity extends AppCompatActivity {
    Context bluetooh_scan_activity_context;                                 // For handling context
    BluetoothSPP bt;                                                        // For Bluetooth use
    SharedPreferences sharedPreferences;                                    // For configuration settings
    SharedPreferences.Editor editPreferences;                               // For editing configurations settings
    String LOGID;                                                           // For Logging ID
    ArrayAdapter<String> itemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_scan);

        LOGID = this.getResources().getString(R.string.log_id);                  // For Logging ID
        bluetooh_scan_activity_context = this;

        setup();
    }

    private void loadDevices() {
        final ListView list_devices = (ListView) findViewById(R.id.list_devices);
        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < bt.getPairedDeviceName().length; ++i) {
            list.add(bt.getPairedDeviceName()[i] + "\n" + bt.getPairedDeviceAddress()[i]);
        }
        itemsAdapter =
                new ArrayAdapter<String>(
                        bluetooh_scan_activity_context,
                        android.R.layout.simple_list_item_1,
                        list);

        list_devices.setAdapter(itemsAdapter);

        list_devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = (String) list_devices.getItemAtPosition(i);

                Intent intent = new Intent();
                intent.putExtra("BLUETOOTH_DEVICE", item);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    private void doDiscovery() {
        Intent intentOpenBluetoothSettings = new Intent();
        intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(intentOpenBluetoothSettings);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
//            if(resultCode == Activity.RESULT_OK) bt.connect(data);
//        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
//            if(resultCode == Activity.RESULT_OK) {
//                bt.setupService();
//                bt.startService(BluetoothState.DEVICE_ANDROID);
//                setup();
//            } else {
//                // Do something if user doesn't choose any device (Pressed back)
//            }
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDevices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }

    private void setup() {
        bt = new BluetoothSPP(bluetooh_scan_activity_context);
        if(!bt.isBluetoothAvailable()) {
            // TODO: Any command for bluetooth is not available
        }

        if(!bt.isBluetoothEnabled()) {
            doDiscovery();
        }

        bt.startService(BluetoothState.DEVICE_OTHER);

        loadDevices();

        Button button_scan_bluetooth = (Button) findViewById(R.id.button_scan);
        button_scan_bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Clear view
                itemsAdapter.clear();

                // Get all the bluetooth devices list
                doDiscovery();
            }
        });
    }
}

