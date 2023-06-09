package com.example.kor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    UUID SERVICE_UUID = UUID.fromString("00001623-1212-EFDE-1623-785FEABCD123");
    UUID CHARACTERISTIC_UUID = UUID.fromString("00001624-1212-EFDE-1623-785FEABCD123");

    UUID DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    BluetoothGatt mGatt = null;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothLeScanner mLeScanner;
    BluetoothGattService mBluetoothGattService;
    BluetoothGattCharacteristic mBluetoothGattCharacteristic;
    private boolean mScanning = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //scan
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        scanLeDevice(!mScanning);
        Button btn = findViewById(R.id.btn_onoff);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] a = new byte[]{0x04, 0x00, 0x02, 0x02};
                mBluetoothGattCharacteristic.setValue(a);
                Log.wtf("lala",String.valueOf(mGatt.writeCharacteristic(mBluetoothGattCharacteristic)));
                Log.v("lala",Arrays.toString(mBluetoothGattCharacteristic.getValue()));
              //  Log.v("lala",String.valueOf( mGatt.readCharacteristic(mBluetoothGattCharacteristic)));
               // Log.wtf("lala", Arrays.toString(mBluetoothGattCharacteristic.getValue()));

               // Log.v("lala",String.valueOf(mGatt.readCharacteristic(mBluetoothGattCharacteristic)));

            }
        });


        SeekBar bar = findViewById(R.id.bar);
        SeekBar bar2 = findViewById(R.id.bar2);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //Log.w("lala",String.valueOf(i));

                byte v = (byte)(-i < 0 ? (255 - i) : -i);
                byte[] a = new byte[]{8, 0x00, (byte)0x81, 0x01, 0x11, 0x51, 0x00, v};
                mBluetoothGattCharacteristic.setValue(a);
                mGatt.writeCharacteristic(mBluetoothGattCharacteristic);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(0);
                byte[] a = new byte[]{8, 0x00, (byte)0x81, 0x01, 0x11, 0x51, 0x00, 0x00};
                mBluetoothGattCharacteristic.setValue(a);
                mGatt.writeCharacteristic(mBluetoothGattCharacteristic);
            }
        });
        bar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int angle = normalizeAngle(i);
                byte[] a = new byte[]{0x0E, 0x00, (byte)0x81, 0x00, 0x11, 0x0D, (byte)(angle & 0xff), (byte)((angle >> 8) & 0xff), (byte)((angle >> 16) & 0xff), (byte)((angle >> 24) & 0xff), 0x32, 0x32, 0x7E, 0x00};
                mBluetoothGattCharacteristic.setValue(a);
                mGatt.writeCharacteristic(mBluetoothGattCharacteristic);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(0);
                byte[] a = new byte[]{0x0E, 0x00, (byte)0x81, 0x00, 0x11, 0x0D, 0x00, 0x00, 0x00, 0x00, 0x32, 0x32, 0x7E, 0x00};
                mBluetoothGattCharacteristic.setValue(a);
                mGatt.writeCharacteristic(mBluetoothGattCharacteristic);
            }
        });

    }
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();

            Log.wtf("lala", device.getName());
            mGatt = device.connectGatt(getApplicationContext(),false,bluetoothGattCallback);
        }
    };
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.w("lala","connected");
                gatt.discoverServices();
            } else {
               // mGatt.close();
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.i("lala","services discovered");
            mBluetoothGattService = mGatt.getService(SERVICE_UUID);
            mBluetoothGattCharacteristic = mBluetoothGattService.getCharacteristic(CHARACTERISTIC_UUID);
            mBluetoothGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            mGatt.setCharacteristicNotification(mBluetoothGattCharacteristic, true);
            BluetoothGattDescriptor gattDescriptor = mBluetoothGattCharacteristic.getDescriptors().get(0);
            gattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            gattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mGatt.writeDescriptor(gattDescriptor);
            //Log.wtf("lala", String.valueOf(mBluetoothGattCharacteristic.getValue()));
            //Log.i("lala",String.valueOf(mGatt.readCharacteristic(mBluetoothGattCharacteristic)));

        }

        @Override
        public void onCharacteristicRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value, int status) {
            super.onCharacteristicRead(gatt, characteristic, value, status);
            Log.wtf("lala","read");
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.wtf("lala","written");
        }

        @Override
        public void onCharacteristicChanged(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value) {
            super.onCharacteristicChanged(gatt, characteristic, value);
            Log.wtf("lala","changed");
        }
    };
    private void scanLeDevice(final boolean enable){
        if(enable){
            UUID[] serviceUUIDs = new UUID[]{SERVICE_UUID};
            List<ScanFilter> filters = null;
            if(serviceUUIDs != null) {
                filters = new ArrayList<>();
                for (UUID serviceUUID : serviceUUIDs) {
                    ScanFilter filter = new ScanFilter.Builder()
                            .setServiceUuid(new ParcelUuid(serviceUUID))
                            .build();
                    filters.add(filter);
                }
            }
            ScanSettings scanSettings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                    .setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH)
                    .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                    .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
                    .setReportDelay(0L)
                    .build();
            if (mLeScanner != null){
                mScanning = true;
                mLeScanner.startScan(filters,scanSettings,scanCallback);
                Log.d("lala", "scan started");
            } else {
                Log.e("lala", "could not get scanner object");
            }
        } else {
            mScanning = false;
            mLeScanner.stopScan(scanCallback);
        }
    }
    private int normalizeAngle(int angle){
        if (angle >= 180)
        {
            return angle - (360 * ((angle + 180) / 360));
        }
        else if (angle < -180)
        {
            return angle + (360 * ((180 - angle) / 360));
        }

        return angle;
    }
}