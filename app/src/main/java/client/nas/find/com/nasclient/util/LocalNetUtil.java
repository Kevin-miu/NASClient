package client.nas.find.com.nasclient.util;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author Kevin-
 * @time 20181203
 * @description 局域网操作类
 * @updateTime 20181203
 */

public class LocalNetUtil {

    private Context context;

    private String localIp; //本地ip地址 如 192.168.0.1
    private String netMask; //子网掩码，用来计算网段
    private String networkSegment; //本地网段 如 192.168.0.

    private int ipIndex; //ip网段最后位 如 1~255
    private volatile List<String> localNetIps = new ArrayList<>();//存放同网段的ip

    private boolean isRoot = false;//是否root

    //命令行变量
    private String ping = "ping -c 3 -w 10 ";//-c 是指ping的次数 -w 10  以秒为单位指定超时间
    private Runtime runtime = Runtime.getRuntime();
    private Process process = null;

    //定义回调接口
    private ScanIpCallback mScanIpCallback = null;

    //定义WifiManager对象
    private WifiManager mWifiManager;
    //定义WifiInfo对象
    private WifiInfo mWifiInfo;
    //定义DhcpInfo对象
    private DhcpInfo mDhcpInfo;
    //扫描出的网络连接列表
    private List<ScanResult> mWifiList;
    //网络连接列表
    private List<WifiConfiguration> mWifiConfiguration;
    //定义一个WifiLock
    WifiManager.WifiLock mWifiLock;


    //判断是否root的辅助常量(暂时没用)
    private final static int kSystemRootStateUnknow = -1;
    private final static int kSystemRootStateDisable = 0;
    private final static int kSystemRootStateEnable = 1;
    private static int systemRootState = kSystemRootStateUnknow;


    /**
     * 构造函数
     *
     * @param context
     */
    public LocalNetUtil(Context context, ScanIpCallback callback) {

        this.context = context;
        this.mScanIpCallback = callback;

        //取得WifiManager对象
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        //取得WifiInfo对象
        mWifiInfo = mWifiManager.getConnectionInfo();
        //取得DhcpInfo对象
        mDhcpInfo = mWifiManager.getDhcpInfo();

        mWifiManager.startScan();
        //得到扫描结果
        mWifiList = mWifiManager.getScanResults();
        //得到配置好的网络连接
        mWifiConfiguration = mWifiManager.getConfiguredNetworks();

        //设置本地ip
        String ip = transferLocalIp().toString();
        localIp = ip.substring(1, ip.length());
        System.out.println("获取IP：" + localIp);
        //设置子网掩码
        long mask = mDhcpInfo.netmask;
        netMask = longToNetForm(mask);
        System.out.println("获取Mask：" + netMask);
        //计算网段
        networkSegment = this.localIp.substring(0, this.localIp.lastIndexOf(".") + 1);
    }


    /**
     * 获取本地ip
     */
    public InetAddress transferLocalIp() {

        int hostAddress = mWifiInfo.getIpAddress();
        byte[] addressBytes = {(byte) (0xff & hostAddress),
                (byte) (0xff & (hostAddress >> 8)),
                (byte) (0xff & (hostAddress >> 16)),
                (byte) (0xff & (hostAddress >> 24))};

        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }

    /**
     * 将long型数据转成网络地址型的字符串
     *
     * @param numLong
     * @return
     */
    private String longToNetForm(long numLong) {
        String numStr = "";
        for (int i = 3; i >= 0; i--) {
            numStr += String.valueOf((numLong & 0xff));
            if (i != 0) {
                numStr += ".";
            }
            numLong = numLong >> 8;
        }
        return numStr;
    }

    /**
     * 将网络地址型的字符串转成long型数据
     *
     * @param numStr
     * @return
     */
    private long netTolongForm(String numStr) {
        Long numLong = 0L;
        String[] numbers = numStr.split("\\.");
        //等价上面
        for (int i = 0; i < 4; ++i) {
            numLong = numLong << 8 | Integer.parseInt(numbers[i]);
        }
        return numLong;
    }

    /**
     * 定义（静态）回调接口，必须实现以下回调方法
     */
    public interface ScanIpCallback {
        void onFound(List<String> ips, String msg);

        void onNotFound(List<String> ips, String msg);
    }


    /**
     * 扫描同网段的所有IP
     */
    public void scanIpInSameSegment() throws InterruptedException {

        final CountDownLatch latch = new CountDownLatch(256);
        localNetIps.clear();

        if (this.localNetIps == null || "".equals(this.localNetIps)) {
            return;
        }

        //没有权限
        //if (!LocalNetUtil.isRootSystem()) {
        //    toast("扫描网络ip需要root权限,请先root后再尝试!");
        //    return null;
        //}

        //产生256个线程测试ip
        for (int i = 0; i < 256; i++) {
            ipIndex = i;
            new Thread(new Runnable() {
                @Override
                public synchronized void run() {

                    String currentIp = networkSegment + ipIndex;

                    if (currentIp.equals(localIp)) {

                    }

                    String command = ping + currentIp;

                    try {
                        process = runtime.exec(command);

                        int result = process.waitFor();

                        if (result == 0) {
                            Log.i("IP", "连接成功:" + currentIp);

                            if (!currentIp.equals(localIp)) {
                                localNetIps.add(currentIp);
                            }

                        } else {
                            Log.i("IP", "连接失败:" + currentIp);
                        }

                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    } finally {
                        process.destroy();
                        latch.countDown(); //子线程执行完毕后，latch中的数字减1
                        //如果latch中数字为0代表全部子线程执行完毕，回调
                        if (latch.getCount() <= 0) {
                            if (localNetIps.size() > 0) {
                                mScanIpCallback.onFound(localNetIps, "扫描成功");
                            } else {
                                mScanIpCallback.onNotFound(localNetIps, "没有扫描到设备");
                            }
                        }
                    }
                }
            }).start();
        }

        //        latch.await();//阻塞当前线程，直到所有子线程执行完毕

    }

    /**
     * 获取root权限（暂时没有用上）
     */
    public void getRootPermission() {

        if (LocalNetUtil.isRootSystem()) {
            return;
        }

        try {
            String rootCommand = "su";
            process = runtime.exec(rootCommand);

            int result = process.waitFor();

            if (result == 0) {
                this.isRoot = true;
                Log.i("IP", "Root成功");
                Toast.makeText(context, "Root成功", Toast.LENGTH_SHORT).show();
            } else {
                Log.i("IP", "Root失败");
                Toast.makeText(context, "Root失败", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 判断是否已经root
     */
    public static boolean isRootSystem() {
        if (systemRootState == kSystemRootStateEnable) {
            return true;
        } else if (systemRootState == kSystemRootStateDisable) {
            return false;
        }
        File f = null;
        final String kSuSearchPaths[] = {"/system/bin/", "/system/xbin/",
                "/system/sbin/", "/sbin/", "/vendor/bin/"};
        try {
            for (int i = 0; i < kSuSearchPaths.length; i++) {
                f = new File(kSuSearchPaths[i] + "su");
                if (f != null && f.exists()) {
                    systemRootState = kSystemRootStateEnable;
                    return true;
                }
            }
        } catch (Exception e) {
        }
        systemRootState = kSystemRootStateDisable;
        return false;
    }

    public String getLocalIp() {
        return localIp;
    }

    public String getNetMask() {
        return netMask;
    }

    public String getNetworkSegment() {
        return networkSegment;
    }
}
