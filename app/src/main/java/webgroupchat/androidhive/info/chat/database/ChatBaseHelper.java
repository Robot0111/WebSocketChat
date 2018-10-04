package webgroupchat.androidhive.info.chat.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import webgroupchat.androidhive.info.chat.database.ChatDbSchema.ChatTable;

public class ChatBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "chatBase.db";
    public ChatBaseHelper(Context context) {
        super(context, DATABASE_NAME,null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+ ChatTable.NAME + "(" +
                " _id integer primary key autoincrement, "+
                ChatTable.Cols.CONTT + ", " +
                ChatTable.Cols.DATE + ", " +
                ChatTable.Cols.FROMENAME + ", " +
                ChatTable.Cols.SOLVED  + ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
