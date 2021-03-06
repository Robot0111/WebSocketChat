package webgroupchat.androidhive.info.chat.Manaer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;

import com.android.print.sdk.PrinterConstants;
import com.android.print.sdk.PrinterInstance;
import com.github.promeg.pinyinhelper.Pinyin;
/*import com.github.promeg.pinyinhelper.Pinyin;
import com.qd.wash.Crash.CrashHandler;
import com.qd.wash.Modle.Mode;s
import com.qd.wash.MyApplication;
import com.qd.wash.R;
import com.qd.wash.Utils.Util;*/

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import webgroupchat.androidhive.info.chat.Model.Mode;
import webgroupchat.androidhive.info.chat.MyApplication;
import webgroupchat.androidhive.info.chat.R;
import webgroupchat.androidhive.info.chat.Utils.Utils;
import webgroupchat.androidhive.info.chat.crash.CrashHandler;


public class PrintfManager  {

    protected String TAG = "PrintfManager";
    public final static int WIDTH_PIXEL = 384;
    protected List<BluetoothChangLister> bluetoothChangListerList = new ArrayList<>();

    /**
     * 添加蓝牙改变监听
     *
     * @param bluetoothChangLister
     */
    public void addBluetoothChangLister(BluetoothChangLister bluetoothChangLister) {
        bluetoothChangListerList.add(bluetoothChangLister);
    }

    protected Context context;

    protected PrinterInstance mPrinter;


    private PrintfManager() {
    }

    static class PrintfManagerHolder {
        private static PrintfManager instance = new PrintfManager();

    }

    public static PrintfManager getInstance(Context context) {
        if (PrintfManagerHolder.instance.context == null) {
            PrintfManagerHolder.instance.context = context.getApplicationContext();
        }
        return PrintfManagerHolder.instance;
    }

    public void setPrinter(PrinterInstance mPrinter) {
        this.mPrinter = mPrinter;
    }

    public void connection() {
        if (mPrinter != null) {
            CONNECTING = true;
            mPrinter.openConnection();
        }
    }

    private ConnectSuccess connectSuccess;

    public void setConnectSuccess(ConnectSuccess connectSuccess) {
        this.connectSuccess = connectSuccess;
    }

    /**
     * 解除观察者
     *
     * @param bluetoothChangLister
     */
    public void removeBluetoothChangLister(BluetoothChangLister bluetoothChangLister) {
        if (bluetoothChangLister == null) {
            return;
        }
        if (bluetoothChangListerList.contains(bluetoothChangLister)) {
            bluetoothChangListerList.remove(bluetoothChangLister);
        }
    }

