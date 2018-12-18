package client.nas.find.com.nasclient.task;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;

/**
 * @author Kevin-
 * @time
 * @description
 * @updateTime
 */

public class SmbUploadTask extends AsyncTask<Object, Integer, Boolean> {

    private int hasRead = 0;

    private String localPath;
    private String smbPath;
    private String username;
    private String passwd;
    private String ip;

    private OnFinishUpload onFinishUpload;

    public SmbUploadTask(String smbPath, String localPath, String username, String passwd,
                         String ip, Context context, OnFinishUpload onFinishUpload) {
        this.smbPath = smbPath;
        this.localPath = localPath;
        this.username = username;
        this.passwd = passwd;
        this.ip = ip;
        this.onFinishUpload = onFinishUpload;
    }

    public interface OnFinishUpload {
        void onStart();

        void onSuccess();

        void onFail();

        void onProgress(int progress);
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
        if (onFinishUpload != null) {
            onFinishUpload.onStart();
        }

    }

    @Override
    protected Boolean doInBackground(Object... objects) {

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            File localFile = new File(localPath);
            inputStream = new FileInputStream(localFile);
            SmbFile smbFile = getSmbFile();
            smbFile.connect();
            outputStream = new SmbFileOutputStream(smbFile);
            byte[] buffer = new byte[10240];
            int len = 0; // 读取长度
            long uploadSize = 0; //上传长度
            long totalSize = inputStream.available(); //总长度
            while ((len = inputStream.read(buffer, 0, buffer.length)) != -1) {
                outputStream.write(buffer, 0, len);
                uploadSize += len;
                //Log.i("msg", "upload" + uploadSize);
                hasRead = (int) (uploadSize / (float) totalSize) * 100;
                publishProgress(hasRead);
            }

            // 刷新缓冲的输出流
            outputStream.flush();

            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean b) {
        super.onPostExecute(b);
        if (onFinishUpload != null) {
            if (b) {
                onFinishUpload.onSuccess();
            } else {
                onFinishUpload.onFail();
            }
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (values != null && values.length > 0) {
            if (onFinishUpload != null) {
                onFinishUpload.onProgress(values[0]);
            }
        }
    }


}
