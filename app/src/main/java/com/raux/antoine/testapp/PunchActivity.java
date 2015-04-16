package com.raux.antoine.testapp;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.UUID;


public class PunchActivity extends ActionBarActivity {

    private static final String TAG = "PunchActivity";

    public final static UUID ACCELEROMETER_SERVICE_UUID = UUID.fromString("F000AA10-0451-4000-B000-000000000000");
    public final static UUID ACCEL_DATA_UUID = UUID.fromString("F000AA11-0451-4000-B000-000000000000");
    public final static UUID ACCEL_CONFIG_UUID = UUID.fromString("F000AA12-0451-4000-B000-000000000000");
    public final static UUID ACCEL_PERIOD_UUID = UUID.fromString("F000AA13-0451-4000-B000-000000000000");

    protected BluetoothDevice mDevice;
    protected BluetoothGatt mGatt;
    protected BluetoothGattService mAccelService;

    protected TextView mDataView;

    // Various callback methods defined by the BLE API.
    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {

                    //Test
                    Log.i(TAG, "onConnectionStateChange: "+newState);

                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.i(TAG, "Connected to GATT server.");
                        mGatt = gatt;
                        mAccelService = mGatt.getService(ACCELEROMETER_SERVICE_UUID);
                        BluetoothGattCharacteristic characteristic = mAccelService.getCharacteristic(ACCEL_DATA_UUID);
                        mGatt.setCharacteristicNotification(characteristic, true);
                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(ACCEL_CONFIG_UUID);
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        mGatt.writeDescriptor(descriptor);

                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.i(TAG, "Disconnected from GATT server.");
                    }
                }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicChanged(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic c) {
                    final Integer x = c.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 0);
                    final Integer y = c.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 1);
                    final Integer z = c.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 2) * -1;
                    Log.i(TAG, "X="+x+", Y="+y+", Z="+z);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            mDataView.setText(x+" "+y+" "+z);
                        }
                    });
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_punch);

        mDataView = (TextView)findViewById(R.id.textbox);

        Log.i(TAG, "Connecting to Gatt");
        Intent intent = getIntent();
        mDevice = (BluetoothDevice)intent.getExtras().get(MainActivity.EXTRA_DEVICE);
        mGatt = mDevice.connectGatt(this, false, mGattCallback);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_punch, menu);
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
}
