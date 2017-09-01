package com.android.bsl;

import java.util.Timer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

public class ControlInterface extends Activity {
    public static NodeInfo nodeinfo;
    private int sensorType;
    private boolean isbtn1Checked, isbtn2Checked, isbtn3Checked, isbtn4Checked;
    private int currentProcess;
    private TextView titleView;
    private Timer timer;
    public static Handler handler;
    private View view1, view2;
    RadioGroup mada_radioGroup;
    RadioButton mada_rb1;
    RadioButton mada_bb2;
    SeekBar mada_seekBar;
    Button mada_button;
    private ImageView io_deng1;

    private ToggleButton io_btn1;

    private Button io_sureBtn;
    private Button io_cancelBtn;
    private static final String TAG = "ControlInterface";
    private boolean[] btnCheckedState = new boolean[4];
    private boolean[] btnRecordState = new boolean[4];
    private byte lastDengState = -1;
    private boolean initFinished;
    private byte mada_turn;
    private int mada_sudu;
    private byte dengState = 0;
    public static String currentUiName = "";
    private Timer timer2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        nodeinfo = (NodeInfo) bundle.getParcelable("node");
        sensorType = bundle.getInt("type");
        view1 = getLayoutInflater().inflate(R.layout.mada_control, null);
        view2 = getLayoutInflater().inflate(R.layout.deng_control, null);

