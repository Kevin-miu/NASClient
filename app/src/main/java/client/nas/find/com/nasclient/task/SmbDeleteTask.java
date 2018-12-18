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

public class SmbDeleteTask extends AsyncTask<String, Integer, Boolean> {

    private String smbPath;
    private String username;
    private String passwd;
    private String ip;
    private OnFinishDelete onFinishDelete;

    public interface OnFinishDelete {
        void onSuccess();

        void onFail();
    }

    public SmbDeleteTask(String smbPath, String username, String passwd, String ip, OnFinishDelete onFinishDelete) {

        this.smbPath = smbPath;
        this.username = username;
        this.passwd = passwd;
        this.ip = ip;
        this.onFinishDelete = onFinishDelete;
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

        SmbFile SmbFile;
        try {
            SmbFile = getSmbFile();
            if (SmbFile.exists()) {
                SmbFile.delete();
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
        if (onFinishDelete != null) {
            if (aBoolean) {
                onFinishDelete.onSuccess();
            } else {
                onFinishDelete.onFail();
            }
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }
}
