package com.android.bsl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener {

    private Button mBtnExit;
    private Button mBtnLogin;

    private EditText mEtName = null;
    private EditText mEtPwd = null;
    private SharedPreferences preferences;
    private String defaultIp;
    private String defaultPort;
    private SharedPreferences.Editor editor;
    private AlertDialog dialog;
    public static ClientThread rxListenerThread;
    BroadcastReceiver br;
    public static List<NodeInfo> nodelist;
    public static Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork() // 这里可以替换为detectAll()
                // 就包括了磁盘读写和网络I/O
                .penaltyLog() // 打印logcat，当然也可以定位到dropbox，通过文件保存相应的log
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects() // 探测SQLite数据库操作
                .penaltyLog() // 打印logcat
                .penaltyDeath().build());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // mBtnRegister = (Button)findViewById(R.id.title_right_button);
        // MapLocation ml = new MapLocation(mApp, this);

        // isContactNetWork();

        preferences = getSharedPreferences("ip&port", MODE_PRIVATE);
//        getSharedPreferences(name,mode)方法的第一个参数用于指定该文件的名称，名称不用带后缀，后缀会由Android自动加上。方法的第二个参数指定文件的操作模式，共有四种操作模式，这四种模式前面介绍使用文件方式保存数据时已经讲解过
        editor = preferences.edit();
        defaultIp = preferences.getString("ip", "192.168.3.113");
        defaultPort = preferences.getString("port", "8000");

        init();
        br = new BroadcastReceiver() {
//       在 Android 系统中，广播（Broadcast）是在组件之间传播数据的一种机制，这些组件可以位于不同的进程中，起到进程间通信的作用
//       BroadcastReceiver 是对发送出来的 Broadcast 进行过滤、接受和响应的组件。
//       首先将要发送的消息和用于过滤的信息（Action，Category）装入一个 Intent 对象，然后通过调用 Context.sendBroadcast() 、 sendOrderBroadcast() 方法把 Intent 对象以广播形式发送出去。 广播发送出去后，所以已注册的 BroadcastReceiver 会检查注册时的 IntentFilter 是否与发送的 Intent 相匹配，若匹配则会调用 BroadcastReceiver 的 onReceiver() 方法
            @Override
            public void onReceive(Context context, Intent intent) {
                mBtnLogin.setEnabled(true);

            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.action.nettestfinished");
        registerReceiver(br, intentFilter);

    }

    private void init() {
        // TODO Auto-generated method stub

        mEtName = (EditText) findViewById(R.id.etUserName);
        mEtName.setText(defaultIp);
        mEtPwd = (EditText) findViewById(R.id.etUserPwd);
        mEtPwd.setText(defaultPort);
        mBtnLogin = (Button) findViewById(R.id.btnOk);
        mBtnLogin.setOnClickListener(this);

        mBtnExit = (Button) findViewById(R.id.btnExit);
        mBtnExit.setOnClickListener(this);

    }

    @SuppressWarnings("deprecation")
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btnOk:
                if (!mEtName.getText().toString().equals("")) {
                    if (!mEtPwd.getText().toString().equals("")) {
                        if (!defaultIp.equalsIgnoreCase(mEtName.getText().toString()))
                            editor.putString("ip", mEtName.getText().toString());
                        if (!defaultPort.equalsIgnoreCase(mEtPwd.getText().toString()))
                            editor.putString("port", mEtPwd.getText().toString());
                        editor.commit();

                        boolean isConnected = NetworkDetector.detect(LoginActivity.this);
                        //NetworkDetector为自定义类
                        if (!isConnected) {
                            Toast.makeText(LoginActivity.this, "网络不可用!", Toast.LENGTH_LONG).show();
                        } else {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            //强行跳转
                            try {
                                InetSocketAddress socketAddress = new InetSocketAddress(mEtName.getText().toString(), Integer.parseInt(mEtPwd.getText().toString()));
                                Socket socket = new Socket();
                                socket.connect(socketAddress, 3000);

                                if (socket.isConnected()) {
                                    socket.close();

                                    Toast.makeText(LoginActivity.this, "登录成功!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    //startActivity用于实现界面切换
                                } else {
                                    Toast.makeText(LoginActivity.this, "登录失败!", Toast.LENGTH_SHORT).show();
                                }

                            } catch (NumberFormatException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                                Toast.makeText(LoginActivity.this, "登录失败!", Toast.LENGTH_SHORT).show();
                            } catch (SocketTimeoutException e) {
                                Toast.makeText(LoginActivity.this, "登录失败!", Toast.LENGTH_SHORT).show();
                            } catch (UnknownHostException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                                Toast.makeText(LoginActivity.this, "登录失败!", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                                Toast.makeText(LoginActivity.this, "登录失败!", Toast.LENGTH_SHORT).show();
                            }


                        }
                    }
                }
                break;
            case R.id.btnExit:
                finish();
                break;
        }
    }

    @Override
    @Deprecated
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub

        switch (id) {
            case 0x0001:
                dialog = new AlertDialog.Builder(LoginActivity.this).setTitle("连接成功").setMessage("网络连接正常!").create();
                return dialog;
            case 0x0002:
                dialog = new AlertDialog.Builder(LoginActivity.this).setTitle("连接失败").setMessage("抱歉，网络出错了！").create();
                return dialog;

            case 0x0003:
                dialog = new AlertDialog.Builder(LoginActivity.this).setTitle("当前网络不可用，是否去设置网络").setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                    }
                }).create();

                return dialog;
        }
        return null;
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
        // unregisterReceiver(br);
    }

    // -------------------------------------------------------------------------

    public static class NetworkDetector {
        public final static int NONE = 0;
        // 无网络
        public final static int WIFI = 1;
        // Wi-Fi
        public final static int MOBILE = 2;

        public static boolean detect(Activity act) {

            ConnectivityManager manager = (ConnectivityManager) act.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

            if (manager == null) {
                return false;
            }

            NetworkInfo networkinfo = manager.getActiveNetworkInfo();

            if (networkinfo == null || !networkinfo.isAvailable()) {
                return false;
            }

            return true;
        }

        public static int getNetworkState(Context context) {
            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            // 手机网络判断
            State state = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
            if (state == State.CONNECTED || state == State.CONNECTING) {
                return MOBILE;
            }
            // Wifi网络判断
            state = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
            if (state == State.CONNECTED || state == State.CONNECTING) {
                return WIFI;
            }

            return NONE;
        }
    }
}