    /**
     * 连接
     *
     * @param mDevice
     */
    public void openPrinter(final BluetoothDevice mDevice) {
        MyApplication.getInstance().getCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                //连接保存
                SharedPreferencesManager.saveBluetoothName(context, mDevice.getName());
                SharedPreferencesManager.saveBluetoothAddress(context, mDevice.getAddress());
                setPrinter(new PrinterInstance(context, mDevice, mHandler));
                // default is gbk...
                connection();
            }
        });
    }

    private boolean isHasPrinter = false;

    public boolean isConnect() {
        return isHasPrinter;
    }

    public void disConnect(final String text) {
        MyApplication.getInstance().getCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                isHasPrinter = false;
                if (mPrinter != null) {
                    mPrinter.closeConnection();
                    mPrinter = null;
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.ToastText(context, text);
                    }
                });

            }
        });
    }

    /**
     * 是否正在连接
     */
    private volatile boolean CONNECTING = false;

    public boolean isCONNECTING() {
        return CONNECTING;
    }

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String bluetoothName = context.getString(R.string.no_connect_blue_tooth);
            String bluetoothAddress = bluetoothName;
            switch (msg.what) {
                case PrinterConstants.Connect.SUCCESS://成功
                    isHasPrinter = true;
                    Utils.ToastText(context, context.getString(R.string.connection_success));
                    bluetoothName = SharedPreferencesManager.getBluetoothName(context);
                    bluetoothAddress = SharedPreferencesManager.getBluetoothAddress(context);
                    if (connectSuccess != null) {
                        connectSuccess.success();
                    }
                    break;
                case PrinterConstants.Connect.FAILED://失败
                    disConnect(context.getString(R.string.connection_fail));
                    break;
                case PrinterConstants.Connect.CLOSED://关闭
                    disConnect(context.getString(R.string.bluetooth_disconnect));
                    break;
            }

            for (BluetoothChangLister bluetoothChangLister : bluetoothChangListerList) {
                bluetoothChangLister.chang(bluetoothName, bluetoothAddress);
            }
            CONNECTING = false;
        }
    };


    public void defaultConnection() {
        String address = SharedPreferencesManager.getBluetoothAddress(context);
        if (address == null) {
            return;
        }
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> bondedDevices = defaultAdapter.getBondedDevices();
        for (BluetoothDevice device : bondedDevices) {
            if (device.getAddress().equals(address)) {
                mPrinter = new PrinterInstance(context, device, mHandler);
                mPrinter.openConnection();
                return;
            }
        }
    }
    public void printf_answer(final byte[] b, final byte[] n) {
        MyApplication.getInstance().getCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Utils.ToastTextThread(context, "正在打印...请稍候");
                    if(b.length<100){
                        printOneColumn(b,n);
                        printfWrap(5);
                    }else {
                        mPrinter.sendByteData(b);
                        printfWrap(5);
                    }

                } catch (Exception e) {
                    CrashHandler.getInstance().saveCrashInfo2File(e);
                    e.printStackTrace();
                }
            }
        });
    }
    private void printOneColumn(byte[] title,byte[] content) throws IOException {
        int iNum = 0;
        byte[] byteBuffer = new byte[100];
        byte[] tmp = title;

        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.length);
        iNum += tmp.length;

       /* tmp = setLocation(getOffset(content));
        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.length);
        iNum += tmp.length;*/

        tmp = content;
        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.length);


        mPrinter.sendByteData(byteBuffer);
    }
    public void printf_50(final String companyName,
                          final String operator, final String remark,
                          final List<Mode> modeList) {

        MyApplication.getInstance().getCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Utils.ToastTextThread(context, "正在打印...请稍候");
                    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
                    //计算居中的左边距
                    int left = getCenterLeft(48, bitmap);
                    byte[] bytes = bitmap2PrinterBytes(bitmap, left);
                    mPrinter.sendByteData(bytes);
                    printTwoColumn("公司名称:", companyName);
                    printfWrap();
                    printTwoColumn("操作人:", operator);
                    printfWrap();
                    printTwoColumn("时间:", Utils.stampToDate(System.currentTimeMillis()));
                    printfWrap();
                    printPlusLine_50();
                    printText("类型");
                    printTabSpace(6);
                    printText("数量");
                    printTabSpace(4);
                    printText("价钱");
                    printfWrap();
                    //补全空白算法：举例：“类型”字符串占据4个字节，用对应的类型名称减去4，则得到要补全的空白字节数
                    // " " 表示一个字节
                    for (int j = 0; j < modeList.size(); j++) {
                        Mode mode = modeList.get(j);
                        String name = mode.getFromName();
                        printText(name);//打印类型
                        int supplementNumber = name.getBytes().length - 4;
                        printTabSpace(6 - supplementNumber);//补全空白

                        Integer number = mode.isSelf();
                        printText(String.valueOf(number));//打印数量
                        supplementNumber = String.valueOf(number).getBytes().length - 4;
                        printTabSpace(4 - supplementNumber);//补全空白
                       /* String totalPrice = String.valueOf(mode.getPrice() * number);
                        printText(totalPrice);//打印总价*/

                        printfWrap();
                    }
                    printPlusLine_50();
                    printText("备注：");
                    printfWrap();
                    printText(remark);
                    printfWrap(4);
                } catch (Exception e) {
                    CrashHandler.getInstance().saveCrashInfo2File(e);
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 打印粗体与字体大小
     * @param text ：需要打印的字体注意要加上换行
     * @param size ：大小
     * @param isBold：是否要粗体
     */
    public void printf_bold_size(String text, int size,boolean isBold) {
        try {
           /* if (isBold) {
                mPrinter.sendByteData(boldOn());
            }else{
                mPrinter.sendByteData(boldOff());
            }*/
            mPrinter.sendByteData(boldOn());
            printLargeText(size, text);
            printfWrap(3);
        } catch (Exception e) {
            CrashHandler.getInstance().saveCrashInfo2File(e);
            e.printStackTrace();
        }
    }
    public static int getCenterLeft(int paperWidth, Bitmap bitmap) {
        //计算居中的边距
        int width = bitmap.getWidth();
        //计算出图片在纸上宽度 单位为mm   8指的是1mm=8px
        float bitmapPaperWidth = width / 8F;
        //79为真实纸宽
        return (int) (paperWidth / 2F - bitmapPaperWidth / 2);
    }

    /**
     * 将bitmap对象转化成byte数组
     *
     * @param bitmap
     * @param left：图片左边距
     * @return 图片居中方法：可增加左边距，让图片居中
     */
    public static byte[] bitmap2PrinterBytes(Bitmap bitmap, int left) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        byte[] imgbuf = new byte[(width / 8 + left + 4) * height];
        byte[] bitbuf = new byte[width / 8];
        int[] p = new int[8];
        int s = 0;
        System.out.println("+++++++++++++++ Total Bytes: " + (width / 8 + 4) * height);

        for (int y = 0; y < height; ++y) {
            int n;
            for (n = 0; n < width / 8; ++n) {
                int value;
                for (value = 0; value < 8; ++value) {
                    int grey = bitmap.getPixel(n * 8 + value, y);
                    int red = ((grey & 0x00FF0000) >> 16);
                    int green = ((grey & 0x0000FF00) >> 8);
                    int blue = (grey & 0x000000FF);
                    int gray = (int) (0.29900 * red + 0.58700 * green + 0.11400 * blue); // 灰度转化公式
                    if (gray <= 190) {
                        gray = 1;
                    } else {
                        gray = 0;
                    }
                    p[value] = gray;
                }
                value = p[0] * 128 + p[1] * 64 + p[2] * 32 + p[3] * 16 + p[4] * 8 + p[5] * 4 + p[6] * 2 + p[7];
                bitbuf[n] = (byte) value;
            }

            if (y != 0) {
                ++s;
                imgbuf[s] = 22;
            } else {
                imgbuf[s] = 22;
            }

            ++s;
            imgbuf[s] = (byte) (width / 8 + left);

            for (n = 0; n < left; ++n) {
                ++s;
                imgbuf[s] = 0;
            }

            for (n = 0; n < width / 8; ++n) {
                ++s;
                imgbuf[s] = bitbuf[n];
            }

            ++s;
            imgbuf[s] = 21;
            ++s;
            imgbuf[s] = 1;
        }

        return imgbuf;
    }


    /**
     * @param length 需要打印空白的长度,
     * @throws IOException
     */
    private void printTabSpace(int length) throws IOException {
        StringBuilder space1 = new StringBuilder();
        for (int i = 0; i < length; i++) {
            space1.append(" ");
        }
        mPrinter.sendByteData(space1.toString().getBytes());
    }


    private void printTwoColumn(String title, String content) throws IOException {
        int iNum = 0;
        byte[] byteBuffer = new byte[100];
        byte[] tmp;

        tmp = getGbk(title);
        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.length);
        iNum += tmp.length;

        tmp = setLocation(getOffset(content));
        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.length);
        iNum += tmp.length;

        tmp = getGbk(content);
        System.arraycopy(tmp, 0, byteBuffer, iNum, tmp.length);

        mPrinter.sendByteData(byteBuffer);
    }

    private byte[] getGbk(String stText) throws IOException {
        byte[] returnText = stText.getBytes("GBK"); // 必须放在try内才可以
        return returnText;
    }

    private void printfWrap() throws IOException {
        printfWrap(1);
    }

    private void printfWrap(int lineNum) throws IOException {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < lineNum; i++) {
            line.append(" \n");
        }
        mPrinter.sendByteData(line.toString().getBytes());
    }

    /**
     * 绝对打印位置
     *
     * @return
     * @throws IOException
     */
    private byte[] setLocation(int offset) throws IOException {
        byte[] bs = new byte[4];
        bs[0] = 0x1B;
        bs[1] = 0x24;
        bs[2] = (byte) (offset % 256);
        bs[3] = (byte) (offset / 256);
        return bs;
    }

    private int getOffset(String str) {
        return WIDTH_PIXEL - getStringPixLength(str);
    }

    private int getStringPixLength(String str) {
        int pixLength = 0;
        char c;
        for (int i = 0; i < str.length(); i++) {
            c = str.charAt(i);
            if (Pinyin.isChinese(c)) {
                pixLength += 24;
            } else {
                pixLength += 12;
            }
        }
        return pixLength;
    }

    /**
     * 注意：线条不能太长，不然会换出一行，如果决定长度不够，可以增城两个字符，但是得去掉换行符
     *
     * @throws IOException
     */
    private void printPlusLine_80() throws IOException {
        printText("- - - - - - - - - - - - - - - - - - - - - - -\n");
    }

    private void printPlusLine_50() throws IOException {
        printText("- - - - - - - - - - - - - - - -\n");
    }

    /**
     * 字体大小
     *
     * @param text
     * @throws IOException
     */
    public void printLargeText(int size, String text) throws IOException {
        byte[] bytes = {0x1b, 0x21, (byte) size};//代表字体的大小
        mPrinter.sendByteData(bytes);
        printText(text);
        byte[] bytes1 = {0x1b, 0x21, 0};
        mPrinter.sendByteData(bytes1);
        mPrinter.init();
    }

    /**
     * 取消加粗模式
     *
     * @return
     */
    public static byte[] boldOff() {
        byte[] result =  { 27, 69, 0 };
        return result;
    }

    /**
     * 选择加粗模式
     *
     * @return
     */
    public static byte[] boldOn() {
        byte[] result = { 27, 69, 1 };
        return result;
    }

    public void changBlueName(final String name) {
        //启动线程，来接收数据
        MyApplication.getInstance().getCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Utils.ToastTextThread(context, context.getString(R.string.chang_bluetooth_name_now));
                    //进入空中AT指令
                    String AT = "$OpenFscAtEngine$";
                    mPrinter.sendByteData(AT.getBytes());
                    Thread.sleep(500);
                    byte[] read = mPrinter.read();
                    if (read == null) {
                        Utils.ToastTextThread(context, context.getString(R.string.chang_bluetooth_name_fail));
                    } else {
                        String readString = new String(read);
                        if (readString.contains("$OK,Opened$")) {//进入空中模式
                            mPrinter.sendByteData(("AT+NAME=" + name + "\r\n").getBytes());
                            Thread.sleep(500);
                            byte[] isSuccess = mPrinter.read();
                            if (new String(isSuccess).contains("OK")) {
                                Utils.ToastTextThread(context, context.getString(R.string.chang_bluetooth_name_success));
                                SharedPreferencesManager.saveBluetoothName(context,name);
                            } else {
                                Utils.ToastTextThread(context, context.getString(R.string.chang_bluetooth_name_fail));
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 打印文字
     *
     * @param text
     * @throws IOException
     */
    private void printText(String text) throws IOException {
        mPrinter.sendByteData(getGbk(text));
    }

    public interface BluetoothChangLister {
        void chang(String name, String address);
    }

    public interface ConnectSuccess {
        void success();
    }

}
