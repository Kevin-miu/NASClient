package client.nas.find.com.nasclient.task;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import client.nas.find.com.nasclient.bean.FileBean;
import client.nas.find.com.nasclient.util.LocalFileUtil;

/**
 * @author Kevin-
 * @time 20181210
 * @description 异步任务，加快本地目录加载速度
 * @updateTime 20181210
 */

public class LocalTask extends AsyncTask {

    private File file;
    private MyTaskUIOperation myTaskUIOperation;

    public LocalTask(File file) {
        this.file = file;
    }

    @Override
    protected Object doInBackground(Object[] objects) {

        List<FileBean> fileBeanList = new ArrayList<>();
        if (file.isDirectory()) {
            File[] fileArray = file.listFiles();
            Log.i("msg", "fileArray的长度：" + fileArray.length);

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
                    fileBean.setPath(f.getAbsolutePath());

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
