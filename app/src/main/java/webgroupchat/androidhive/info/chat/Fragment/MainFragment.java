package webgroupchat.androidhive.info.chat.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import webgroupchat.androidhive.info.chat.Manaer.PrintfManager;
import webgroupchat.androidhive.info.chat.Manaer.TopLayoutManager;
import webgroupchat.androidhive.info.chat.Model.Mode;
import webgroupchat.androidhive.info.chat.R;
import webgroupchat.androidhive.info.chat.Utils.Utils;
import webgroupchat.androidhive.info.chat.View.KeyboardLayout;
import webgroupchat.androidhive.info.chat.WsConfig;
import webgroupchat.androidhive.info.chat.database.ModeLab;

public class MainFragment extends Fragment {
    /**
     * 播放默认的通知声音
     */
/*    public void playBeep() {

        try {
            Uri notification = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getActivity(),
                    notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static final String ARG_NAME = "name";
    // LogCat tag
    private static final String TAG = MainFragment.class.getSimpleName();
    // JSON flags to identify the kind of JSON response
    private static final String TAG_SELF = "self", TAG_NEW = "new", TAG_MESSAGE = "message", TAG_EXIT = "exit";
    private static final int VIEW_TYPE_LEFT = 1;
    private static final int VIEW_TYPE_RIGHT = 2;
    public long LASTIME = -1;
    final PrintfManager printfManager = PrintfManager.getInstance(getActivity());
    private List<Mode> mListModes = new ArrayList<>();
    private Utils utils;
    private RecyclerView mChatRecyclerView;
    private ChatAdapter mAdapter;
    private EditText inputMsg;
    private WebSocket mWebSocket;
    @SuppressLint("SimpleDateFormat")
    private DateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //private DateFormat f = SimpleDateFormat.getDateTimeInstance()
    // Client name
    @Nullable
    private String name = "";

    //设置Fragment args
    public static MainFragment newInstance(String name) {
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }

        return new String(hexChars);
    }

    /**
     * 发送消息
     */
    private void sendMessageToServer(String message) {
        if (mWebSocket != null && mWebSocket.isOpen()) {
            mWebSocket.send(message);
        } else {
            Log.e(TAG, "message is not be send. mWebSocket is null or not open !");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        f.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final KeyboardLayout view = (KeyboardLayout) inflater.inflate(R.layout.fragment_main, container, false);
        mChatRecyclerView = view.findViewById(R.id.chat_list_messages);
        Button btnSend = view.findViewById(R.id.btnSend);
        inputMsg = view.findViewById(R.id.inputMsg);
        utils = new Utils(getActivity());
        view.setOnkbdStateListener(new KeyboardLayout.onKeyboaddsChangeListener() {

            public void onKeyBoardStateChange(int state) {
                switch (state) {
                    case KeyboardLayout.KEYBOARD_STATE_HIDE:
                        //软键盘隐藏
                        mChatRecyclerView.smoothScrollToPosition(mAdapter.getItemCount());
                        break;
                    //软键盘弹起
                    case KeyboardLayout.KEYBOARD_STATE_SHOW:
                        mChatRecyclerView.smoothScrollToPosition(mAdapter.getItemCount());
                        break;
                }
            }
        });
        // 从上一个屏幕获取姓名
        if (getArguments() != null) {
            name = getArguments().getString(ARG_NAME);
        } else {
            Log.e(TAG, "not found name property");
        }
        //加载历史消息
        mListModes = ModeLab.get(getActivity()).getCrimes();
        try {
            AsyncHttpClient.getDefaultInstance().websocket(WsConfig.URL_WEBSOCKET + (URLEncoder.encode(name, "UTF-8")), "chat",
                    new AsyncHttpClient.WebSocketConnectCallback() {
                        @Override
                        public void onCompleted(Exception ex, WebSocket webSocket) {
                            if (ex != null) {
                                ex.printStackTrace();
                                return;
                            }
                            MainFragment.this.mWebSocket = webSocket;
                            webSocket.setStringCallback(new WebSocket.StringCallback() {
                                @Override
                                public void onStringAvailable(String message) {
                                    Log.d(TAG, String.format("Got string message! %s", message));
                                    parseMessage(message);
                                }
                            });
                            webSocket.setDataCallback(new DataCallback() {
                                @Override
                                public void onDataAvailable(DataEmitter emitter, ByteBufferList data) {
                                    // Mode will be in JSON format
                                    parseMessage(bytesToHex(data.getAllByteArray()));
                                }
                            });

                        }
                    });
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage());
        }

        //设置下滑隐藏软键盘
        mChatRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy < -10 && getActivity() != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(inputMsg.getWindowToken(), 0);
                    }
                }
            }
        });


        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sending message to web socket server
                String mes = inputMsg.getText().toString();
                if (!"".equals(mes)) {
                    sendMessageToServer(utils.getSendMessageJSON(mes));
                    // Clearing the input filed once message was sent
                    inputMsg.setText("");
                }else{
                    showToast("请输入内容");
                }
            }
        });
        LinearLayoutManager layout = new TopLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        //设置聊天从底部发出
        // layout.setStackFromEnd(true);
        //layout.setReverseLayout(true);
        mChatRecyclerView.setLayoutManager(layout);

        mAdapter = new ChatAdapter(mListModes);
        mChatRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        if (mWebSocket != null && mWebSocket.isOpen())
            mWebSocket.getSocket().end();
        super.onDestroyView();
    }

    //加载历史纪录
    private void initHistory(String initmessage) {

        try {
            JSONArray jsonArray = new JSONArray(initmessage);
            if (mListModes.size() < 1) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    boolean n = jsonObject.getString("FROMENAME").equals(name);
                    Date date = f.parse(jsonObject.getString("DATE"));
                    appendMessage(new Mode(jsonObject.getString("FROMENAME"), jsonObject.getString("MESSAGE"), n ? 2 : 1, date));
                }

            } else {
                Mode mode = mListModes.get(mListModes.size() - 1);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    //获取android设备数据库Mode
                    boolean n = jsonObject.getString("FROMENAME").equals(name);
                    Date servertime = f.parse(jsonObject.getString("DATE"));
                    boolean m = jsonObject.getString("MESSAGE").equals(mode.getMessage());
                    Date localtime = mode.getDate();
                    //判断服务器信息和android本地信息
                    if (localtime.before(servertime) && !n && !m) {
                        appendMessage(new Mode(jsonObject.getString("FROMENAME"), jsonObject.getString("MESSAGE"),
                                1, servertime));
                        // Log.i(TAG, );

                    }
                }
            }
        } catch (JSONException | ParseException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * 解析从服务端收到的json 消息的目的由flag字段所指定，flag=self，消息属于指定的人，
     * new：新人加入   *    到对话中，message：新的消息，exit：退出
     */
    private void parseMessage(final String msg) {
        try {

            JSONObject jObj = new JSONObject(msg);
            String flag = jObj.getString("flag");
            if (jObj.has("name")) {
                boolean teacher = jObj.getString("name").equals("老师");

                try {
                    if (!printfManager.isConnect() && !teacher && "学生".equals(name))
                        showToast("蓝牙没有连接");
                    else if (!flag.equalsIgnoreCase(TAG_EXIT) && !flag.equals("new") && teacher && printfManager.isConnect()) {
                        //将老师发送过来的信息转换成GBK byte[]
                        final byte[] b = jObj.getString("message").getBytes("GBK");

                        final byte[] n = "\n".getBytes("GBK");
                        Log.i(TAG, String.valueOf(b.length));
                        //打印
                        printfManager.printf_answer(b, n);
                    }
                } catch (JSONException | UnsupportedEncodingException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
            // JSON node 'flag'


            // 如果是self，json中包含sessionId信息
            if (flag.equalsIgnoreCase(TAG_SELF)) {

                String sessionId = jObj.getString("sessionId");

                // Save the session id in shared preferences
                utils.storeSessionId(sessionId);
                initHistory(jObj.getString("initMessage"));

                Log.e(TAG, "Your session id: " + utils.getSessionId());

            } else if (flag.equalsIgnoreCase(TAG_NEW)) {
                // If the flag is 'new', new person joined the room
                String name = jObj.getString(ARG_NAME), message = jObj.getString("message"), onlineCount = jObj.getString("onlineCount");
                showToast(name + message + ". 当前 " + onlineCount + " 人在线!");

            } else if (flag.equalsIgnoreCase(TAG_MESSAGE)) {
                // if the flag is 'message', new message received
                String fromName = name;
                String message = jObj.getString("message");
                String date = jObj.getString("date");
                int isSelf = VIEW_TYPE_RIGHT;

                // Checking if the message was sent by you
                if (!jObj.getString("sessionId").equals(utils.getSessionId())) {
                    fromName = jObj.getString(ARG_NAME);

                    isSelf = VIEW_TYPE_LEFT;

                }

                try {
                    Mode m = new Mode(fromName, message, isSelf, f.parse(date));
                    // 把消息加入到arraylist中并且插入数据库。
                    appendMessage(m);
                    mChatRecyclerView.smoothScrollToPosition(mAdapter.getItemCount());
                } catch (ParseException e) {
                    Log.e(TAG, e.getMessage());
                }


            } else if (flag.equalsIgnoreCase(TAG_EXIT)) {
                // If the flag is 'exit', somebody left the conversation

                String name = jObj.getString(ARG_NAME);
                if ("学生".equals(name) && printfManager.isCONNECTING()) {
                    printfManager.disConnect("学生已退出了聊天室并且关闭了蓝牙");
                }
                String message = jObj.getString("message");

                showToast(name + message);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void appendMessage(final Mode m) {
        if (getActivity() != null) getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 把消息加入到数据库中
                ModeLab.get(getActivity()).addMode(m);
                mListModes.add(m);

                mAdapter.notifyDataSetChanged();

                // Playing device's notification
                // playBeep();
            }
        });
    }

    private void showToast(final String message) {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), message,
                            Toast.LENGTH_LONG).show();
                }
            });

    }

    private class ChatLeftHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        private ImageView lblFrom;
        private TextView txtMsg, tv_date;


        private Mode mMode;

        private ChatLeftHolder(View itemView) {
            super(itemView);
            lblFrom = itemView.findViewById(R.id.chat_item_avatar);
            txtMsg = itemView.findViewById(R.id.chat_item_content_text);
            tv_date = itemView.findViewById(R.id.chat_item_date);
            txtMsg.setOnLongClickListener(this);
        }

        public void bind(Mode mode) {
            mMode = mode;
            String n = null;
            if (mode != null) {
                n = mode.getFromName();
                long currie = mode.getDate().getTime();
                if (LASTIME == -1) {
                    LASTIME = currie;
                }
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));

                /* long m = calendar.get(Calendar.MINUTE);*/

                long lastime = new Date(LASTIME).getTime();
                if (currie - lastime > 0) {
                    calendar.setTime(new Date(currie - lastime));
                    int YEAR = calendar.get(Calendar.YEAR), MONTH = calendar.get(Calendar.MONTH) + 1, DAY = calendar.get(Calendar.DAY_OF_MONTH), HOUR = calendar.get(Calendar.HOUR), MINUTE = calendar.get(Calendar.MINUTE);
                    if (YEAR > 1970 || MONTH > 1 || DAY > 1 || HOUR > 8 || MINUTE > 8) {
                        tv_date.setText(f.format(currie));
                        tv_date.setVisibility(View.VISIBLE);
                    }
                }
                LASTIME = currie;
            }

            if (n != null && n.equals("老师")) {
                lblFrom.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
            } else {
                lblFrom.setImageDrawable(getResources().getDrawable(R.mipmap.luql));
            }
            txtMsg.setText(mMode.getMessage());
        }

        @Override
        public boolean onLongClick(View v) {
            if (name !=null&&"学生".equals(name)&&printfManager.isConnect()) {
                //将老师发送过来的信息转换成GBK byte[]
                try {
                    final byte[] b = mMode.getMessage().getBytes("GBK");
                    final byte[] n = "\n".getBytes("GBK");
                    Log.i(TAG, String.valueOf(b.length));
                    //打印
                    printfManager.printf_answer(b, n);
                } catch (UnsupportedEncodingException e) {
                    showToast(e.getMessage());
                }

            }else {
                showToast("您不是学生或者蓝牙没有连接");
            }
            return false;
        }
    }

    private class ChatRightHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        private ImageView lblFrom;
        private TextView txtMsg, tv_date;

        private Mode mMode;

        private ChatRightHolder(View itemView) {
            super(itemView);
            lblFrom = itemView.findViewById(R.id.chat_item_avatar);
            txtMsg = itemView.findViewById(R.id.chat_item_content_text);
            tv_date = itemView.findViewById(R.id.chat_item_date);
            txtMsg.setOnLongClickListener(this);
        }

        public void bind(Mode mode) {
            this.mMode = mode;
            String m = null;
            //设置时间
            if (mode != null) {
                m = mode.getFromName();
                long currie = mode.getDate().getTime();
                if (LASTIME == -1) {
                    LASTIME = currie;
                }
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
                long lastime = new Date(LASTIME).getTime();
                if (currie - lastime > 0) {
                    calendar.setTime(new Date(currie - lastime));
                    int YEAR = calendar.get(Calendar.YEAR), MONTH = calendar.get(Calendar.MONTH) + 1, DAY = calendar.get(Calendar.DAY_OF_MONTH), HOUR = calendar.get(Calendar.HOUR), MINUTE = calendar.get(Calendar.MINUTE);
                    if (YEAR > 1970 || MONTH > 1 || DAY > 1 || HOUR > 8 || MINUTE > 8) {
                        tv_date.setText(f.format(currie));
                        tv_date.setVisibility(View.VISIBLE);
                    }
                }
                LASTIME = currie;
            }

            //添加头像
            if (m != null && m.equals("老师")) {
                lblFrom.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
            } else {
                lblFrom.setImageDrawable(getResources().getDrawable(R.mipmap.luql));
            }
            txtMsg.setText(mMode.getMessage());

        }

        @Override
        public boolean onLongClick(View v) {
            if (name != null && "学生".equals(name)&&printfManager.isConnect()) {
                //将老师发送过来的信息转换成GBK byte[]
                try {
                    final byte[] b = mMode.getMessage().getBytes("GBK");
                    final byte[] n = "\n".getBytes("GBK");
                    Log.i(TAG, String.valueOf(b.length));
                    //打印
                    printfManager.printf_answer(b, n);
                } catch (UnsupportedEncodingException e) {
                    showToast(e.getMessage());
                }
            }else {
                showToast("您不是学生或者蓝牙没有连接");
            }
            return false;
        }
    }

    private class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<Mode> mModes;


        private ChatAdapter(List<Mode> modes) {
            mModes = modes;
        }

        @Override
        public int getItemCount() {
            return mModes.size();
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case VIEW_TYPE_LEFT:
                    ChatLeftHolder leftHolder = (ChatLeftHolder) holder;
                    Mode mode = mModes.get(position);
                    leftHolder.bind(mode);
                    break;
                case VIEW_TYPE_RIGHT:
                    ChatRightHolder righttHolder = (ChatRightHolder) holder;
                    Mode rightMode = mModes.get(position);
                    righttHolder.bind(rightMode);
                    break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            return mModes.get(position).isSelf();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            RecyclerView.ViewHolder holder;
            switch (viewType) {
                case VIEW_TYPE_LEFT:
                    holder = new ChatLeftHolder(layoutInflater.inflate(R.layout.chat_item_list_left, parent, false));
                    break;
                case VIEW_TYPE_RIGHT:
                    holder = new ChatRightHolder(layoutInflater.inflate(R.layout.chat_item_list_right, parent, false));
                    break;
                default:
                    throw new NullPointerException();
            }
            return holder;
        }
    }

}
