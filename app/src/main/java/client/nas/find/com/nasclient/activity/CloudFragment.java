package client.nas.find.com.nasclient.activity;

import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import client.nas.find.com.nasclient.R;
import client.nas.find.com.nasclient.activity.base.MultiView;
import client.nas.find.com.nasclient.adapter.DeviceAdapter;
import client.nas.find.com.nasclient.bean.DeviceBean;
import client.nas.find.com.nasclient.util.CommomUtil;
import client.nas.find.com.nasclient.util.LocalNetUtil;
import client.nas.find.com.nasclient.util.PerferenceUtil;
import jcifs.netbios.NbtAddress;

/**
 * @author Kevin-
 * @time
 * @description
 * @updateTime
 */

public class CloudFragment extends Fragment implements LocalNetUtil.ScanIpCallback {

    //主界面（UI线程）变量、空间等
    private View view;
    private Context context;
    private boolean isExit = false;
    private ProgressDialog mProgressDialog = null;
    private Button btnScan, btnAdd;
    private Handler mUIHandler = new Handler(Looper.getMainLooper());

    FileFragment fileFragment;

    //其他工具、辅助类等
    private DismissAction mDismissAction = new DismissAction();
    private MultiView multiView = null;
    private DeviceAdapter mAdapter = null;
    private LocalNetUtil localNetUtil = null;

    //其他变量
    private List<DeviceBean> mDevices = new ArrayList<>();
    private List<String> mIps = new ArrayList<>();
    private List<String> mHostNames = new ArrayList<>();

    //回调接口实例化
    private OnSwitchFragment onSwitchFragment;

    /**
     * 设置Context
     */
    public void setContext(Context context) {
        this.context = context;
    }

    public interface OnSwitchFragment {
        void switchFragmentToFileFragment(Fragment fragment);
    }

