package client.nas.find.com.nasclient.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import client.nas.find.com.nasclient.R;
import client.nas.find.com.nasclient.adapter.base.RecyclerViewAdapter;
import client.nas.find.com.nasclient.adapter.holder.TitleHolder;
import client.nas.find.com.nasclient.bean.TitlePathBean;

/**
 * @author Kevin-
 * @time 20181206
 * @description 文件表群体适配器
 * @updateTime 20181206
 */

public class TitleAdapter extends RecyclerViewAdapter {

    private List<TitlePathBean> list;
    private LayoutInflater mLayoutInflater;

    public TitleAdapter(Context context, List<TitlePathBean> list) {
        this.list = list;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.title_holder, parent, false);
        return new TitleHolder(view);
    }

    @Override
    public void onBindViewHolders(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TitleHolder) {
            TitleHolder titleHolder = (TitleHolder) holder;
            //加载TitleHolder中设置的样式
            titleHolder.onBindViewHolder(titleHolder, this, position);
        }
    }

    @Override
    public Object getAdapterData() {
        return list;
    }

    @Override
    public Object getItem(int positon) {
        return list.get(positon);
    }

    @Override
    public int getItemCount() {
        if (list == null)
            return 0;
        return list.size();
    }

    public void addItem(TitlePathBean titlePath) {
        list.add(titlePath);
        notifyItemChanged(list.size() - 1);
    }

    public void removeItem(int positon) {
        list.remove(positon);
        notifyItemRemoved(positon);
    }

    public void removeLast() {
        if (list == null)
            return;
        int lastPosition = getItemCount() - 1;
        list.remove(lastPosition);
        notifyItemRemoved(lastPosition);
    }
}
