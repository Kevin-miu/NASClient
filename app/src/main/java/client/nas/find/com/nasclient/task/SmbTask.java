package client.nas.find.com.nasclient.task;

import android.os.AsyncTask;
import android.util.Log;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import client.nas.find.com.nasclient.bean.FileBean;
import client.nas.find.com.nasclient.util.SmbFileUtil;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * @author Kevin-
 * @time 20181210
 * @description 异步任务，加快smb共享目录的加载速度
 * @updateTime 20181210
 */

public class SmbTask extends AsyncTask {

    private SmbFile smbFile;

    private String smbPath;
    private String username;
    private String passwd;
    private String ip;
    private SmbTask.MyTaskUIOperation myTaskUIOperation;


    public SmbTask(String smbPath, String username, String pwd, String ip) {
        this.smbPath = smbPath;
        this.username = username;
        this.passwd = pwd;
        this.ip = ip;
    }

    /**
     * 联网获取smbFile
     *
     * @return
     */
    private SmbFile getSmbFile() {

        SmbFile rootSmbFile = null;
        try {
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(ip, username, passwd);
            rootSmbFile = new SmbFile(smbPath, auth);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            return rootSmbFile;
        }
    }

    @Override
    protected Object doInBackground(Object[] objects) {

        //联网获取smbFile
        smbFile = getSmbFile();

        List<FileBean> fileBeanList = new ArrayList<>();

        try {

            if (smbFile.isDirectory()) {
                SmbFile[] fileArray = smbFile.listFiles();
                Log.i("msg", "fileArray的长度：" + fileArray.length);

                if (fileArray != null) {
                    List<SmbFile> fileList = new ArrayList<>();
                    //数组转换成列表
                    Collections.addAll(fileList, fileArray);
                    //按照名字排序
                    Collections.sort(fileList, SmbFileUtil.comparator);

                    for (SmbFile f : fileList) {
                        if (f.isHidden())
                            continue;

                        FileBean fileBean = new FileBean();
                        fileBean.setName(f.getName());
                        fileBean.setFileType(SmbFileUtil.getFileType(f));
                        fileBean.setHolderType(0);
                        //这里的路径不确定是否正确
                        fileBean.setPath(f.getPath());

                        fileBeanList.add(fileBean);

                        //设置分割线
                        FileBean lineBean = new FileBean();
                        lineBean.setHolderType(1);
                        fileBeanList.add(lineBean);
                    }
                }
            }
        } catch (SmbException e) {
            e.printStackTrace();
        } finally {
            myTaskUIOperation.setFileBeanListByMyTask(fileBeanList);
            return fileBeanList;
        }
    }

    @Override
    protected void onPostExecute(Object o) {
        myTaskUIOperation.setEmptyContainerByMyTask();
    }


    public interface MyTaskUIOperation {
        void setFileBeanListByMyTask(List<FileBean> list);

        void setEmptyContainerByMyTask();
    }

    public void setMyTaskUIOperation(SmbTask.MyTaskUIOperation myTaskUIOperation) {
        this.myTaskUIOperation = myTaskUIOperation;
    }
}