    public void setOnSwitchFragment(OnSwitchFragment onSwitchFragment) {
        this.onSwitchFragment = onSwitchFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, Bundle savedInstanceState) {

        fileFragment = new FileFragment();
        fileFragment.setContext(context);

        view = inflater.inflate(R.layout.fragment_cloud, container, false);
        multiView = view.findViewById(R.id.device_container);
        mAdapter = new DeviceAdapter(view.getContext());
        multiView.setAdapter(mAdapter);

        // 设置触发页面跳转
        mAdapter.setOnItemClickListener(new DeviceAdapter.ItemClickListener() {
            @Override
            public void onItemClick(DeviceAdapter adapter, int position) {

                showDialog(adapter, position);
            }
        });

        //从sharepreference中获取默认值
        String perIP = PerferenceUtil.getIp(context);
        String perHostname = PerferenceUtil.getHostname(context);

        if (perIP != "" && perHostname != "") {
            DeviceBean deviceBean = new DeviceBean();
            deviceBean.setHostname(perHostname);
            deviceBean.setIp(perIP);

            if (!mDevices.contains(deviceBean)) {
                //仅当不存在设备列表中才添加
                mDevices.add(deviceBean);
            }
        }

        mAdapter.addAll(mDevices);


        //设置扫描按钮的点击事件(fragment专用方法)
        btnScan = view.findViewById(R.id.btn_scan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanDevice(v);
            }
        });

        //设置扫描按钮的点击事件
        btnAdd = view.findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDevice(v);
            }
        });

        return view;
    }

    /**
     * 扫描按钮点击事件
     *
     * @param v
     */
    public void scanDevice(View v) {

        //0 再次扫描之前需要将列表清空
        mDevices.clear();

        //1 扫描之前，主线程需要根据情况等待一段时间，应该配合相关的等待窗口
        mProgressDialog = ProgressDialog.show(context, "提示", "正在扫描中");

        //2 扫描局域网IP
        try {
            localNetUtil = new LocalNetUtil(context, this);
            //localNetUtil.getRootPermission();
            localNetUtil.scanIpInSameSegment();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * 添加设备
     *
     * @param v
     */
    public void addDevice(View v) {
        CommomUtil.Toast("点击添加设备");
    }


    /**
     * 扫描成功回调次函数
     *
     * @param ips 扫描结果
     * @param msg 成功消息
     */
    @Override
    public void onFound(List<String> ips, String msg) {
        mIps.clear();
        mHostNames.clear();
        //获取ip
        mIps = ips;
        //获取hostName
        Log.i("msg", "扫描结束，扫描到的长度：" + mIps.size());
        parseHostName(mIps);
        mUIHandler.post(mDismissAction);
    }

    /**
     * 扫描不成功回调此函数
     *
     * @param ips 扫描结果
     * @param msg 成功消息
     */
    @Override
    public void onNotFound(List<String> ips, String msg) {
        mIps.clear();
        mHostNames.clear();
        Log.i("msg", "扫描结束，扫描到的长度：" + mIps.size());
        mUIHandler.post(mDismissAction);
    }


    /**
     * 提示退出该Activity
     */
    public void exit() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(getActivity(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false;
                }
            }, 2000);
        } else {
            getActivity().finish();
        }
    }

    /**
     * 关闭等待窗口特定类
     */
    private class DismissAction implements Runnable {

        @Override
        public void run() {
            //结束等待窗口
            mProgressDialog.dismiss();
            Log.i("msg", "扫描完成");
            CommomUtil.Toast("扫描完成");
            //需要将之前保存在adapter的数据清空，否则会重复（区别清空设备列表）
            mAdapter.clear();
            Log.i("msg", "装配前设备列表的长度：" + mDevices.size());
            mAdapter.addAll(mDevices);
        }
    }

    /**
     * 表单窗口
     *
     * @param adapter
     * @param position
     */
    private void showDialog(DeviceAdapter adapter, int position) {
        final DeviceBean device = adapter.getItem(position);

        CommonDialog dialog = new CommonDialog(context, R.style.dialog, "正在连接", new CommonDialog.OnCloseListener() {
            @Override
            public void onClick(Dialog dialog, boolean confirm, String username, String passwd, boolean isCheck) {
                //如果是确认键
                if (confirm) {
                    Log.i("msg", "执行跳转click");

                    //这边要先将设置默认的值保存到sharePreference中
                    if (isCheck) {
                        //PerferenceUtil.setWorkgroup("OPENWRT", context);
                        //PerferenceUtil.setFolder("FamilyCloud",context);
                        PerferenceUtil.setCheck(isCheck, context);
                        PerferenceUtil.setIp(device.getIp(), context);
                        PerferenceUtil.setHostname(device.getHostname(), context);
                        PerferenceUtil.setUser(username, context);
                        PerferenceUtil.setPass(passwd, context);
                    }

                    //带消息跳转
                    Bundle bundle = new Bundle();
                    bundle.putString("username", username);
                    bundle.putString("passwd", passwd);
                    bundle.putString("ip", device.getIp());
                    fileFragment.setArguments(bundle);


                    //这里切换时可能会有问题
                    onSwitchFragment.switchFragmentToFileFragment(fileFragment);
                }
            }
        });
        //设置窗口的控件默认值
        dialog.setTitle("正在连接到 smb://" + device.getIp() + "/").setCheckBox(PerferenceUtil.getCheck(context)).
                setUsernameEd(PerferenceUtil.getUser(context)).setPasswdEd(PerferenceUtil.getPass(context)).
                show();

        //手动设置对话框宽度
        int screenWidth = CommomUtil.getScreenWidth();
        WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        p.width = (int) (screenWidth * 0.9); // 宽度设置为屏幕的0.9,高度不变
        dialog.getWindow().setAttributes(p);
    }

    /**
     * 根据ip解析主机名
     *
     * @param ips
     */
    private void parseHostName(List<String> ips) {
        //再次扫描需要将设备列表清空，否则会出现重复
        mDevices.clear();
        //应该为0
        Log.i("msg", "解析前的设备列表长度：" + mDevices.size());
        //应该为扫描得到的ip数量
        Log.i("msg", "解析前的IP列表长度：" + ips.size());
        if (ips != null && ips.size() > 0) {
            for (String ip : ips) {
                try {
                    NbtAddress nbtAddress = NbtAddress.getByName(ip);
                    DeviceBean device = new DeviceBean();
                    device.setIp(ip);
                    String name = nbtAddress.firstCalledName();
                    Log.i("hostName", name);
                    //添加到设备名列表
                    mHostNames.add(name);
                    device.setHostname(name);
                    //添加到设备列表
                    mDevices.add(device);

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    Log.i("err", "主机名解析错误");
                }
            }
        }
    }

}
