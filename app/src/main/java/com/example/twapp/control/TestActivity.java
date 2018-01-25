package com.example.twapp.control;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.twapp.R;
import com.example.twapp.base.BaseActivity;
import com.example.twapp.utils.CRCUtil;
import com.example.twapp.utils.MyEventBus;
import com.example.twapp.utils.ReadSerialPort;
import com.speedata.libutils.DataConversionUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class TestActivity extends BaseActivity {
    TextView tvTrue, tvFalse, numtrue, numfasle, numtotal, tv;
    Button btn;
    ListView listView;
    boolean isFlag = true;
    private long start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        tvFalse = findViewById(R.id.tv_false);
        tvTrue = findViewById(R.id.tv_true);
        numtrue = findViewById(R.id.num_true);
        numfasle = findViewById(R.id.num_false);
        numtotal = findViewById(R.id.num_total);
        tv = findViewById(R.id.num_false2);
        listView = findViewById(R.id.listview);

        btn = findViewById(R.id.start);
        EventBus.getDefault().register(this);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFlag) {
                    start = System.currentTimeMillis();
                    total = 0;
                    trues = 0;
                    falses = 0;
                    falses2 = 0;
                    numfasle.setText("失败：");
                    numtrue.setText("成功：");
                    numtotal.setText("总接收：");
                    tv.setText("crc错误：");

                    tvFalse.setText("");
                    tvTrue.setText("");
                    isFlag = false;
                    ReadSerialPort.startReader();
                    btn.setText("关闭接收数据");
                } else {
                    isFlag = true;
                    ReadSerialPort.onDestroy();
                    btn.setText("开始接收数据");
                }
            }
        });
    }

    int total = 0;
    int trues = 0;
    int falses = 0;
    int falses2 = 0;

    /**
     * 接收串口数据
     *
     * @param myEventBus
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getSerilportData(MyEventBus myEventBus) {
        byte[] originalData = myEventBus.getTemperature();
        numtotal.setText("总接收：" + (total += 1));
        if (originalData == null || originalData.length <= 2) {
            System.out.println("invalid data for len");
            numfasle.setText("失败：" + (falses += 1));
            tvFalse.setText(DataConversionUtils.byteArrayToString(originalData));
        } else if (originalData[1] + 1 != originalData.length) {
            numfasle.setText("失败：" + (falses += 1));
            tvFalse.setText(DataConversionUtils.byteArrayToString(originalData));
            System.out.println("invalid data for len" + (originalData[1] + 1) + " !=" + originalData.length);
        } else if (originalData[0] != 0x5A) {
            System.out.println("invalid header");
            numfasle.setText("失败：" + (falses += 1));
            tvFalse.setText(DataConversionUtils.byteArrayToString(originalData));
        } else if (DataConversionUtils.byteArrayToInt(CRCUtil.GetCRC(0xffff, cutBytes(originalData, 1, originalData.length - 1))) != 0) {
            //TODO 判断 CRC
            Log.e("log", "校验失败 数据" + DataConversionUtils.byteArrayToString(originalData));
            tv.setText("crc失败：" + (falses2 += 1));
            tvFalse.setText(DataConversionUtils.byteArrayToString(originalData));
            byte[] ff = cutBytes(originalData, 1, originalData.length - 1);
            byte[] gg = CRCUtil.GetCRC(0xffff, ff);
            int ss = DataConversionUtils.byteArrayToInt(gg);
        } else {
            Log.i("log", "校验通过 数据" + DataConversionUtils.byteArrayToString(originalData));
            System.out.println("valid ok");
            numtrue.setText("成功：" + (trues += 1));
            tvTrue.setText(DataConversionUtils.byteArrayToString(originalData));
        }
//        parseDatas(originalData);
    }

//    public void parseDatas(byte[] b) {
//        if (TWManager.isValid(b)) {
//            TWManager.assembleData().parseFlag().decodeSNandpayload().parseSN().parsePayload();
//            initInfo();
//        } else {
//            falses += 1;
//        }
//        tvTrue.setText("正确：" + trues + "错误：" + falses + "小于10秒：" + millis1 + "大于10小于1分：" + millis2 + "大于1分：" + millis3);
//    }
//
//    int millis1 = 0;
//    int millis2 = 0;
//    int millis3 = 0;
//
//    private void initInfo() {
//        String num = TWManager.getBody().getRunningNumber();
//        if (num.equals("130")) {
//            trues += 1;
//            Log.i("test", "上: "+start+"現在："+System.currentTimeMillis());
//            if (System.currentTimeMillis() - start < 10) {
//                millis1 += 1;
//            }
//            else if (System.currentTimeMillis() - start > 60000) {
//                millis2 += 1;
//            }
//            else {
//                millis3 += 1;
//            }
//            start = System.currentTimeMillis();
//
//            tvTrue.setText("正确：" + trues + "错误：" + falses + "小于10秒：" + millis1 + "大于10小于1分：" + millis2 + "大于1分：" + millis3);
//        } else if (num.equals("128")) {
//
//        }
//
//    }

    /**
     * 截取数组
     *
     * @param bytes  被截取数组
     * @param start  被截取数组开始截取位置
     * @param length 新数组的长度
     * @return 新数组
     */
    public static byte[] cutBytes(byte[] bytes, int start, int length) {
        byte[] res = new byte[length];
        System.arraycopy(bytes, start, res, 0, length);
        return res;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
