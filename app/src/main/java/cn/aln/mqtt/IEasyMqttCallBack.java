package cn.aln.mqtt;


import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;

public interface IEasyMqttCallBack {

    //收到消息，参数：主题，message内容，接收qos策略
    void messageArrived(String topic, String message, int qos);

    //连接断开，抛出异常
    void connectionLost(Throwable arg0);

    //发送成功
    void deliveryComplete(IMqttDeliveryToken arg0);

    //连接成功
    void connectSuccess(IMqttToken arg0);

    //连接失败
    void connectFailed(IMqttToken arg0, Throwable arg1);

}
