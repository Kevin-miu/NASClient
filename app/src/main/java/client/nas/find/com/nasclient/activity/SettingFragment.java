package client.nas.find.com.nasclient.activity;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import client.nas.find.com.nasclient.R;

/**
 * @author Kevin-
 * @time
 * @description
 * @updateTime
 */

public class SettingFragment extends Fragment {

    private View view;
    private Context context;
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

        view = inflater.inflate(R.layout.fragment_setting, container, false);

        return view;
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
}
