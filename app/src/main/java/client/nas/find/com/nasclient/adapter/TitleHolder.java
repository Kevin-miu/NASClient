package client.nas.find.com.nasclient.adapter;

import android.view.View;
import android.widget.TextView;

import client.nas.find.com.nasclient.R;
import client.nas.find.com.nasclient.adapter.base.RecyclerViewAdapter;
import client.nas.find.com.nasclient.adapter.base.RecyclerViewHolder;
import client.nas.find.com.nasclient.bean.TitlePathBean;

/**
 * @author Kevin-
 * @time
 * @description
 * @updateTime
 */

public class TitleHolder extends RecyclerViewHolder<TitleHolder> {

    private TextView fileTitleTxt;

    public TitleHolder(View itemView) {
        super(itemView);

        fileTitleTxt = itemView.findViewById(R.id.file_title_Name);
    }

    @Override
    public void onBindViewHolder(TitleHolder titleHolder, RecyclerViewAdapter adapter, int position) {
        TitlePathBean titlePath = (TitlePathBean) adapter.getItem(position);

        titleHolder.fileTitleTxt.setText(titlePath.getNameState());
    }
}
