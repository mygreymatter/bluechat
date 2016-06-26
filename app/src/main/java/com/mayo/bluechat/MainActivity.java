package com.mayo.bluechat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.Set;

public class MainActivity extends AppCompatActivity implements Callback{

    private static final int REQUEST_ENABLE_BT = 111;
    private static final int FINE_LOCATION_PERMISSION = 112;
    private BluetoothAdapter mBluetoothAdapter;
    private DeviceAdapter adapter;
    private RecyclerView mRecyler;
    private Button mDiscoverBtn;
    private boolean isDiscovering;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_main);

        mDiscoverBtn = (Button) findViewById(R.id.start_stop_blue);
        adapter = new DeviceAdapter();

        mRecyler = (RecyclerView) findViewById(R.id.device_list);
        mRecyler.setLayoutManager(new LinearLayoutManager(this));
        mRecyler.setHasFixedSize(true);
        /*mRecyler.addItemDecoration(new SimpleDividerItemDecoration(
                getApplicationContext()
        ));*/
        mRecyler.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).build());
        mRecyler.setAdapter(adapter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions()) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION);
                return;
            } else {
                init();
            }
        } else {
            init();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mReceiver, getIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }

        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:

                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(MainActivity.this, "Bluetooth Enabled!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Bluetooth Not Enabled!", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case FINE_LOCATION_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Logger.print("Please give permission");
                        Toast.makeText(this, "Please give permission", Toast.LENGTH_SHORT).show();
                    } else {
                        Logger.print("Permission Denied!");
                    }
                } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Logger.print("Permission Granted!");
                    init();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    private void init() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public void toggleBluetooth(View v){
        discoverDevices();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Logger.print("Action: " + action);

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                Logger.print("Bluetooth Enabled: " + mBluetoothAdapter.isEnabled());
            } else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Logger.print("Already bonded " + device.getName());
                } else {
                    Logger.print(device.getName());
                }

                Blue.getInstance().devices.put(device.getAddress(),device.getName());

            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) && isDiscovering) {
                mDiscoverBtn.setText("Discover");
                isDiscovering = false;
//                Toast.makeText(MainActivity.this,"Stopped Discovering!",Toast.LENGTH_SHORT).show();
            }else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED) && !isDiscovering) {
                mDiscoverBtn.setText("Discovering");
                isDiscovering = true;
//                Toast.makeText(MainActivity.this,"Started Discovering!",Toast.LENGTH_SHORT).show();
            }
        }
    };

    private IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);

        return filter;
    }

    private void findPairedDevices() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                Logger.print("Paired Device: " + device.getName());
            }
        }
    }

    private void discoverDevices() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        if (mBluetoothAdapter.startDiscovery()) {
            Logger.print("Started discovery..!");
            Blue.getInstance().devices.clear();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                   mBluetoothAdapter.cancelDiscovery();
                    adapter.notifyDataChanged();
                }
            },12000);
        }
    }

    private boolean hasPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            return false;
        return true;
    }

    private void makeDiscoverable() {
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    @Override
    public void connect(String address) {
        Logger.print("Connect: " + address);
    }
}
