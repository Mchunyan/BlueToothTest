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
    private static final long SCAN_PERIOD = 10000;//10秒的搜索时间
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothGattCallback mBluetoothGattCallback;
    private BleResultCallBack bleResultCallBack;
    private BluetoothGattService gattService;
    private BluetoothGattCharacteristic gattCharacteristic;

    private byte[][] data = new byte[][]{{2, 0, 19, 67, 79, 49, 50, 51, 52, 53, 54, 55, 56, 1, 73, -33, 77, -19, -61, -1},
            {41, -45, -26, 3}};

    private int indexTpye = 0;

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
                    gattService = gatt.getService(UUID.fromString("49535343-fe7d-4ae5-8fa9-9fafd205e455"));
                    indexTpye = 1;
                    if (gattService == null) {
                        indexTpye = 2;
                        gattService = gatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
                    }
                    if (gattService == null) {
                        Log.e("mcy", "获取bluetoothGattService失败...");
                    } else {
                        if (indexTpye == 1) {
                            gattCharacteristic = gattService.getCharacteristic(UUID.fromString("49535343-8841-43F4-A8D4-ECBE34729BB3"));
                        } else {
                            gattCharacteristic = gattService.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
                        }
                        if (gattCharacteristic == null) {
                            Log.e("mcy", "获取Characteristic失败...");
                        } else {
                            bleResultCallBack.onDiscoverServicesSuccess();
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
                Log.e("mcy", "---蓝牙回传数据onCharacteristicChanged---");
                byte[] value = characteristic.getValue();
                if (value != null && value.length > 0) {
                    Log.e("mcy", "接收数据" + Arrays.toString(value));
                    bleResultCallBack.onReturnResult(value);

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
//            //10秒后停止搜索
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    isScanning = false;
//                    Log.e("mcy", "10s钟到啦,停止扫描...");
//                    stopScan(leScanCallback, scanCallback);
//                }
//            }, SCAN_PERIOD);
//            isScanning = true;

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
//            if (!isScanning) {
            Log.e("mcy", "停止扫描...");
            if (bluetoothAdapter != null && mLeScanCallback != null) {
                bluetoothAdapter.stopLeScan(mLeScanCallback);
            }
            if (bluetoothLeScanner != null && scanCallback != null) {
                bluetoothLeScanner.stopScan(scanCallback);
            }

//            }
        }


        /**
         * 连接
         */

        public void connectLeDevice(Context context, BluetoothDevice device) {
            bluetoothGatt = device.connectGatt(context, false, mBluetoothGattCallback);
        }

        public void connection(Context context, String address) {
            if (BluetoothAdapter.checkBluetoothAddress(address)) {
                BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(address);
                if (remoteDevice == null) {
                    Log.e("mcy", "remoteDevice not found.  Unable to connect.");
                }
                connectLeDevice(context, remoteDevice);
            } else {
                Log.e("mcy", "remoteDevice address 不可用");
            }
        }

        /**
         * 向蓝牙发送数据
         */
        public void sendDataToBT() {
            if (gattCharacteristic != null && bluetoothGatt != null) {
                //设置读数据的UUID
                if (indexTpye == 1) {
                    gattCharacteristic = gattService.getCharacteristic(UUID.fromString("49535343-1E4D-4BD9-BA61-23C647249616"));
                } else {
                    gattCharacteristic = gattService.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
                }
                bluetoothGatt.setCharacteristicNotification(gattCharacteristic, true);
                for (byte[] datum : data) {
                    Log.e("mcy_devidedPacket", "" + Arrays.toString(datum));
                    gattCharacteristic.setValue(datum);
                    writeData();
                }
            }

        }

        private void writeData() {
            try {
                boolean b = bluetoothGatt.writeCharacteristic(gattCharacteristic);
                if (b) {
                    Thread.sleep(100);
                } else {
                    for (int i = 0; i < 10; i++) {
                        Thread.sleep(100);
                        writeData();
                    }
                    return;
                }
                Log.e("mcy", "发送数据是否成功:" + b);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * 断开连接
         */
        public void cancleConnection() {
            if (bluetoothGatt != null) {
                bluetoothGatt.close();
                Log.e("mcy", "主动断开连接...");
            }
        }

    }


    @Override
    public IBinder onBind(Intent intent) {
        return new BleBlueToothBind();
    }
}
