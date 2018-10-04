package webgroupchat.androidhive.info.chat.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import webgroupchat.androidhive.info.chat.Model.Mode;
import webgroupchat.androidhive.info.chat.database.ChatDbSchema.ChatTable;

public class ModeLab {
    private static final String TAG = ModeLab.class.getSimpleName();
    private static ModeLab sModeLab;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static ModeLab get(Context context){
        if(sModeLab == null){
            sModeLab = new ModeLab(context);
        }
        return sModeLab;
    }
    public void addMode(Mode c){
        ContentValues values = getContentValues(c);
        mDatabase.insert(ChatTable.NAME,null,values);
        //mCrimes.add(c);

    }
    public List<Mode> getCrimes(){
        List<Mode> crimes = new ArrayList<>();
        ChatCursorWrapper cursor = queryMode(null,null);
        try{
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                crimes.add(cursor.getMode());
                cursor.moveToNext();
            }
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
        }finally {
            cursor.close();
        }
        return crimes;
    }
    private ModeLab(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new ChatBaseHelper(mContext).getWritableDatabase();
    }
    private static ContentValues getContentValues(Mode crime){
        ContentValues values = new ContentValues();
        values.put(ChatTable.Cols.CONTT,crime.getMessage());
        values.put(ChatTable.Cols.DATE,crime.getDate().getTime());
        values.put(ChatTable.Cols.SOLVED,crime.isSelf());
        values.put(ChatTable.Cols.FROMENAME,crime.getFromName());
        return values;
    }
    private ChatCursorWrapper queryMode(String whereClause, String[] whereArgs){
        Cursor cursor = mDatabase.query(ChatTable.NAME,null,whereClause,whereArgs,null,null,null);
        return new ChatCursorWrapper(cursor);
    }
}
