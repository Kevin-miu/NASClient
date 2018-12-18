package client.nas.find.com.nasclient.task;

import android.os.AsyncTask;

import java.net.MalformedURLException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * @author Kevin-
 * @time
 * @description
 * @updateTime
 */

public class SmbMkdirTask extends AsyncTask<String, Integer, Boolean> {

    private String smbPath;
    private String username;
    private String passwd;
    private String ip;

    private OnFinishMkdir onFinishMkdir;

    public interface OnFinishMkdir {
        void onSuccess();

        void onFail();
    }

    public SmbMkdirTask(String smbPath, String username, String passwd, String ip, OnFinishMkdir onFinishMkdir) {

        this.smbPath = smbPath;
        this.username = username;
        this.passwd = passwd;
        this.ip = ip;
        this.onFinishMkdir = onFinishMkdir;
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
        }
        return rootSmbFile;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(String... strings) {

        SmbFile smbFile;
        try {
            smbFile = getSmbFile();
            if (!smbFile.exists()) {
                smbFile.mkdir();
            }
            return true;
        } catch (SmbException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if (onFinishMkdir!=null) {
            if (aBoolean) {
                onFinishMkdir.onSuccess();
            } else {
                onFinishMkdir.onFail();
            }
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }
}
