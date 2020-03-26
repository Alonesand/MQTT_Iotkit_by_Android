package cn.aln.mqtt;


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class deviceDBHelper extends SQLiteOpenHelper {
    public static final String CREATE_MESSAGE = "create table item ("
            +"id integer primary key autoincrement, "
            +"flag String)";
    private Context mContext;

    public deviceDBHelper(Context context, String item_flag, SQLiteDatabase.CursorFactory factory, int version){
        super(context,item_flag,factory,version);
        mContext = context;
    }

    public void addData(SQLiteDatabase sqLiteDatabase,String item_flag){
        ContentValues values = new ContentValues();
        values.put("flag", item_flag);
        sqLiteDatabase.insert("item", null, values);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_MESSAGE);
//        Toast.makeText(mContext,"Create succeeded",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){

    }
}
