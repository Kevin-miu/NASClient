package client.nas.find.com.nasclient.adapter;


import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import client.nas.find.com.nasclient.R;
import client.nas.find.com.nasclient.activity.CommonDialog;
import client.nas.find.com.nasclient.adapter.base.MultiAdapter;
import client.nas.find.com.nasclient.bean.DeviceBean;
import client.nas.find.com.nasclient.util.Util;

/**
 * @author Kevin-
 * @time 20181204
 * @description 设备Adapter
 * @updateTime 20181204
 */

public class DeviceAdapter extends MultiAdapter<DeviceBean> {

    private Context context;
    private View view;

    private TextView deviceName, deviceIp;
    private ImageView deviceIcon;

    private ItemClickListener mItemClickListener;

    public DeviceAdapter(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public View getView(ViewGroup parent, int position) {

        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false);

        deviceName = view.findViewById(R.id.device_name);
        deviceIp = view.findViewById(R.id.device_ip);
        deviceIcon = view.findViewById(R.id.device_icon);

        return view;
    }

    @Override
    public void setData(DeviceBean object) {
        super.setData(object);

        deviceName.setText(object.getHostname());
        deviceIp.setText(object.getIp());
    }

    @Override
    public void setOnItemClick(final int position) {
        super.setOnItemClick(position);

        final DeviceBean device = getItem(position);

        CommonDialog dialog = new CommonDialog(context, R.style.dialog, "确定删除此消息" + Integer.toString(position), new CommonDialog.OnCloseListener() {
            @Override
            public void onClick(Dialog dialog, boolean confirm, String username, String passwd, boolean isCheck) {
                //如果是确认键
                if (confirm) {
                    //1.传递用username和passwd

                    Util.Toast("用户 " + username + "正在请求，密码是" + passwd);

                    //2.触发跳转
                    mItemClickListener.onclick();

                }
            }
        });
        dialog.setTitle("正在连接到 smb://" + device.getIp() + "/").show();

        //手动设置对话框宽度
        int screenWidth = Util.getScreenWidth();
        WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        p.width = (int) (screenWidth * 0.9); // 宽度设置为屏幕的0.9,高度不变
        dialog.getWindow().setAttributes(p);
    }

    //定义触发跳转的回调接口
    public interface ItemClickListener {
        void onclick();
    }

    public void setOnItemClickListener(ItemClickListener itemClick) {
        this.mItemClickListener = itemClick;
    }

}