        switch (sensorType) {
            case 0x08:

                setContentView(view1);
//                titleView = (TextView) findViewById(R.id.tvActionBarTitle);
//                titleView.setText("马达控制");
                ProcessView1();

                break;
            case 0x17:

                setContentView(view2);
//                titleView = (TextView) findViewById(R.id.tvActionBarTitle);
//                titleView.setText("I/O控制");
                ProcessView2();


                break;
        }


        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {

                super.handleMessage(msg);
                switch (msg.what) {
                    case 0x2222:

                        nodeinfo = (NodeInfo) msg.obj;

                        changeDengUIState();
                        Log.i(TAG, "changeDengUIState");

                        break;
                    case 0x2223:
                        nodeinfo = (NodeInfo) msg.obj;
                        changeMaDaUi();
                        break;
                    default:

                        break;
                }
            }

        };

    }

    private void ProcessView1() {
        mada_radioGroup = (RadioGroup) view1.findViewById(R.id.rg);
        mada_rb1 = (RadioButton) view1.findViewById(R.id.rb1);
        mada_rb1.setChecked(true);
        mada_bb2 = (RadioButton) view1.findViewById(R.id.rb2);
        mada_radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {


            }
        });
        mada_seekBar = (SeekBar) view1.findViewById(R.id.pb1);
        mada_seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                // TODO Auto-generated method stub
                currentProcess = arg1;
            }
        });

        mada_button = (Button) view1.findViewById(R.id.sure);
        mada_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                byte[] buffer = new byte[18];
                buffer[0] = buffer[1] = (byte) 0xff;
                buffer[2] = 0x12;
                buffer[3] = (byte) 13;
                String temp1 = (nodeinfo.getId().substring(0, 2));
                String temp2 = (nodeinfo.getId().substring(2, 4));

                buffer[4] = (byte) (Integer.parseInt(temp1, 16));
                buffer[5] = (byte) (Integer.parseInt(temp2, 16));
                buffer[6] = (byte) 0x04;

                if (mada_rb1.isChecked()) {
                    buffer[7] = (byte) 0x00;
                    buffer[8] = (byte) currentProcess;
                } else {
                    buffer[7] = (byte) currentProcess;
                    buffer[8] = (byte) 0x00;

                }
                buffer[9] = (byte) 0x00;
                buffer[10] = (byte) 0xc8;
                for (int i = 11; i < 17; i++) {
                    buffer[i] = (byte) 0xfe;
                }
                byte[] temp3 = new byte[17];
                for (int i = 0; i < temp3.length; i++) {
                    temp3[i] = buffer[i];
                }
                byte temp4 = (byte) 0x00;

                for (int i = 0; i < temp3.length; i++) {
                    temp4 ^= temp3[i];
                }
                buffer[17] = temp4;
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putByteArray("sendData", buffer);
                message.what = 0x1112;
                message.setData(bundle);
                MainActivity.mainHandler.sendMessage(message);

            }
        });
        changeMaDaUi();

    }

    private void ProcessView2() {
        Log.i(TAG, "YYYYYYYYYYYYYYYY" + nodeinfo.getDengState());
        io_deng1 = (ImageView) view2.findViewById(R.id.light1);
//	io_deng2=(ImageView)view2.findViewById(R.id.light2);
//	io_deng3=(ImageView)view2.findViewById(R.id.light3);
//	io_deng4=(ImageView)view2.findViewById(R.id.light4);
        io_btn1 = (ToggleButton) view2.findViewById(R.id.control_1);

//	io_btn2=(ToggleButton)view2.findViewById(R.id.control_2);

//	io_btn3=(ToggleButton)view2.findViewById(R.id.control_3);
//	io_btn4=(ToggleButton)view2.findViewById(R.id.control_4);
        io_cancelBtn = (Button) view2.findViewById(R.id.cancel);
        io_cancelBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //	io_btn1.setChecked(btnRecordState[0]);


            }
        });
        io_sureBtn = (Button) view2.findViewById(R.id.sure);

        io_sureBtn.setOnClickListener(new MyOnClickListener());

        io_btn1.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                btnCheckedState[0] = arg1;


            }

        });
        changeDengUIState();


    }

    private void changeDengUIState() {
        Log.i(TAG, "YYYYYYYYYYYYYYYY" + nodeinfo.getDengState());
//	if(nodeinfo.getDengState()!=dengState)
//	{
        dengState = nodeinfo.getDengState();
        if (dengState == 0) {
            //	io_deng1.setBackgroundDrawable(getResources().getDrawable(R.drawable.device_wallbtn_close));
            io_deng1.setImageResource(R.drawable.device_wallbtn_close);
            io_btn1.setChecked(false);
        } else {
            io_deng1.setImageResource(R.drawable.device_wallbtn_open);
            //		io_btn1.setBackgroundDrawable(getResources().getDrawable(R.drawable.device_wallbtn_open));
            io_btn1.setChecked(true);
        }

        //}
    }

    private class MyOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            clickProcess();

        }

    }


    public void clickProcess() {
        isbtn1Checked = io_btn1.isChecked();


        byte[] buffer = new byte[18];
        buffer[0] = buffer[1] = (byte) 0xff;
        buffer[2] = 0x12;
        buffer[3] = (byte) 13;
        String temp1 = (nodeinfo.getId().substring(0, 2));
        String temp2 = (nodeinfo.getId().substring(2, 4));
        System.out.println(temp1);
        System.out.println(temp2);
        buffer[4] = (byte) (Integer.parseInt(temp1, 16));
        buffer[5] = (byte) (Integer.parseInt(temp2, 16));
        buffer[6] = (byte) 0x08;
        if (isbtn1Checked)
            buffer[7] = (byte) 0x01;
        else {
            buffer[7] = (byte) 0x00;
        }


        for (int i = 8; i < 17; i++) {
            buffer[i] = (byte) 0xfe;
        }
        byte[] temp = new byte[17];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = buffer[i];
        }
        byte temp3 = (byte) 0x00;

        for (int i = 0; i < temp.length; i++) {
            temp3 ^= temp[i];
        }
        buffer[17] = temp3;
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putByteArray("sendData", buffer);
        message.what = 0x1112;
        message.setData(bundle);
        Log.i(TAG, "ONCLICKED");
        MainActivity.mainHandler.sendMessage(message);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

    }


    /*
    private byte decodeRecordeState()
    {
        int temp=0;
        if(btnRecordState[0])
            temp+=1000;
        if(btnRecordState[1])
            temp+=100;
        if(btnRecordState[2])
            temp+=10;
        if(btnRecordState[3])
            temp+=1;
        byte result=0;
        switch(temp)
        {
        case 1111:
            result=(byte)0x00;
            break;
        case 1110:
            result=(byte)0x10;
            break;
        case 1101:
            result=(byte)0x20;
            break;
        case 1100:
            result=(byte)0x30;
            break;
        case 1011:
            result=(byte)0x40;
            break;
        case 1010:
            result=(byte)0x50;
            break;
        case 1001:
            result=(byte)0x60;
            break;
        case 1000:
            result=(byte)0x70;
            break;
        case 111:
            result=(byte)0x80;
            break;
        case 110:
            result=(byte)0x90;
            break;
        case 101:
            result=(byte)0xa0;
            break;
        case 100:
            result=(byte)0xb0;
            break;
        case 11:
            result=(byte)0xc0;
            break;
        case 10:
            result=(byte)0xd0;
            break;
        case 1:
            result=(byte)0xe0;
            break;
        case 0:
            result=(byte)0xf0;
            break;
        }
        return result;
    }

    private void syncRecord()
    {
        for(int i=0;i<4;i++)
        {
            btnRecordState[i]=btnCheckedState[i];
        }
    }
    */
    private void changeMaDaUi() {
        if (initFinished && mada_turn == nodeinfo.getMada_turnto() && mada_sudu == nodeinfo.getMada_sudu()) {
            return;
        }
        if (nodeinfo.getMada_turnto() == 1) {
            mada_rb1.setChecked(true);
        } else {
            mada_bb2.setChecked(true);
        }
        mada_seekBar.setProgress(nodeinfo.getMada_sudu());
        mada_turn = nodeinfo.getMada_turnto();
        mada_sudu = nodeinfo.getMada_sudu();
        initFinished = true;
    }
}
