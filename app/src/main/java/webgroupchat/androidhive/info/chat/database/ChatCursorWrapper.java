package webgroupchat.androidhive.info.chat.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.Date;

import webgroupchat.androidhive.info.chat.Model.Mode;
import webgroupchat.androidhive.info.chat.database.ChatDbSchema.ChatTable;

public class ChatCursorWrapper extends CursorWrapper {
    public ChatCursorWrapper(Cursor cursor) {
        super(cursor);
    }
    public Mode getMode(){
        String contt = getString(getColumnIndex(ChatTable.Cols.CONTT));
        long date = getLong(getColumnIndex(ChatTable.Cols.DATE));
        String fromename = getString(getColumnIndex(ChatTable.Cols.FROMENAME));
        int solved = getInt(getColumnIndex(ChatTable.Cols.SOLVED));


        return  new Mode(fromename,contt,solved,new Date(date));
    }
}
