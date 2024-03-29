package com.example.erikj.sensor_standoffapp;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;

public class  DeviceListActivity extends AppCompatActivity implements Serializable{
    private static final int REQUEST_ENABLE_BT = 1;

    private ListView lvDevices;
    private Button btnScan;
    private Button btnConnect;

    private ArrayList<BluetoothDevice> mBTDevices;
    private BluetoothDevice mBTDevice;
    private BluetoothAdapter mBluetoothAdapter;
    private DeviceListAdapter mDeviceListAdapter;

    private BluetoothConnectThread mConnectThread;
    private BluetoothAcceptThread mAcceptThread;

    private final static String TAG = "DeviceListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        mBTDevices = new ArrayList<>();
        lvDevices = (ListView)findViewById(R.id.lvDevices);
        btnScan = (Button)findViewById(R.id.btnScan);
        btnConnect = (Button) findViewById(R.id.btnConnect);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Log.d(TAG, "enableDiscoverable: Making the device discoverable for 120 seconds");

        Intent discoverableIntent = new Intent(mBluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(mBluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
        startActivity(discoverableIntent);

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanForDevices();
            }
        });

        lvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "You selected: " + mBTDevices.get(position)
                .getName(), Toast.LENGTH_SHORT).show();
                mBluetoothAdapter.cancelDiscovery();
                mBTDevice = mBTDevices.get(position);

                startAcceptThread();

/*               Log.d(TAG, "onItemClick: You Clicked on a device");
                String deviceName = mBTDevices.get(position).getName();
                String deviceAddress = mBTDevices.get(position).getAddress();

                Log.d(TAG, "onItemClick: deviceName = " +deviceName);
                Log.d(TAG, "onItemClick: deviceAddress = " +deviceAddress);

                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
                    Log.d(TAG, "Trying to pair with " +deviceName);
        //            mBTDevices.get(position).createBond();

                    mBTDevice = mBTDevices.get(position);

                } */
            }
        });

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connect();
            }
        });

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBondingReceiver, filter);
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mDiscoverReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
      /*  if(!mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } */
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        mBluetoothAdapter.cancelDiscovery();
        unregisterReceiver(mDiscoverReceiver);
        unregisterReceiver(mBondingReceiver);
        if(mAcceptThread != null){
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        if(mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        super.onDestroy();
    }

    private synchronized void startAcceptThread(){
        if(mAcceptThread == null){
            mAcceptThread = new BluetoothAcceptThread(this);
            mAcceptThread.start();
        }
    }

    private synchronized void connect(){
        if(mBTDevice != null) {
            if(mConnectThread != null){
                mConnectThread.cancel();
                mConnectThread = null;
            }
            if(mConnectThread == null){
                mConnectThread = new BluetoothConnectThread(this, mBTDevice);
                mConnectThread.start();
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "Choose a device from the list", Toast.LENGTH_SHORT).show();
        }
    }

    private void scanForDevices() {

        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "Canceling discovery");

            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            if(mBTDevices.size() > 0){
                mBTDevices.clear();
                mDeviceListAdapter.notifyDataSetChanged();
            }
            IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mDiscoverReceiver, intentFilter);
        }

        if(!mBluetoothAdapter.isDiscovering()){
            if(mBTDevices.size() > 0){
                mBTDevices.clear();
                mDeviceListAdapter.notifyDataSetChanged();
            }
            Log.d(TAG, "Start discovery");
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mDiscoverReceiver, intentFilter);
        }
    }

    private BroadcastReceiver mDiscoverReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);

                Log.d(TAG,"onReceive: " + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                lvDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };

    private final BroadcastReceiver mBondingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    mBTDevice = mDevice;
                }
                //case2: creating a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };

    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     *
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    private void checkBTPermissions(){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1){
            int permissionCheck = this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            permissionCheck += this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            if(permissionCheck != 0){
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }else{
                Log.d("CheckPermission-----", "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
            }
        }
    }

}
