package webgroupchat.androidhive.info.chat.Fragment;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import webgroupchat.androidhive.info.chat.Manaer.PopupWindowManager;
import webgroupchat.androidhive.info.chat.Manaer.PrintfManager;
import webgroupchat.androidhive.info.chat.Manaer.SharedPreferencesManager;
import webgroupchat.androidhive.info.chat.MyApplication;
import webgroupchat.androidhive.info.chat.R;
import webgroupchat.androidhive.info.chat.Utils.PermissionUtil;
import webgroupchat.androidhive.info.chat.Utils.Utils;

public class PrintBlueFragment extends Fragment {

    private static final String TAG = NameFragment.class.getSimpleName();

    private BluetoothAdapter mBluetoothAdapter = null;

    private AbstractList<BluetoothDevice> bluetoothDeviceArrayList;

    private Context context;

    private View root;

    private RecyclerView lv_blue_list,lv_already_blue_list;

    private TextView tv_blue_list_back, tv_blue_list_operation;

    private TextView tv_blue_list_modify,tv_blue_list_name,tv_blue_list_address;

    private LinearLayout ll_blue_list_already_paired,ll_blue_list_unpaired;

    private ImageView iv_blue_list_already_paired,iv_blue_list_unpaired;
    //是否打开蓝牙
    private boolean ALREADY_PAIRED_IS_OPEN,UNPAIRED_IS_OPEN,isRegister;

    private PrintfManager printfManager;

    private static final int REQUEST_ENABLE_BT = 2;

    private Adapter mAdapter;

    private List<BluetoothDevice> alreadyBlueList;

