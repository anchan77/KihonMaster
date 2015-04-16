package com.raux.antoine.testapp;

import android.annotation.TargetApi;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.bluetooth.*;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;


public class MainActivity extends ActionBarActivity {

    protected BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mSensorTag;
    private boolean mScanning;
    private Handler mHandler;

    protected final static int REQUEST_ENABLE_BT = 1;
    public final static String EXTRA_DEVICE = "com.raux.antoine.testapp.DEVICE";
    public final static String SENSOR_TAG_SERVICE_UUID = "F000AA11-0451-4000-B000-000000000000";

    // Device scan callback.
    private ScanCallback bleScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    connectDevice(result.getDevice());
                }
            };

    protected void connectDevice(BluetoothDevice d) {
        Intent intent = new Intent(this, PunchActivity.class);
        intent.putExtra(EXTRA_DEVICE, d);
        startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        ScanFilter.Builder filterBuilder = new ScanFilter.Builder();
//        ScanFilter sensorTagFilter = filterBuilder.setServiceUuid(new ParcelUuid(UUID.fromString(SENSOR_TAG_SERVICE_UUID))).build();
        ScanFilter sensorTagFilter = filterBuilder.setDeviceName("SensorTag").build();
        List<ScanFilter> filters = new LinkedList<ScanFilter>();
        filters.add(sensorTagFilter);
        ScanSettings.Builder settingsBuilder = new ScanSettings.Builder();
        ScanSettings bleSettings = settingsBuilder.build();

        mBluetoothAdapter.getBluetoothLeScanner().startScan(filters, bleSettings, bleScanCallback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private void scanForSensorTag(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.cancelDiscovery();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startDiscovery();
        } else {
            mScanning = false;
            mBluetoothAdapter.cancelDiscovery();
        }

    }
}
