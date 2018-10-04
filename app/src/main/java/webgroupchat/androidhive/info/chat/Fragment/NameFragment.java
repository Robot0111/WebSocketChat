package webgroupchat.androidhive.info.chat.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import webgroupchat.androidhive.info.chat.Manaer.PrintfManager;
import webgroupchat.androidhive.info.chat.R;
import webgroupchat.androidhive.info.chat.WsConfig;
import webgroupchat.androidhive.info.chat.activity.MainActivity;
import webgroupchat.androidhive.info.chat.activity.PrintfBlueListActivity;


public class NameFragment extends Fragment {
    private static final String TAG = NameFragment.class.getSimpleName();
    private static final String NAME="webgroupchat.androidhive.info.chat.other.name";
    private Context context;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final PrintfManager printfManager = PrintfManager.getInstance(context);
        View v = inflater.inflate(R.layout.fragment_name,container,false);
        final Button  btnTeacher = v.findViewById(R.id.teacher_name),
              btnStudent = v.findViewById(R.id.student_name);
        final TextView tv_main_bluetooth = v.findViewById(R.id.tv_main_bluetooth);
        printfManager.defaultConnection();

        btnTeacher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),MainActivity.class);
                intent.putExtra(NAME, btnTeacher.getText());
                startActivity(intent);
            }
        });

        btnStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),MainActivity.class);
                intent.putExtra(NAME, btnStudent.getText());
                startActivity(intent);
            }
        });
            printfManager.addBluetoothChangLister(new PrintfManager.BluetoothChangLister() {
                @Override
                public void chang(String name, String address) {
                    tv_main_bluetooth.setText(name);
                    Log.i(TAG,"chang() start");
                }
            });
            tv_main_bluetooth.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(),PrintfBlueListActivity.class);
                    startActivity(intent);
                }
            });
        return v;

    }
}
