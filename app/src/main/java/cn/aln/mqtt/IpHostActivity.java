package cn.aln.mqtt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class IpHostActivity extends AppCompatActivity {

    private EditText ip;
    private EditText port;
    private EditText sub ,name,psd;

    private CheckBox cb;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_ip_host);
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

            cb = findViewById(R.id.iprem);

            sp = getSharedPreferences("info", MODE_PRIVATE);
            editor = sp.edit();

            ip = findViewById(R.id.ip);
            port = findViewById(R.id.port);
            sub = findViewById(R.id.sub);
            name = findViewById(R.id.name);
            psd = findViewById(R.id.psd);

            if (sp.getBoolean("flag", false)) {
                ip.setText(sp.getString("ip", ""));
                port.setText(sp.getString("port", ""));
                sub.setText(sp.getString("sub", ""));
                name.setText(sp.getString("name", ""));
                psd.setText(sp.getString("psd", ""));
                cb.setChecked(sp.getBoolean("checked",false));
            }

        }

        Button connect = findViewById(R.id.connect);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断是否填写
                if (ip.getText().toString().length() !=0 && port.getText().toString().length()!=0 &&
                        sub.getText().toString().length()!=0&&name.getText().toString().length()!=0&&
                        psd.getText().toString().length()!=0){

                    if(cb.isChecked()){
                        editor.putBoolean("flag",true);
                        editor.putString("ip",ip.getText().toString());
                        editor.putString("port",port.getText().toString());
                        editor.putString("sub",sub.getText().toString());
                        editor.putString("name",name.getText().toString());
                        editor.putString("psd",psd.getText().toString());
                        editor.putBoolean("checked",cb.isChecked());
                        editor.commit();
                    }

                    Intent intent = new Intent(IpHostActivity.this,itemManager.class);
//                    传递参数给itemManager
                    intent.putExtra("ip",ip.getText().toString());
                    intent.putExtra("port",port.getText().toString());
                    intent.putExtra("sub",sub.getText().toString());
                    intent.putExtra("name",name.getText().toString());
                    intent.putExtra("psd",psd.getText().toString());
                    startActivity(intent);
                }
            }
        });

    }
}
