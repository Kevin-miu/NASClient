package client.nas.find.com.nasclient.task;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

/**
 * @author Kevin-
 * @time
 * @description
 * @updateTime
 */

public class SmbDownloadTask extends AsyncTask<String, Integer, Boolean> {

    private static String downloadDir = "0SMBDownload";

    private String localPath;
    private String smbPath;
    private String username;
    private String passwd;
    private String ip;

    private OnFinishDownload onFinishDownload;

    public interface OnFinishDownload {
        void onStart();

        void onSuccess();

        void onFail();

        void onProgress(int progress);
    }

    public SmbDownloadTask(String smbPath, String localPath, String username, String passwd,
                           String ip, OnFinishDownload onFinishDownload) {

        this.localPath = localPath;
        this.smbPath = smbPath;
        this.username = username;
        this.passwd = passwd;
        this.ip = ip;
        this.onFinishDownload = onFinishDownload;
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
        if (onFinishDownload != null) {
            onFinishDownload.onStart();
        }
    }

    @Override
    protected Boolean doInBackground(String... strings) {

        InputStream in = null;
        OutputStream out = null;

        try {
            SmbFile smbFile = getSmbFile();
            String fileName = smbPath.substring(smbPath.lastIndexOf("/") + 1, smbPath.length());
            File localFolder = new File(localPath + File.separator + downloadDir);
            //判断是否存在目录
            if (!localFolder.exists()) {
                localFolder.mkdir();
            }

            String downloadFileDir = localPath + File.separator + downloadDir + File.separator + fileName;
           // Log.i("msg", "downloadFileDir" + downloadFileDir);
            File localFile = new File(downloadFileDir);
            //判断文件是否存在
            if (!localFile.exists()) {
                localFile.createNewFile();
            }

            in = new BufferedInputStream(new SmbFileInputStream(smbFile));
            out = new BufferedOutputStream(new FileOutputStream(localFile));
            byte[] buffer = new byte[10240];

            long totalSize = in.available();
            int readNum = 0;
            while (in.read(buffer) != -1) {
                out.write(buffer);
                buffer = new byte[10240];

                readNum++;
                int hasRead = (int) (10240 * readNum / (float) totalSize);
                publishProgress(hasRead);
            }

            return true;
        } catch (SmbException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (onFinishDownload != null) {
            onFinishDownload.onProgress(values[0]);
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if (onFinishDownload != null) {
            if (aBoolean) {
                onFinishDownload.onSuccess();
            } else {
                onFinishDownload.onFail();
            }
        }
    }
}
