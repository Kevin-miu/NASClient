package client.nas.find.com.nasclient.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import client.nas.find.com.nasclient.R;
import client.nas.find.com.nasclient.adapter.base.MultiAdapter;
import client.nas.find.com.nasclient.bean.DeviceBean;

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
        mItemClickListener.onItemClick(this, position);
    }

    //定义触发跳转的回调接口
    public interface ItemClickListener {
        void onItemClick(DeviceAdapter adppter, int position);
    }

    public void setOnItemClickListener(ItemClickListener itemClick) {
        this.mItemClickListener = itemClick;
    }

}
