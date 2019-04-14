package com.chunyan.bluetoothtest2.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.chunyan.bluetoothtest2.broadcastreceiver.BlueReceiver;
import com.chunyan.bluetoothtest2.callback.ClientCallBack;
import com.chunyan.bluetoothtest2.callback.ServiceCallback;
import com.chunyan.bluetoothtest2.task.AcceptThread;
import com.chunyan.bluetoothtest2.task.ClientThread;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by machunyan on 2019/4/14.
 */
public class ClassicsBlueToothService extends Service {
    private IntentFilter filter;
    private BlueReceiver pinBlueReceiver;
    private BluetoothAdapter bluetoothAdapter;
    private ClientThread sendDataThread;
    private BluetoothSocket bluetoothSocket;
    private ClientCallBack blueCallBack;
    private AcceptThread acceptThread;

    @Override
    public void onCreate() {
        super.onCreate();
        //获取蓝牙适配器
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //注册广播
        //蓝牙广播,系统自动发送广播,只要设置制定的 action 即可
        filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//开始扫描
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//结束扫描
        filter.addAction(BluetoothDevice.ACTION_FOUND);//发现设备
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);//发起配对请求
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//配对状态
        pinBlueReceiver = new BlueReceiver(blueCallBack);

        registerReceiver(pinBlueReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(pinBlueReceiver);
    }

    public class ClassicaBlueToothBind extends Binder {

        public BluetoothAdapter getAdapter() {
            return bluetoothAdapter;
        }

        public void setBlueCallback(ClientCallBack callback) {
            ClassicsBlueToothService.this.blueCallBack = callback;
            pinBlueReceiver.setCallBack(callback);
        }

        /**
         * 扫描的方法 返回true 扫描成功
         * 通过接收广播获取扫描到的设备
         *
         * @return
         */
        public boolean scanBlueTooth() {
            //当前是否在扫描，如果是就取消当前的扫描，重新扫描
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
            //此方法是个异步操作，一般搜索12秒
            return bluetoothAdapter.startDiscovery();
        }

        /**
         * 取消扫描蓝牙
         *
         * @return true 为取消成功
         */
        public boolean cancelScanBule() {
            return bluetoothAdapter.cancelDiscovery();
        }

        /**
         * 配对（配对成功与失败通过广播返回）
         * ！！！弹出配对框
         *
         * @param device
         */
        public void pinBlueTooth(BluetoothDevice device) {
            if (device == null) {
                Log.e("mcy", "设备不可以为空");
                return;
            }
            //配对之前把扫描关闭
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
            //判断设备是否配对，没有配对在配，配对了就不需要配了
            if (device.getBondState() == BluetoothDevice.BOND_NONE) {//BOND_NONE 没有配对状态
                Log.d("mcy", "attemp to bond:" + device.getName());
                try {
                    boolean returnValue = device.createBond();
                    Log.e("是否配对成功：", "" + returnValue);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.d("mcy", "配对失败");

                }
            }
        }

        /**
         * 取消配对（取消配对成功与失败通过广播返回 也就是配对失败）
         *
         * @param device
         */
        public void cancelPinBuleTooth(BluetoothDevice device) {
            if (device == null) {
                Log.d("mcy", "设备不可以为空");
                return;
            }
            //判断设备是否配对，没有配对就不用取消了
            if (device.getBondState() != BluetoothDevice.BOND_NONE) {
                Log.d("mcy", "配对--" + device.getName());
                try {
                    Method removeBondMethod = device.getClass().getMethod("removeBond");
                    Boolean returnValue = (Boolean) removeBondMethod.invoke(device);
                    returnValue.booleanValue();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.d("mcy", "取消配对失败");
                }
            }
        }

        /**
         * @param uuid   用户指定的 uuid
         * @param device 连接的设备
         */



        public void connectionBlueTooth(String uuid, BluetoothDevice device) {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid));
                if (bluetoothSocket != null && !bluetoothSocket.isConnected()) {
                    sendDataThread = new ClientThread(bluetoothSocket, blueCallBack);
                    sendDataThread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * 写数据
         */
        public void sendData(byte[] data) {
            if (sendDataThread != null) {
                sendDataThread.write(data);
            }
        }

        /**
         * 写数据断开连接
         *
         * @return
         */
        public void cancleConnecion() {
            sendDataThread.closeSocket();
        }


        /**
         * 读数据
         */
        public void readListern(String name, String uuid, ServiceCallback callBack) {
            acceptThread = new AcceptThread(name, uuid, bluetoothAdapter, callBack);
            acceptThread.start();
        }

        /**
         * 写数据断开连接
         *
         * @return
         */
        public void cancleServiceConnecion() {
            acceptThread.closeSocket();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ClassicaBlueToothBind();
    }
}
