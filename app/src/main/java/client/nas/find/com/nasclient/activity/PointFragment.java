package client.nas.find.com.nasclient.activity;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import client.nas.find.com.nasclient.R;
import client.nas.find.com.nasclient.util.LocalNetUtil;

/**
 * @author Kevin-
 * @time
 * @description
 * @updateTime
 */

public class PointFragment extends Fragment implements LocalNetUtil.ScanIpCallback {

    private View view;
    private Context context;

    private LocalNetUtil mLocalNetUtil = null;

    private boolean isExit = false;

    /**
     * 设置context
     *
     * @param context
     */
    public void setContext(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_point, container, false);

        initTest();

        return view;

    }

    /**
     * 初始化测试
     */
    private void initTest() {
        TextView tx2 = view.findViewById(R.id.point_tx2);
        TextView TX3 = view.findViewById(R.id.point_tx3);

        mLocalNetUtil = new LocalNetUtil(context, this);
        tx2.setText(mLocalNetUtil.getLocalIp());
        TX3.setText(mLocalNetUtil.getNetMask());
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

    @Override
    public void onNotFound(List<String> ips, String msg) {

    }

    @Override
    public void onFound(List<String> ips, String msg) {

    }
}
