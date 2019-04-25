package com.chunyan.bluetoothtest2;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chunyan.bluetoothtest2.callback.BleResultCallBack;
import com.chunyan.bluetoothtest2.callback.ClientCallBack;
import com.chunyan.bluetoothtest2.callback.ServiceCallback;
import com.chunyan.bluetoothtest2.service.BleBlueToothService;
import com.chunyan.bluetoothtest2.service.ClassicsBlueToothService;
import com.chunyan.bluetoothtest2.utils.LocalUtils;
import com.chunyan.bluetoothtest2.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "mcy";
    private TextView textView;
    private TextView textView2;
    private TextView textView3;
    private EditText editTxt;
    String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
    private final int openBTCode = 100;
    // ----------------经典蓝牙------------------
    private ClassicsBlueToothService.ClassicaBlueToothBind classicaBTBind;
    private ServiceConnection classicaConnection;
    private List<BluetoothDevice> devicesList = new ArrayList<>();

    //---------------低功耗蓝牙----------------
    private BleBlueToothService.BleBlueToothBind bleBTBind;
    private ServiceConnection bleConnection;
    private BluetoothAdapter.LeScanCallback leScanCallback;
    private ScanCallback scanCallback;
    private ClientCallBack blueCallBack;
    private String text = "";


    private String uuid = "6db14d27-04f1-4df8-98ca-356dfc16ee43";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        //检查权限   >6.0以上版本需要动态的申请定位权限,< 6.0 清单文件声明了即可
        if (LocalUtils.checkLocalPermissiion(this, permissions)) {
            initClassica();
            initBle();
        }

    }


    private void initView() {
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);
        editTxt = findViewById(R.id.editTxt);
        findViewById(R.id.button5).setOnClickListener(this);
        findViewById(R.id.button11).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button5://经典--写数据
                text = editTxt.getText().toString();
                classicaBTBind.sendData(text.getBytes());
                break;
            case R.id.button11://低功耗--发送数据
                break;

        }
    }

    private void initBle() {
        //api<21回调这个借口
        leScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

                if (!TextUtils.isEmpty(device.getName())) {
                    if (!devicesList.contains(device)) {
                        devicesList.add(device);
                        Log.e("mcy", "扫描到设备-->" + device.getName());
                        textView.setText(textView.getText() + "\n" + device.getName());
                    }

                }


            }
        };
        //api>21回调这个借口
        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                if (!TextUtils.isEmpty(result.getDevice().getName())) {
                    if (!devicesList.contains(result.getDevice())) {
                        devicesList.add(result.getDevice());
                        textView.setText(textView.getText() + "\n" + result.getDevice().getName());
                    }
                    if (result.getDevice().getName().equals("00doos009000012147")) {//连接制定的设备。！！！！！测试使用！！！！！！
                        Log.e("mcy", "扫描到设备-->" + result.getDevice().getName());
                        bleBTBind.stopScan(leScanCallback, scanCallback);
                        bleBTBind.connection(MainActivity.this, result.getDevice().getAddress());
                    }
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
                    } else {
                        //========================开始执行工作=============================
                        bleBTBind.scanLeDevice(leScanCallback, scanCallback);
                        final StringBuilder stringBuilder = new StringBuilder();
                        bleBTBind.setBleResultCallBack(new BleResultCallBack() {
                            @Override
                            public void onReturnResult(byte[] data) {
                                bleBTBind.stopScan(leScanCallback, scanCallback);
                                for (byte byteChar : data) {
                                    stringBuilder.append(String.format("%02X ", byteChar));
                                }
                                String returnedPacket = stringBuilder.toString().replace(" ", "");
                                byte[] packetByte = Utils.hexStringToByteArray(returnedPacket);
                                if (packetByte.length - 5 == Utils.getLengthFromToken(packetByte)) {
                                    Log.e("mcy_returnedPacket", returnedPacket);
                                    bleBTBind.cancleConnection();//取消连接
                                }
                            }

                            @Override
                            public void onDiscoverServicesSuccess() {
                                bleBTBind.stopScan(leScanCallback, scanCallback);
                                bleBTBind.sendDataToBT();//方式一
//                                bleBTBind.sendDataToBT2();//方式二

                            }
                        });
                    }
                } else {
                    Log.e("mcy", "此设备不支持蓝牙");
                }


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
//                if (classicaBTBind.getAdapter() != null) {
//                    //判断蓝牙是否开启
//                    if (!classicaBTBind.getAdapter().isEnabled()) {
//                        //打开蓝牙
//                        openBlueSync(MainActivity.this, openBTCode);
//                    } else {
//                        //==============从开始执行工作,释放开下面两行代码!!!================
//                        classicaBTBind.scanBlueTooth();//扫描蓝牙
//                        registReadListener();//注册读数据事件
//                    }
//                } else {
//                    Toast.makeText(MainActivity.this, "此设备不支持蓝牙", Toast.LENGTH_SHORT).show();
//                }


            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                classicaBTBind = null;

            }
        };
        bindService(new Intent(this, ClassicsBlueToothService.class), classicaConnection, BIND_AUTO_CREATE);
    }

    //经典蓝牙注册读数据事件
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


    //GPS
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LocalUtils.open_GPSCode://检查是否手机打开定位
                if (LocalUtils.checkGPSIsOpen(this)) {
                    LocalUtils.checkLocalPermissiion(this, permissions);
                } else {
                    LocalUtils.goToOpenGPS(this);
                }
                break;
        }
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
                            initClassica();//初始化经典蓝牙
                            initBle();//初始化低功耗蓝牙

                        }
                    }
                }
                break;
            case openBTCode://是否打开蓝牙
                if (bleBTBind.getAdapter().isEnabled()) {
                    initClassica();//初始化经典蓝牙
                    initBle();//初始化低功耗蓝牙
                } else {
                    openBlueSync(MainActivity.this, openBTCode);//打开蓝牙
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(classicaConnection);
        unbindService(bleConnection);
    }
}
