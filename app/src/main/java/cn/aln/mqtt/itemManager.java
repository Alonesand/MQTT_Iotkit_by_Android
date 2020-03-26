package cn.aln.mqtt;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class itemManager extends AppCompatActivity implements View.OnClickListener{
    private AlertDialog.Builder builder;
    private ListView item_list;
    private Button newIotdevice;

    private deviceDBHelper helper;
    private deviceDBHelper dbHelper;
    private deviceDBHelper dbHelper2;
    SQLiteDatabase sqLiteDatabase;

    private String flag;
    private String psd;
    private String name;
    private String sub;
    private String ip;
    private String port;
    private List<IoTdevice> list=new ArrayList<IoTdevice>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_item);

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

        psd = getIntent().getStringExtra("psd");
        name = getIntent().getStringExtra("name");
        sub = getIntent().getStringExtra("sub");
        ip = getIntent().getStringExtra("ip");
        port = getIntent().getStringExtra("port");

        newIotdevice=(Button) findViewById(R.id.newIotdevice);
        item_list=(ListView) findViewById(R.id.device);

        newIotdevice.setOnClickListener(this);

        helper = new deviceDBHelper(itemManager.this, "item", null, 1);
        sqLiteDatabase = helper.getWritableDatabase();

        dbHelper = new deviceDBHelper(itemManager.this,"item",null,1);
        dbHelper2 = new deviceDBHelper(itemManager.this,"item",null,1);

        SQLiteDatabase db2 = dbHelper.getReadableDatabase();
        Cursor cursor = db2.query("item",null,null,null,null,null,null,null);

        if(cursor.moveToFirst()){
            do{
                String ff = cursor.getString(cursor.getColumnIndex("flag"));
                IoTdevice a = new IoTdevice(ff);
                list.add(a);

            }while (cursor.moveToNext());
        }

        DeviceAdapter da=new DeviceAdapter(list,this);
        item_list.setAdapter(da);

        item_list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(itemManager.this,""+list.get(position).getName(),Toast.LENGTH_LONG).show();
                Intent intent = new Intent(itemManager.this,MainActivity.class);
//                    传递参数给MainActivity
                intent.putExtra("ip",ip);
                intent.putExtra("port",port);
                intent.putExtra("sub",sub);
                intent.putExtra("name",name);
                intent.putExtra("psd",psd);
                intent.putExtra("item_name",list.get(position).getName());
                startActivity(intent);
            }
        });
    }
    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.newIotdevice:{
                final EditText editText=new EditText(this);
                builder=new AlertDialog.Builder(this).setTitle("添加新的IoT设备").setView(editText)
                        .setPositiveButton("完成", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {


                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                String name = editText.getText().toString();
                                Cursor cursor = db.query("item",new String[]{"flag"},"flag=?",new String[] {name},null,null,null,null);
                                if(cursor.getCount()!=0){
                                    Toast.makeText(getApplicationContext(),"身份已存在", Toast.LENGTH_SHORT).show();
                                }else{
                                    helper.addData(sqLiteDatabase, name);
                                    IoTdevice newdevice=new IoTdevice(editText.getText().toString());
                                    list.add(newdevice);
                                    Toast.makeText(itemManager.this,"创建成功",Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                builder.create().show();
                break;
            }
        }
    }

    private void inputWrong(){
        new AlertDialog.Builder(this)
                .setTitle("错误")
                .setMessage("已存在该ID")
                .setPositiveButton("确定", null)
                .show();
    }

}
