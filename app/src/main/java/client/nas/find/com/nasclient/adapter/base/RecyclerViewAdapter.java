package client.nas.find.com.nasclient.adapter.base;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import client.nas.find.com.nasclient.adapter.FileAdapter;

/**
 * @author Kevin-
 * @time 20181130
 * @description 自定义RecyclerViewAdapter，配合RecyclerView组件使用
 * @updateTime
 */

public abstract class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /**
     * 回调接口1：点击事件监听
     */
    public interface OnItemClickListener {
        void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position);
    }

    /**
     * 回调接口2：长按事件监听
     */
    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, RecyclerView.ViewHolder viewHolder, int position);
    }

    //定义以及设置回调接口，如果外部设置时可以重写该方法
    public FileAdapter.OnItemClickListener onItemClickListener;
    public FileAdapter.OnItemLongClickListener onItemLongClickListener;

    public void setOnItemClickListener(FileAdapter.OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public void setOnItemLongClickListener(FileAdapter.OnItemLongClickListener listener) {
        onItemLongClickListener = listener;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    int pos = holder.getLayoutPosition();
                    //view的点击事件触发自定义的点击事件
                    onItemClickListener.onItemClick(v, holder, pos);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onItemLongClickListener != null) {
                    int pos = holder.getLayoutPosition();
                    //view的长按事件触发自定义的长按事件
                    return onItemLongClickListener.onItemLongClick(v, holder, pos);
                }
                return false;
            }
        });

        onBindViewHolders(holder, position);
    }

    public abstract void onBindViewHolders(RecyclerView.ViewHolder holder, int position);

    public abstract Object getAdapterData();

    public abstract Object getItem(int positon);
}
