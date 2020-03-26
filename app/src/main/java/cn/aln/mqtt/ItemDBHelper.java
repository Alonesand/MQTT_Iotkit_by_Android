package cn.aln.mqtt;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ItemDBHelper extends SQLiteOpenHelper {
    public static final String CREATE_MESSAGE = "create table user ("
            +"id integer primary key autoincrement, "
            +"user_id String, "
            +"pwd String)";
    private Context mContext;

    public ItemDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context,name,factory,version);
        mContext = context;
    }

    public void addData(SQLiteDatabase sqLiteDatabase, String userid, String password){
        ContentValues values = new ContentValues();
        values.put("user_id", userid);
        values.put("pwd", password);
        sqLiteDatabase.insert("user", null, values);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_MESSAGE);
//        Toast.makeText(mContext,"Create succeeded",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }
}
