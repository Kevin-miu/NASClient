package client.nas.find.com.nasclient.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import client.nas.find.com.nasclient.R;
import client.nas.find.com.nasclient.adapter.base.RecyclerViewAdapter;
import client.nas.find.com.nasclient.bean.FileBean;

/**
 * @author Kevin-
 * @time 20181206
 * @description 文件列表适配器
 * @updateTime 20181206
 */

public class FileAdapter extends RecyclerViewAdapter {

    private Context context;
    private List<FileBean> list;
    private LayoutInflater mLayoutInflater;

    public FileAdapter(Context context, List<FileBean> list) {
        this.context = context;
        this.list = list;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) {
            view = mLayoutInflater.inflate(R.layout.item_file, parent, false);
            return new FileHolder(view);
        } else {
            view = mLayoutInflater.inflate(R.layout.item_line, parent, false);
            return new LineHolder(view);
        }
    }

    @Override
    public void onBindViewHolders(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FileHolder) {
            FileHolder fileHolder = (FileHolder) holder;
            fileHolder.onBindViewHolder(fileHolder, this, position);
        } else if (holder instanceof LineHolder) {
            LineHolder lineHolder = (LineHolder) holder;
            lineHolder.onBindViewHolder(lineHolder, this, position);
        }
    }

    @Override
    public Object getItem(int positon) {
        return list.get(positon);
    }

    @Override
    public Object getAdapterData() {
        return list;
    }

    @Override
    public int getItemCount() {
        if (list != null) {
            return list.size();
        } else {
            return 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).getHolderType();
    }

    public void refresh(List<FileBean> list) {
        this.list = list;
        notifyDataSetChanged();
    }
}