    private PrintfManager.BluetoothChangLister bluetoothChangLister;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if(getContext()!=null){context = getActivity();}
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_fragment_printf_blue,container,false);
        root = v.findViewById(R.id.root);
        ll_blue_list_already_paired = v.findViewById(R.id.ll_blue_list_already_paired);
        ll_blue_list_unpaired = v.findViewById(R.id.ll_blue_list_unpaired);
        //附近的蓝牙 ，已配对的蓝牙view
        lv_blue_list =  v.findViewById(R.id.lv_blue_list);
        lv_already_blue_list = v.findViewById(R.id.lv_already_blue_list);
        //返回，搜索，修改
        tv_blue_list_back = v.findViewById(R.id.tv_blue_list_back);
        tv_blue_list_operation =  v.findViewById(R.id.tv_blue_list_operation);
        tv_blue_list_modify = v.findViewById(R.id.tv_blue_list_modify);
        //名称，地址 ，，
        tv_blue_list_name =  v.findViewById(R.id.tv_blue_list_name);
        tv_blue_list_address = v.findViewById(R.id.tv_blue_list_address);
        iv_blue_list_already_paired = v.findViewById(R.id.iv_blue_list_already_paired);
        iv_blue_list_unpaired = v.findViewById(R.id.iv_blue_list_unpaired);
        lv_blue_list .setLayoutManager(new LinearLayoutManager(getActivity()));
        lv_already_blue_list.setLayoutManager(new LinearLayoutManager(getActivity()));
        initData();
        setLister();
        if(PermissionUtil.checkLocationPermission(context)){
            if(!printfManager.isConnect()){
                starSearchBlue();
            }
        }
        return v;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PermissionUtil.MY_PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(!printfManager.isConnect()){
                        starSearchBlue();
                    }
                } else {
                    //权限被拒绝
                    new AlertDialog.Builder(context).setMessage(getString(R.string.permissions_are_rejected_bluetooth))
                            .setPositiveButton(getString(R.string.to_set_up), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = Utils.getAppDetailSettingIntent(context);
                                    startActivity(intent);
                                    dialog.dismiss();
                                }
                            }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).setTitle(getString(R.string.prompt)).show();
                    break;
                }
        }
    }
    private void starSearchBlue() {
        tv_blue_list_operation.setText(getString(R.string.printf_blue_list_stop));
        Utils.ToastText(context,getString(R.string.start_search));
        bluetoothDeviceArrayList.clear();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
         registerReceiver(mReceiver, filter);
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            mBluetoothAdapter.startDiscovery();
        }
    }

    private void setLister() {

        printfManager.setConnectSuccess(new PrintfManager.ConnectSuccess() {
            @Override
            public void success() {
                if(getActivity()!=null) {
                    getActivity().finish();
                }
            }
        });

        tv_blue_list_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!printfManager.isConnect()){
                    Utils.ToastText(context,getString(R.string.please_connect_bluetooth));
                    return;
                }

                final PopupWindowManager popupWindowManager = PopupWindowManager.getInstance(context);
                popupWindowManager.showPopupWindow(getString(R.string.bluetooth_name),
                        getString(R.string.please_input_bluetooth_name),getString(R.string.bluetooth_name),root);
                popupWindowManager.changOrdinaryInputType();
                popupWindowManager.setPopCallback(new PopupWindowManager.PopCallback() {
                    @Override
                    public void callBack(String data) {
                        printfManager.changBlueName(data);
                    }
                });
            }
        });


        ll_blue_list_already_paired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ALREADY_PAIRED_IS_OPEN) {
                    ALREADY_PAIRED_IS_OPEN = false;
                    closeRotate(iv_blue_list_already_paired);
                    lv_already_blue_list.setVisibility(View.GONE);
                } else {
                    openAlreadyPaired();
                }
            }
        });

        ll_blue_list_unpaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UNPAIRED_IS_OPEN) {
                    UNPAIRED_IS_OPEN = false;
                    closeRotate(iv_blue_list_unpaired);
                    lv_blue_list.setVisibility(View.GONE);
                } else {
                    openUnpaired();
                }
            }
        });


        iv_blue_list_unpaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        tv_blue_list_operation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = tv_blue_list_operation.getText().toString();
                String stopText = getString(R.string.printf_blue_list_stop);
                String searchText = getString(R.string.printf_blue_list_search);
                if (text.equals(searchText)) {//点了搜索
                     starSearchBlue();
                } else if (text.equals(stopText)) {//点击了停止
                    stopSearchBlue();
                }
            }
        });

        tv_blue_list_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity()!=null) {
                    getActivity().finish();
                }
            }
        });

        PrintfManager.getInstance(context).addBluetoothChangLister(bluetoothChangLister);
    }
    private void openUnpaired() {
        UNPAIRED_IS_OPEN = true;
        openRotate(iv_blue_list_unpaired);
        lv_blue_list.setVisibility(View.VISIBLE);
    }

    private void openAlreadyPaired() {
        ALREADY_PAIRED_IS_OPEN = true;
        openRotate(iv_blue_list_already_paired);
        lv_already_blue_list.setVisibility(View.VISIBLE);
    }

    private void closeRotate(View v) {
        Utils.rotate(v, 90f, 0f);
    }

    private void openRotate(View v) {
        Utils.rotate(v, 0f, 90f);
    }
    @Override
    public void onDestroy() {
        stopSearchBlue();
        printfManager.removeBluetoothChangLister(bluetoothChangLister);
        super.onDestroy();
    }

    //蓝牙名称地址holder
    private class Holder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tv_blue_list_name,tv_blue_list_address;

       // private int viewType,position;
        private Holder(View itemView) {
            super(itemView);
            tv_blue_list_name = itemView.findViewById(R.id.tv_blue_list_name);
            tv_blue_list_address = itemView.findViewById(R.id.tv_blue_list_address);
        }
        public void bind(BluetoothDevice device){
            //this.position = position;
            //this.viewType = viewType;
            tv_blue_list_name.setText(device.getName());
            tv_blue_list_address.setText(device.getAddress());

        }
        @Override
        public void onClick(View v) {

            //Toast.makeText(getActivity(),position,viewType).show();

        }

    }
    private class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<BluetoothDevice> deviceList;
        private Context context;
        private int args;
        private Adapter(Context context, List<BluetoothDevice> deviceList,int arg){
            this.args = arg;
            this.context = context;
            this.deviceList = deviceList;
        }
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            RecyclerView.ViewHolder holder;
            holder = new Holder(layoutInflater.inflate(R.layout.blue_list_item,parent,false));
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int p) {
            Holder h = (Holder)holder;h.bind(deviceList.get(p));
            h.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (args){
                        case 1:
                            MyApplication.getInstance().getCachedThreadPool().execute(new Runnable() {
                                //int position = holder.getLayoutPosition();
                                @Override
                                public void run() {
                                    printfManager.openPrinter(alreadyBlueList.get(args));
                                }
                            });
                            break;
                        case 0:
                            Utils.ToastText(context,getString(R.string.connect_now));
                            //先停止搜索
                            stopSearchBlue();
                            //进行配对
                            MyApplication.getInstance().getCachedThreadPool().execute(new Runnable() {
                                int position = holder.getLayoutPosition();
                                @Override
                                public void run() {
                                    try {
                                        BluetoothDevice mDevice = mBluetoothAdapter.getRemoteDevice(bluetoothDeviceArrayList.get(position).getAddress());
                                        printfManager.openPrinter(mDevice);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            break;
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return deviceList.size();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }
    public void updateUI(){
        if(mAdapter == null){
            mAdapter = new Adapter(context, bluetoothDeviceArrayList,0);
            lv_blue_list.setAdapter(mAdapter);
            lv_already_blue_list.setAdapter(new Adapter(context,alreadyBlueList,1));
        }else {
            mAdapter.notifyDataSetChanged();
        }
    }
    private void initData() {
        printfManager = PrintfManager.getInstance(context);
        bluetoothDeviceArrayList = new ArrayList<>();
        alreadyBlueList = new ArrayList<>();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
        //蓝牙名称
        String blueName = "",blueAddress = "";
        for(BluetoothDevice device : bondedDevices){
            if (judge(device,alreadyBlueList))
                continue;
            alreadyBlueList.add(device);
            blueName = device.getName();blueAddress = device.getAddress();
        }

        mAdapter = new Adapter(context, bluetoothDeviceArrayList,0);
        lv_blue_list.setAdapter(mAdapter);
        lv_already_blue_list.setAdapter(new Adapter(context,alreadyBlueList,1));
        if(printfManager.isConnect()){
            blueName = SharedPreferencesManager.getBluetoothName(context);
            blueAddress = SharedPreferencesManager.getBluetoothAddress(context);
        }
        tv_blue_list_name.setText(getString(R.string.name_colon,blueName));
        tv_blue_list_address.setText(getString(R.string.address_colon,blueAddress));

        bluetoothChangLister = new PrintfManager.BluetoothChangLister() {
            @Override
            public void chang(String name, String address) {
                tv_blue_list_name.setText(getResources().getString(R.string.name_colon,name));
                tv_blue_list_address.setText(getString(R.string.address_colon,address));
            }
        };

        openUnpaired();
    }
    public void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        isRegister = true;
       if (getActivity()!=null)
           getActivity().registerReceiver(receiver, filter);
    }
    public void unregisterReceiver(BroadcastReceiver receiver) {
        if(getActivity()!=null){
            getActivity().unregisterReceiver(receiver);
        }
        isRegister = false;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
                if (resultCode == FragmentActivity.RESULT_OK) {
                    mBluetoothAdapter.startDiscovery();
                }
        }
    }
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //找到设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (judge(device,bluetoothDeviceArrayList))
                    return;
                bluetoothDeviceArrayList.add(device);
                 mAdapter.notifyDataSetChanged();
            }
            //搜索完成
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                tv_blue_list_operation.setText(getString(R.string.printf_blue_list_search));
                stopSearchBlue();
            }
        }
    };
    private boolean judge( BluetoothDevice device, List<BluetoothDevice> devices) {
        int majorDeviceClass = device.getBluetoothClass().getMajorDeviceClass();
        return devices.contains(device)||majorDeviceClass != 1536;

    }

    private void stopSearchBlue() {
        tv_blue_list_operation.setText(getString(R.string.printf_blue_list_search));
        if (mReceiver != null && isRegister) {
            try {
                unregisterReceiver(mReceiver);
                Utils.ToastText(context,getString(R.string.stop_search));
            }catch (Exception e){
                Log.e(TAG,e.getMessage());
            }
        }
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    @Override
    public void startActivity(Intent intent) {
        if (getActivity()!=null)
        if(PrintfManager.getInstance(getActivity()).isCONNECTING()){
            Utils.ToastText(getActivity(),getActivity().getString(R.string.bluetooth_is_being_connected));
            return;
        }
        super.startActivity(intent);
    }
}
