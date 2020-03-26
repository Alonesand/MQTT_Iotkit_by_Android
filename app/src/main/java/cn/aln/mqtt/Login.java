package cn.aln.mqtt;


import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends AppCompatActivity {

    private EditText account;
    private EditText edt_pwd1;
    private Button login;
    private CheckBox rem;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private ItemDBHelper dbHelper;

    private AlertDialog.Builder mybuild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

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

        account = findViewById(R.id.login_account);
        edt_pwd1 = findViewById(R.id.login_pwd);
        rem = findViewById(R.id.jizhu);

        sp = getSharedPreferences("user_mes", MODE_PRIVATE);
        editor = sp.edit();

        login = findViewById(R.id.login);

        if (sp.getBoolean("flag", false)) {
            account.setText(sp.getString("user_id", ""));
            edt_pwd1.setText(sp.getString("pwd", ""));
            rem.setChecked(sp.getBoolean("isChecked",false));
        }

        //数据库创建
        dbHelper = new ItemDBHelper(this,"user",null,1);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //检错
                if(account.getText().toString().equals("") || edt_pwd1.getText().toString().equals("")){
                    showExitDialog01();
                }else{
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    String name = account.getText().toString();
                    String pwd = edt_pwd1.getText().toString();
                    Cursor cursor = db.query("user",new String[]{"pwd"},"user_id=?",new String[] {name},null,null,null,null);
                    if(cursor.moveToNext()){
                        if(pwd.equals(cursor.getString(cursor.getColumnIndex("pwd")))){
                            if(rem.isChecked()){
                                editor.putBoolean("flag",true);
                                editor.putString("user_id",name);
                                editor.putString("pwd",pwd);
                                editor.putBoolean("isChecked",true);
                                editor.commit();
                            }else{
                                editor.clear();
                                editor.commit();
                            }
                            Toast.makeText(getApplicationContext(),"登陆成功！", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(Login.this,IpHostActivity.class);
                            startActivity(intent);
                            finish();
                            //弹出主页音乐显示页。
                        }else{
                            Toast.makeText(getApplicationContext(),"账号或密码错误！", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(),"账户不存在！", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
    }

    private void showExitDialog01(){
        new AlertDialog.Builder(this)
                .setTitle("错误")
                .setMessage("请正确填写账号密码")
                .setPositiveButton("确定", null)
                .show();
    }

    public void jump_to_reg(View v){
        Intent intent = new Intent(Login.this, Reg.class);
        startActivity(intent);
        finish();
    }
}