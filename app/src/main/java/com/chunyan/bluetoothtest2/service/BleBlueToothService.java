package com.chunyan.bluetoothtest2.service;

import android.app.Service;
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
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.chunyan.bluetoothtest2.callback.BleResultCallBack;

import java.util.Arrays;
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
    private BluetoothGattCharacteristic gSCharacteristic;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothGattCallback mBluetoothGattCallback;
    private BleResultCallBack bleResultCallBack;
    private BluetoothGattService bluetoothGattService;
    private BluetoothGattCharacteristic bluetoothGattCharacteristic;

    private byte[][] data = new byte[][]{{2, 0, 19, 67, 79, 49, 50, 51, 52, 53, 54, 55, 56, 1, 73, -33, 77, -19, -61, -1},
            {41, -45, -26, 3}};

    @Override
    public void onCreate() {
        super.onCreate();
        //获取蓝牙适配器
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        mBluetoothGattCallback = new BluetoothGattCallback() {

            //当连接状态发生改变
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.e("mcy", "连接成功..." + gatt.getDevice().getName());
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.e("mcy", "连接断开...");
                } else if (newState == BluetoothProfile.STATE_DISCONNECTING) {
                    Log.e("mcy", "连接ing...");
                }
            }

            //发现新服务，即调用了mBluetoothGatt.discoverServices()后，返回的数据
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.e("mcy", "发现服务成功...");
                    bluetoothGattService = gatt.getService(UUID.fromString("49535343-fe7d-4ae5-8fa9-9fafd205e455"));
                    if (bluetoothGattService != null) {
                        bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString("49535343-8841-43F4-A8D4-ECBE34729BB3"));
                        if (bluetoothGattCharacteristic != null) {
                            bleResultCallBack.onDiscoverServicesSuccess();
                        }
                    } else {
                        bluetoothGattService = bluetoothGatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
                        if (bluetoothGattService != null) {
                            bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
                            if (bluetoothGattCharacteristic != null) {
                                bleResultCallBack.onDiscoverServicesSuccess();
                            }
                        } else {
                            Log.e("mcy", "获取Characteristic失败...");
                        }
                    }
                } else {
                    Log.e("mcy", "发现服务失败...");
                }

            }

            //读取从设备传递过来的数据值，在这里读数据
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                Log.e("mcy", "onCharacteristicRead...");
            }

            //发送数据后的回调
            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                Log.e("mcy", "onCharacteristicWrite...发送成功后走这个方法,并没有什么乱用");

            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                Log.e("mcy", "onCharacteristicChanged...");
                byte[] value = characteristic.getValue();
                if (value != null && value.length > 0) {
                    Log.e("mcy", "接收数据" + Arrays.toString(value));
                    final StringBuilder stringBuilder = new StringBuilder(data.length);
                    for (byte byteChar : value) {
                        stringBuilder.append(String.format("%02X ", byteChar));
                    }
                    Log.e("mcy", "接收数据" + stringBuilder.toString());
                }
            }

        };
    }


    public class BleBlueToothBind extends Binder {

        public BluetoothAdapter getAdapter() {
            return bluetoothAdapter;
        }

        public void setBleResultCallBack(BleResultCallBack bleResultCallBack) {
            BleBlueToothService.this.bleResultCallBack = bleResultCallBack;
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

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                if (bluetoothAdapter.isEnabled() && bluetoothLeScanner != null) {
                    bluetoothLeScanner.startScan(scanCallback);//开始搜索
                } else {
                    Log.e("mcy", "蓝牙不可用...");
                }
            } else {
                if (bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.startLeScan(leScanCallback); //开始搜索
                } else {
                    Log.e("mcy", "蓝牙不可用...");
                }
            }
            Log.e("mcy", "开始扫描...");
        }

        /**
         * 停止扫描
         */
        public void stopScan(BluetoothAdapter.LeScanCallback mLeScanCallback, ScanCallback scanCallback) {
            if (!isScanning) {
                Log.e("mcy", "停止扫描...");
                if (bluetoothAdapter != null && mLeScanCallback != null) {
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                }
                if (bluetoothLeScanner != null && scanCallback != null) {
                    bluetoothLeScanner.stopScan(scanCallback);
                }

            }
        }


        /**
         * 连接
         */

        public void connectLeDevice(Context context, BluetoothDevice device) {
            bluetoothGatt = device.connectGatt(context, false, mBluetoothGattCallback);

        }

        /**
         * 向蓝牙发送数据
         */
        public void sendDataToBT() {
            if (bluetoothGattCharacteristic != null && bluetoothGatt != null) {
                //设置读数据的UUID
                if (bluetoothGattService == null) {
                    bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString("49535343-1E4D-4BD9-BA61-23C647249616"));
                } else {
                    bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
                }
                bluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic, true);
                for (byte[] datum : data) {
                    bluetoothGattCharacteristic.setValue(datum);
                    boolean b = bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
                    Log.e("mcy", "发送数据是否成功:" + b);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        /**
         * 断开连接
         */
        public void cancleConnection() {
            if (bluetoothGatt != null) {
                bluetoothGatt.disconnect();
            }
        }


    }


    @Override
    public IBinder onBind(Intent intent) {
        return new BleBlueToothBind();
    }
}
