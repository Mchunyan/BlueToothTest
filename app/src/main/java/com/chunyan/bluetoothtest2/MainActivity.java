package com.chunyan.bluetoothtest2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chunyan.bluetoothtest2.callback.ClientCallBack;
import com.chunyan.bluetoothtest2.callback.ServiceCallback;
import com.chunyan.bluetoothtest2.service.BleBlueToothService;
import com.chunyan.bluetoothtest2.service.ClassicsBlueToothService;
import com.chunyan.bluetoothtest2.utils.LocalUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "mcy";
    private TextView textView;
    private TextView textView2;
    private TextView textView3;
    private EditText editTxt;
    private final int openBTCode = 100;
    // ----------------经典蓝牙------------------
    private ClassicsBlueToothService.ClassicaBlueToothBind classicaBTBind;
    private ServiceConnection classicaConnection;
    private List<BluetoothDevice> devicesList = new ArrayList<>();

    //---------------低功耗蓝牙----------------
    private BleBlueToothService.BleBlueToothBind bleBTBind;
    private ServiceConnection bleConnection;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    private BluetoothGattCallback mBluetoothGattCallback;
    private ClientCallBack blueCallBack;
    private String text = "";


    private String uuid = "6db14d27-04f1-4df8-98ca-356dfc16ee43";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //检查权限   >6.0以上版本需要动态的申请定位权限,< 6.0 清单文件声明了即可
        LocalUtils.checkPermissions(this);
        initView();
        initClassica();
        initBle();

    }


    private void initView() {
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);
        editTxt = findViewById(R.id.editTxt);
        findViewById(R.id.button5).setOnClickListener(this);
        findViewById(R.id.button6).setOnClickListener(this);
        findViewById(R.id.button11).setOnClickListener(this);
        findViewById(R.id.button12).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button5://经典--写数据
                text = editTxt.getText().toString();
                classicaBTBind.sendData(text.getBytes());
                break;
            case R.id.button6://经典--读数据
                registReadListener();
                break;
            case R.id.button11://低功耗--发送数据
                bleBTBind.sendData();
                break;
            case R.id.button12://低功耗--读数据
                bleBTBind.readData();
                break;
        }
    }

    private void initBle() {


        mBluetoothGattCallback = new BluetoothGattCallback() {
            @Override
            public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
                super.onPhyUpdate(gatt, txPhy, rxPhy, status);
            }

            @Override
            public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
                super.onPhyRead(gatt, txPhy, rxPhy, status);
            }

            //当连接状态发生改变
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.e("mcy", "连接成功...");
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
            }

            //调用mBluetoothGatt.readCharacteristic(characteristic)读取数据回调，在这里面接收数据
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                //这里面就是数据
                characteristic.getValue();
            }

            //发送数据后的回调
            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {//descriptor读
                super.onDescriptorRead(gatt, descriptor, status);
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {//descriptor写
                super.onDescriptorWrite(gatt, descriptor, status);
            }

            @Override
            public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
                super.onReliableWriteCompleted(gatt, status);
            }

            //调用mBluetoothGatt.readRemoteRssi()时的回调，rssi即信号强度
            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {//读Rssi
                super.onReadRemoteRssi(gatt, rssi, status);
            }

            @Override
            public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                super.onMtuChanged(gatt, mtu, status);
            }
        };

        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                //在这里可以把搜索到的设备保存起来,device.getName();获取蓝牙设备名字,device.getAddress();获取蓝牙设备mac地址
                //重复过滤方法，列表中包含不该设备才加入列表中，并刷新列表
                if (!devicesList.contains(device)) {
                    //将设备加入列表数据中
                    devicesList.add(device);
                }
                Log.e("mcy", "扫描到设备-->" + device.getName());
                textView.setText(textView.getText() + "\n" + device.getName());
                Log.e(TAG, "" + device.getName());
                //已配对的蓝牙
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {//BOND_BONDED 已经配对状态
                    textView2.setText(textView2.getText() + "\n" + device.getName());
                } else {
                    bleBTBind.connectLeDevice(MainActivity.this, devicesList.get(0), mBluetoothGattCallback);
                }

            }
        };
        bleConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                bleBTBind = ((BleBlueToothService.BleBlueToothBind) service);
                if (bleBTBind.getAdapter() != null) {
                    //判断蓝牙是否开启
                    if (!bleBTBind.getAdapter().isEnabled()) {
                        //打开蓝牙
                        openBlueSync(MainActivity.this, openBTCode);
                    }
                } else {
                    Log.e("mcy", "此设备不支持蓝牙");
                }

                //========================开始执行工作=============================
                bleBTBind.scanLeDevice(mLeScanCallback);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                bleBTBind = null;

            }
        };
        bindService(new Intent(this, BleBlueToothService.class), bleConnection, BIND_AUTO_CREATE);

    }


    private void initClassica() {
        blueCallBack = new ClientCallBack() {
            @Override
            public void onScanStarted() {
                Log.e("mcy", "开始扫描");

            }

            @Override
            public void onScanFinished() {
                Log.e("mcy", "结束扫描");
            }

            @Override
            public void onScanning(BluetoothDevice device) {
                Log.e("mcy", "扫描到设备-->" + device.getName());
                if (!devicesList.contains(device)) {
                    //将设备加入列表数据中
                    devicesList.add(device);
                }
                textView.setText(textView.getText() + "\n" + device.getName());
                Log.e(TAG, "" + device.getName());
                //已配对的蓝牙
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {//BOND_BONDED 已经配对状态
                    textView2.setText(textView2.getText() + "\n" + device.getName());
                } else {
                    classicaBTBind.pinBlueTooth(devicesList.get(0));
                }

            }

            @Override
            public void onBondRequest() {
                Log.e("mcy", "开始配对");

            }

            @Override
            public void onBondFail(BluetoothDevice device) {
                Log.e("mcy", "取消配对");
            }

            @Override
            public void onBonding(BluetoothDevice device) {
                Log.e("mcy", "配对中");
            }

            @Override
            public void onBondSuccess(BluetoothDevice device) {
                Log.e("mcy", "配对成功");
                classicaBTBind.connectionBlueTooth(uuid, device);
                //registReadListener(); //测试使用,双端通讯

            }

            @Override
            public void onConnectSuccess() {
                Log.e("mcy", "连接成功");
            }

            @Override
            public void onConnectFail(String errorMsg) {
                Log.e("mcy", "连接失败" + errorMsg);
            }

            @Override
            public void onConnectClose() {
                Log.e("mcy", "连接关闭");

            }
        };
        classicaConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                classicaBTBind = ((ClassicsBlueToothService.ClassicaBlueToothBind) service);
                if (blueCallBack != null) {
                    classicaBTBind.setBlueCallback(blueCallBack);//设置广播监听
                }
                if (classicaBTBind.getAdapter() != null) {
                    //判断蓝牙是否开启
                    if (!classicaBTBind.getAdapter().isEnabled()) {
                        //打开蓝牙
                        openBlueSync(MainActivity.this, openBTCode);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "此设备不支持蓝牙", Toast.LENGTH_SHORT).show();
                }
                //========================开始执行工作=============================
                //classicaBTBind.scanBlueTooth();//扫描蓝牙

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                classicaBTBind = null;

            }
        };
        bindService(new Intent(this, ClassicsBlueToothService.class), classicaConnection, BIND_AUTO_CREATE);
    }

    //注册读数据事件
    private void registReadListener() {
        classicaBTBind.readListern("Demo", uuid, new ServiceCallback() {

            @Override
            public void onConnectSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("mcy", "读数据连接成功~");
                    }
                });
            }

            @Override
            public void onConnectFail(final String errorMsg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("mcy", "读数据连接失败" + errorMsg);
                    }
                });

            }

            @Override
            public void onConnectClose() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("mcy", "读数据连接关闭");

                    }
                });
            }

            @Override
            public void onResultMessage(final byte[] data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView3.setText(new String(data));
                    }
                });
            }
        });
    }


    /**
     * 自动打开蓝牙（同步）
     * 这个方法打开蓝牙会弹出提示
     * 需要在onActivityResult 方法中判断resultCode == RESULT_OK  true为成功
     */
    public void openBlueSync(Activity activity, int requestCode) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, requestCode);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(classicaConnection);
        unbindService(bleConnection);
    }

    /**
     * 权限回调
     */
    @Override
    public final void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LocalUtils.permission_LocationCode://是否打开允许定位权限
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            LocalUtils.onPermissionGranted(this, permissions[i]);
                        }
                    }
                }
                break;
            case LocalUtils.open_GPSCode://检查是否手机打开定位
                if (LocalUtils.checkGPSIsOpen(this)) {
                    LocalUtils.checkPermissions(this);
                } else {
                    LocalUtils.goToOpenGPS(this);
                }
                break;
        }
    }


}
