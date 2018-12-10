package client.nas.find.com.nasclient.adapter.base;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * 定义holder的回调接口，继承者可以在自己的类里重写onBindViewHolder
 * 这是因为在adapter里是无法操作UI的
 * 通过回调的方法：adapter的事件调用onBindViewHolder，而onBindViewHolder在holder里被重写可以操作UI的语句
 * @param <T>
 */

public abstract class RecyclerViewHolder<T> extends RecyclerView.ViewHolder {

    public RecyclerViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void onBindViewHolder(T t, RecyclerViewAdapter adapter, int position);
}
