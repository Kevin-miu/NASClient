package client.nas.find.com.nasclient.util;

import android.os.AsyncTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import client.nas.find.com.nasclient.bean.FileBean;

/**
 * @author Kevin-
 * @time
 * @description
 * @updateTime
 */

public class MyTask extends AsyncTask {

    private File file;
    private MyTaskUIOperation myTaskUIOperation;

    public MyTask(File file) {
        this.file = file;
    }

    @Override
    protected Object doInBackground(Object[] objects) {

        List<FileBean> fileBeanList = new ArrayList<>();
        if (file.isDirectory()) {
            File[] fileArray = file.listFiles();

            if (fileArray != null) {
                List<File> fileList = new ArrayList<>();
                //数组转换成列表
                Collections.addAll(fileList, fileArray);
                //按照名字排序
                Collections.sort(fileList, LocalFileUtil.comparator);

                for (File f : fileList) {
                    if (f.isHidden())
                        continue;

                    FileBean fileBean = new FileBean();
                    fileBean.setName(f.getName());
                    fileBean.setFileType(LocalFileUtil.getFileType(f));
                    fileBean.setHolderType(0);
                    fileBean.setSize(f.length());
                    fileBean.setPath(f.getAbsolutePath());
                    fileBean.setChildCount(LocalFileUtil.getFileChildCount(f));

                    fileBeanList.add(fileBean);

                    //设置分割线
                    FileBean lineBean = new FileBean();
                    lineBean.setHolderType(1);
                    fileBeanList.add(lineBean);
                }
            }
        }
        myTaskUIOperation.setFileBeanListByMyTask(fileBeanList);
        return fileBeanList;
    }

    @Override
    protected void onPostExecute(Object o) {
        myTaskUIOperation.setEmptyContainerByMyTask();
    }


    public interface MyTaskUIOperation {
        void setFileBeanListByMyTask(List<FileBean> list);

        void setEmptyContainerByMyTask();
    }

    public void setMyTaskUIOperation(MyTaskUIOperation myTaskUIOperation) {
        this.myTaskUIOperation = myTaskUIOperation;
    }
}
