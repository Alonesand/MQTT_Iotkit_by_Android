package cn.aln.mqtt;


import android.app.AlertDialog;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.Toast;


public class Reg extends AppCompatActivity {

    private EditText account;
    private EditText pwd1;
    private EditText pwd2;
    private Button reg;

    private ItemDBHelper helper;
    private ItemDBHelper dbHelper;
    SQLiteDatabase sqLiteDatabase;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register);

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

        account = findViewById(R.id.reg_account);
        pwd1 = findViewById(R.id.reg_pwd1);
        pwd2 = findViewById(R.id.reg_pwd2);

        reg = findViewById(R.id.reg);
        helper = new ItemDBHelper(Reg.this, "user", null, 1);
        sqLiteDatabase = helper.getWritableDatabase();

        dbHelper = new ItemDBHelper(this,"user",null,1);

        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(account.getText().toString().equals("")||pwd1.getText().toString().equals("")||pwd2.getText().toString().equals("")){
                    inputWrong();
                }else if(!pwd1.getText().toString().equals(pwd2.getText().toString())){
                    diffpwd();
                }else{
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    String name = account.getText().toString();
                    Cursor cursor = db.query("user",new String[]{"user_id"},"user_id=?",new String[] {name},null,null,null,null);
                    if(cursor.getCount()!=0){
                        Toast.makeText(getApplicationContext(),"账号已被注册！", Toast.LENGTH_SHORT).show();
                    }else{
                        helper.addData(sqLiteDatabase, name,pwd1.getText().toString());
                        Toast.makeText(Reg.this, "注册成功,请登录!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Reg.this,Login.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });

    }

    public void jump_to_login(View v){
        Intent intent = new Intent(Reg.this, Login.class);
        startActivity(intent);
        finish();
    }

    private void inputWrong(){
        new AlertDialog.Builder(this)
                .setTitle("错误")
                .setMessage("请正确填写账号密码")
                .setPositiveButton("确定", null)
                .show();
    }
    private void diffpwd(){
        new AlertDialog.Builder(this)
                .setTitle("错误")
                .setMessage("两次密码不同")
                .setPositiveButton("确定", null)
                .show();
    }
}
