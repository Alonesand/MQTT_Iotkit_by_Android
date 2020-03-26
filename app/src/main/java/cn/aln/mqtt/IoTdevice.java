package cn.aln.mqtt;

public class IoTdevice {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public IoTdevice(String name){
        this.name=name;
    }
}
