package client.nas.find.com.nasclient.adapter.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;

import client.nas.find.com.nasclient.R;
import client.nas.find.com.nasclient.adapter.base.RecyclerViewAdapter;
import client.nas.find.com.nasclient.adapter.base.RecyclerViewHolder;
import client.nas.find.com.nasclient.bean.FileBean;
import client.nas.find.com.nasclient.bean.FileType;

/**
 * @author Kevin-
 * @time
 * @description
 * @updateTime
 */

public class FileHolder extends RecyclerViewHolder<FileHolder> {

    private ImageView fileIcon;
    private TextView fileName;
    private ImageView fileEnterIcon;

    public FileHolder(View view) {
        super(view);
        fileIcon = (ImageView) view.findViewById(R.id.file_icon);
        fileName = (TextView) view.findViewById(R.id.file_name);
        fileEnterIcon = (ImageView) view.findViewById(R.id.file_enter_icon);
    }

    @Override
    public void onBindViewHolder(final FileHolder fileHolder, RecyclerViewAdapter adapter, int position) {

        FileBean fileBean = (FileBean) adapter.getItem(position);
        fileHolder.fileName.setText(fileBean.getName());

        //设置文件名
        FileType fileType = fileBean.getFileType();
        //设置进入图标的可见性：目录可见，文件不可见
        if (fileType == FileType.directory) {
            fileHolder.fileEnterIcon.setVisibility(View.VISIBLE);
        } else {
            fileHolder.fileEnterIcon.setVisibility(View.GONE);
        }

        //设置图标
        if (fileType == FileType.directory) {
            fileHolder.fileIcon.setImageResource(R.mipmap.file_icon_dir);
        } else if (fileType == FileType.music) {
            fileHolder.fileIcon.setImageResource(R.mipmap.file_icon_music);
        } else if (fileType == FileType.video) {
            fileHolder.fileIcon.setImageResource(R.mipmap.file_icon_video);
        } else if (fileType == FileType.txt) {
            fileHolder.fileIcon.setImageResource(R.mipmap.file_icon_txt);
        } else if (fileType == FileType.zip) {
            fileHolder.fileIcon.setImageResource(R.mipmap.file_icon_zip);
        } else if (fileType == FileType.image) {
            Glide.with(fileHolder.itemView.getContext()).load(new File(fileBean.getPath())).into(fileHolder.fileIcon);
        } else if (fileType == FileType.apk) {
            fileHolder.fileIcon.setImageResource(R.mipmap.file_icon_apk);
        } else {
            fileHolder.fileIcon.setImageResource(R.mipmap.file_icon_other);
        }
    }
}
