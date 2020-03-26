package cn.aln.mqtt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.github.mikephil.charting.charts.LineChart;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //定义变量
    private String TAG = "MainActivity";
    private MyMqttService mqttService;
    private EditText input;
    private Button warn;
    private TextView rece;
    private String imei;
    private String ip;
    private String port;
    private String sub ,name ,psd;
    private String item_name;

    //解析数据
    private String message;
    private String flag;
    private String type;
    private String check;
    private int heartbeat;
    private int tem;
    private int hum;
    private String readchecksum;

    private DynamicLineChartManager dynamicLineChartManager2;
    private DynamicLineChartManager dynamicLineChartManager3;
    private List<Integer> list = new ArrayList<>(); //数据集合
    private List<String> names1 = new ArrayList<>(); //折线名字集合
    private List<String> names2 = new ArrayList<>(); //折线名字集合
    private List<Integer> colour1 = new ArrayList<>();//折线颜色集合
    private List<Integer> colour2 = new ArrayList<>();//折线颜色集合

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        //设置透明栏为透明
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        rece = findViewById(R.id.rece);
        input = findViewById(R.id.input);
        warn = findViewById(R.id.warn);

        //getStringExtra拿到从IpHostActivity发来的数据
        psd = getIntent().getStringExtra("psd");
        name = getIntent().getStringExtra("name");
        sub = getIntent().getStringExtra("sub");
        ip = getIntent().getStringExtra("ip");
        port = getIntent().getStringExtra("port");
        item_name = getIntent().getStringExtra("item_name");

        //图表
        LineChart mChart2 = (LineChart) findViewById(R.id.dynamic_chart2);
        LineChart mChart3 = (LineChart) findViewById(R.id.dynamic_chart3);
        //折线名字
        names1.add("湿度");
        names2.add("温度");
        //names.add("其他");
        //折线颜色
        colour1.add(Color.BLUE);
        colour2.add(Color.RED);
        //colour.add(Color.BLUE);

        dynamicLineChartManager2 = new DynamicLineChartManager(mChart2, names1, colour1);
        dynamicLineChartManager3 = new DynamicLineChartManager(mChart3, names2, colour2);

        dynamicLineChartManager2.setYAxis(100, 0, 10);
        dynamicLineChartManager3.setYAxis(100, 0, 10);

        //发送按钮
        warn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnected()) {
                    //消息内容
                    String msg = input.getText().toString();
                    //检测是否发送了空信息
                    if (msg.length()== 0){
                        Toast.makeText(MainActivity.this, "empty msg!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //消息发送到control主题
                    String topic = "control";
                    //消息策略
                    int qos = 0;
                    //是否保留
                    boolean retained = false;
                    //发布消息
                    publish(msg, topic, qos, retained,item_name);
                    input.setText("");
                } else {
                    Toast.makeText(MainActivity.this, "断开连接", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //获取手机状态及权限，尝试连接
        GetIMEI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //没搞懂这个@SuppressLint("HardwareIds")的作用，貌似是提高代码运作效率系统提示添加的静态检查器
    @SuppressLint("HardwareIds")
    private void GetIMEI() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            //申请手机状态权限
            Log.d(TAG, "********没有权限 去申请");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 2);
        } else {
            Log.d(TAG, "*********** 已经有权限了");
            //获取电话权限(设备信息、sim卡信息以及 网络信息)
            TelephonyManager mTelephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            //保证mTelephony != null为真，否则提示警告，未获取电话权限
            assert mTelephony != null;
            if (mTelephony.getDeviceId() != null) {
                //获取imei
                imei = mTelephony.getDeviceId();
            } else {
                imei = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            }
            Log.d(TAG, "imei " + imei);
            if (imei != null) {

                //构造mqtt服务
                buildEasyMqttService();
                connect();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop");
    }

    //判断服务是否连接
    private boolean isConnected() {
        return mqttService.isConnected();
    }

    //发布消息
    private void publish(String msg, String topic, int qos, boolean retained,String item_name) {
        String message = "{\"Flag\":\""+item_name+"\",\"Message\":\""+msg+"\"}";
        mqttService.publish(message, topic, qos, retained);
    }

    //断开连接
    private void disconnect() {
        mqttService.disconnect();
    }


    //关闭连接
    private void close() {
        mqttService.close();
    }


    //订阅主题
    private void subscribe() {
        String[] topics = new String[]{sub};
        //主题对应的推送策略 分别是0, 1, 2 建议服务端和客户端配置的主题一致
        // 0 表示只会发送一次推送消息 收到不收到都不关心
        // 1 保证能收到消息，但不一定只收到一条
        // 2 保证收到切只能收到一条消息
        int[] qoss = new int[]{0};
        mqttService.subscribe(topics, qoss);
    }

    //连接Mqtt服务器
private void connect() {
        mqttService.connect(new IEasyMqttCallBack() {
            String mesg;
            @Override
            public void messageArrived(String topic, String message, int qos) {
                //推送消息到达
                Log.e(TAG, "message= " + message);

                mesg = message;
                JSONObject json = JSONObject.parseObject(mesg);
                flag = json.getString(("Flag"));
                type = json.getString("Type");
                check = json.getString("_MsgId");
                heartbeat = Integer.valueOf(json.getString("HeartKeeper"));
                hum = Integer.valueOf(json.getString("Humidity"));
                tem = Integer.valueOf(json.getString("Temperature"));
                readchecksum  = json.getString("Checksum");
                if(item_name.equals(flag)){
                    char[] data = check.toCharArray();
                    int checksum=0x11;
                    int len=data.length;
                    for(int i=0;i<len;i++) checksum=((checksum&(0xff))^(data[i]&(0xff)))&(0xff);

                    if(Integer.valueOf(readchecksum)==checksum){
                        rece.setText("学号:"+ flag+"\n"+"订阅:"+type+"\n"+"序号:"+check+"\n"+"心跳:"+heartbeat+"\n"+"湿度:"+hum+"\n"+"温度:"+tem+"\n"+"校验:"+checksum);
                    }else{
                        rece.setText(check+"\n"+checksum+"\n"+readchecksum+"\n"+"校验‘’码错误");
                    }

                    list.add((hum));
                    list.add((tem));
                    list.add((int) (Math.random() * 99));
                    dynamicLineChartManager2.addEntry(list);
                    dynamicLineChartManager3.addEntry(list);
                    list.clear();
                }
            }
            @Override
            public void connectionLost(Throwable arg0) {
                //连接断开
                try {
                    Log.e(TAG + "connectionLost", arg0.toString());
                    Toast.makeText(MainActivity.this, "connectionLost", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {

                } finally {

                }
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken arg0) {
                //发送成功
                Log.e(TAG + "@deliveryComplete", "发送成功" + arg0.toString());
                Toast.makeText(MainActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void connectSuccess(IMqttToken arg0) {
                //连接成功
                Toast.makeText(MainActivity.this, "连接成功！", Toast.LENGTH_LONG).show();
                Log.e(TAG + "@@@@@connectSuccess", "success");
                subscribe();
            }
            @Override
            public void connectFailed(IMqttToken arg0, Throwable arg1) {
                //连接失败
                Log.e(TAG + "@@@@@connectFailed", "fail" + arg1.getMessage());
                Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //构建EasyMqttService对象
    private void buildEasyMqttService() {
        mqttService = new MyMqttService.Builder()
                //设置自动重连
                .autoReconnect(true)
                //设置清除回话session  true(false) 不收(收)到服务器之前发出的推送消息
                .cleanSession(true)
                //唯一标示 保证每个设备都唯一就可以 建议 imei，这里设置为自定义名称
                .clientId("MI_9")
                .userName(name)
                .passWord(psd)
                //mqtt服务器地址 格式例如：
                //  tcp://iot.eclipse.org:1883
                .serverUrl("tcp://"+ip+":"+port)
                //心跳包默认的发送间隔
                .keepAliveInterval(20)
                .timeOut(10)
                //构建出EasyMqttService 建议用application的context
                .bulid(this.getApplicationContext());
    }
}