/*‘"
 * 
 Basic ESP8266 MQTT example

 This sketch demonstrates the capabilities of the pubsub library in combination
 with the ESP8266 board/library.

 It connects to an MQTT server then:
  - publishes "hello world" to the topic "outTopic" every two seconds
  - subscribes to the topic "inTopic", printing out any messages
    it receives. NB - it assumes the received payloads are strings not binary
  - If the first character of the topic "inTopic" is an 1, switch ON the ESP Led,
    else switch it off

 It will reconnect to the server if the connection is lost using a blocking
 reconnect function. See the 'mqtt_reconnect_nonblocking' example for how to
 achieve the same result without blocking the main loop.

 To install the ESP8266 board, (using Arduino 1.6.4+):
  - Add the following 3rd party board manager under "File -> Preferences -> Additional Boards Manager URLs":
       http://arduino.esp8266.com/stable/package_esp8266com_index.json
  - Open the "Tools -> Board -> Board Manager" and click install for the ESP8266"
  - Select your ESP8266 in "Tools -> Board"

*/

#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include "dht11.h"
dht11 DHT11;
#define DHT11PIN 5
#include <Wire.h>  // Only needed for Arduino 1.6.5 and earlier
#include "SSD1306Wire.h" // legacy include: `#include "SSD1306.h"`
SSD1306Wire  display(0x3c, D4, D5);

// Update these with values suitable for your network.

//获取wifi连接数据
//const char* ssid = "gkdnmd";
//const char* password = "wxd654221b";
const char* ssid = "iPhone";
const char* password = "zzx135791";
const char* mqtt_server = "103.46.128.45";
const int port=40646;

WiFiClient espClient;
PubSubClient client(espClient);
long lastMsg = 0;
char msg[200];
String rsvm="";
int heartkeeper = 1;
byte keeper=0;

void setup_wifi() {

  delay(10);
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

//连接wifi
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  randomSeed(micros());

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}

void callback(char* topic, byte* payload, unsigned int length) {
  DynamicJsonDocument doc(200);
  DeserializationError err=deserializeJson(doc,(const char*)payload);
  if(!err){
    String flag=doc["Flag"];
    if(flag.equals("31701233")){
      Serial.print("Message arrived [");
      Serial.print(topic);
      Serial.println("]:");
      Serial.println("=======start=========");
      Serial.print("Flag:");Serial.println(flag);
      String message=doc["Message"];rsvm=message;
      Serial.print("Message:");Serial.println(rsvm);
      Serial.println("========end=========");
    }
    else Serial.println("Message err");
  }else Serial.println("Message err");
  
}

void reconnect() {
  while (!client.connected()) {
    Serial.println("Attempting MQTT connection...");
    String clientId = "IoT-31701233-";
    clientId += String(random(0xffff), HEX);
    if (client.connect(clientId.c_str())) {
      Serial.println("connected");
      client.subscribe("control");
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      delay(5000);
    }
  }
}

void setup() {
  pinMode(BUILTIN_LED, OUTPUT);     // Initialize the BUILTIN_LED pin as an output
  Serial.begin(115200);
  setup_wifi();
  client.setServer(mqtt_server, port);
  client.setCallback(callback);
  Serial.println("DHT11 TEST PROGRAM ");
  Serial.print("LIBRARY VERSION: ");
  Serial.println(DHT11LIB_VERSION);
  Serial.println();
  display.init();
  display.clear();
}

void loop() {
  if (!client.connected()) {
    reconnect();
  }
  client.loop();
  keeper=keeper%2==0?1:keeper+1;
  displayData();
  long now = millis();
  if (now - lastMsg > 5000) {
    lastMsg = now;
    writeData();
    //Serial.println(msg);
    client.publish("info", msg);
  }
}

void writeData(){
  DynamicJsonDocument doc(1024);
  ++heartkeeper;
  DHT11.read(DHT11PIN);
  //获取温湿度
  double humidity=(double)DHT11.humidity;
  double temperature=(double)DHT11.temperature;
  //制造4字节随机码用于生成checksum
  String msgid=String(random(0xffffffff), HEX);
  //在串口显示随机码
  Serial.println(msgid);
  int len=msgid.length();char tmp[len+1];
  //数组化
  msgid.toCharArray(tmp,len+1);
  Serial.println(tmp);
  //异或取得checksum
  int checksum=createJsonChecksum(tmp);
  //Serial.println(checksum);
  doc["Flag"]="31701233";
  doc["_MsgId"]=msgid;
  //发送至info话题
  doc["Type"]="info";
  doc["HeartKeeper"]=heartkeeper;
  doc["Humidity"]=humidity;
  doc["Temperature"]=temperature;
  doc["Checksum"]=checksum;
  //serializeJson(doc,Serial);
  serializeJson(doc,msg);
  //以json形式发送数据
}
void displayData(){
  display.clear();
  display.drawString(0,0,"[receive]:");
  display.drawString(0,20,rsvm);
  display.drawString(0,40,"31701233-zzx");
  display.display();
  delay(1000);
}

int createJsonChecksum(const char* data){
  int len=strlen(data);
  int checksum=0x11;
  for(int i=0;i<len;i++){
    //异或校验
    //例如16516fa0
    //16^51的结果^6f，得到的结果在与a0异或得到最终的checksum
    //在安卓端接受并验证是否一致
    checksum=((checksum&(0xff))^(data[i]&(0xff)))&(0xff);
    //Serial.print(data[i]);
  }
  //Serial.println();
  return checksum;
}
