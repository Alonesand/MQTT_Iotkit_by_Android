package cn.aln.mqtt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;


public class DeviceAdapter extends BaseAdapter {
    private List<IoTdevice> mIoTdeviceList;
    private Context context;
    ViewHolder holder;

    public DeviceAdapter(List<IoTdevice> list, Context context){
        mIoTdeviceList =list;
        this.context=context;
    }
    public int getCount(){
        return mIoTdeviceList.size();
    }
    public Object getItem(int position){
        return mIoTdeviceList.get(position);
    }
    public long getItemId(int position){
        return position;
    }
    public View getView(int position, View convertView, ViewGroup parent) {
//        if (convertView == null) {
            convertView=LayoutInflater.from(context).inflate(R.layout.iotdevice_item, parent, false);
            holder=new ViewHolder();
            holder.name=((TextView)convertView.findViewById(R.id.nameValue));
            holder.orderId=((TextView)convertView.findViewById(R.id.orderId));
//        }else{
//            holder=(ViewHolder)convertView.getTag();
//        }
        holder.name.setText(mIoTdeviceList.get(position).getName());
        holder.orderId.setText(""+(position+1));
        return convertView;
    }
    static class ViewHolder{
        TextView name;
        TextView orderId;
    }

}