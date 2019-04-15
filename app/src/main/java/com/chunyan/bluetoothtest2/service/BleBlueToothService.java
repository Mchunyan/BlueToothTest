package com.chunyan.bluetoothtest2.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.UUID;

/**
 * Created by machunyan on 2019/4/14.
 */
public class BleBlueToothService extends Service {

    private boolean isScanning;//是否正在搜索
    private static final long SCAN_PERIOD = 15000;//10秒的搜索时间
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattService gattService;
    private BluetoothGattCharacteristic gSCharacteristic;
    private BluetoothLeScanner bluetoothLeScanner;

    @Override
    public void onCreate() {
        super.onCreate();
        //获取蓝牙适配器
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

    }

    public class BleBlueToothBind extends Binder {

        public BluetoothAdapter getAdapter() {
            return bluetoothAdapter;
        }

        /**
         * 扫描
         */
        public void scanLeDevice(final BluetoothAdapter.LeScanCallback leScanCallback, final ScanCallback scanCallback) {
            //15秒后停止搜索
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isScanning = false;
                    stopScan(leScanCallback, scanCallback);
                }
            }, SCAN_PERIOD);
            isScanning = true;

            if (Build.VERSION.SDK_INT > 21) {
                bluetoothLeScanner.startScan(scanCallback);//开始搜索
            } else {
                bluetoothAdapter.startLeScan(leScanCallback); //开始搜索
            }
            Log.e("mcy", "开始扫描...");
        }

        /**
         * 停止扫描
         */
        public void stopScan(BluetoothAdapter.LeScanCallback mLeScanCallback, ScanCallback scanCallback) {
            if (!isScanning) {
                Log.e("mcy", "停止扫描...");
                if (mLeScanCallback != null) {
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                }
                if (scanCallback != null) {
                    bluetoothLeScanner.stopScan(scanCallback);
                }

            }
        }


        /**
         * 连接
         */

        public void connectLeDevice(Context context, BluetoothDevice device, BluetoothGattCallback mBluetoothGattCallback) {
            bluetoothGatt = device.connectGatt(context, false, mBluetoothGattCallback);

        }

        /**
         * 断开连接
         */
        public void cancleConnection() {
            if (bluetoothGatt != null) {
                bluetoothGatt.disconnect();
            }
        }

        /**
         * 向蓝牙发送数据
         */
        public void sendData() {

            gattService = bluetoothGatt.getService(UUID.fromString(""));
            gSCharacteristic = gattService.getCharacteristic(UUID.fromString(""));

//        byte[] send={(byte) 0xaa,0x01,0x01,(byte)0x81,(byte) 0xff};
            byte[] send = new byte[20];
//        send = hexStringToBytes(editText.getText().toString());
            byte[] sendData = new byte[send.length + 2];
            sendData[0] = (byte) 0xaa;
            sendData[sendData.length - 1] = (byte) 0xff;
            for (int i = 1; i < sendData.length - 1; i++) {
                sendData[i] = send[i - 1];
            }
//        Log.e("dataSend", bytesToHexString(sendData));
//        Log.e("dataSend", linkLossService + "");
            gSCharacteristic.setValue(sendData);
            boolean status = bluetoothGatt.writeCharacteristic(gSCharacteristic);
            Log.e("dataSend", status + "");
        }


        //读取数据时调用这个方法，数据会在回调接口中（BluetoothGattCallback ）获取到
        public void readData() {

        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return new BleBlueToothBind();
    }
}
