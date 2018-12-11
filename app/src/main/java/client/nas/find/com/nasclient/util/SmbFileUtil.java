package client.nas.find.com.nasclient.util;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import client.nas.find.com.nasclient.bean.FileType;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

/**
 * @author Kevin-
 * @time 20181210
 * @description SMB文件共享工具
 * @updateTime 20181210
 */

public class SmbFileUtil {

    static String path;
    static String userpwd;
    static NtlmPasswordAuthentication auth;

    public static void connecting(String pathStr, String userpwdStr) {
        path = pathStr;
        userpwd = userpwdStr;
        auth = new NtlmPasswordAuthentication(userpwd);
    }

    /**
     * 获取smb根目录文件
     *
     * @return
     */
    public static SmbFile getSmbFile() {
        SmbFile smbFile = null;

        try {
            smbFile = new SmbFile(userpwd, auth);
            Log.i("msg", "smbFile不为空");

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.i("msg", "smbFile为空");
        } finally {
            return smbFile;
        }
    }

    /**
     * 读取共享文件夹下的所有文件(文件夹)的名称
     *
     * @param remoteUrl
     */
    public static List<SmbFile> getSmbFileList(String remoteUrl) {

        SmbFile smbFile;
        List<SmbFile> fileList = null;

        try {
            // smb://userName:passWord@host/path/
            smbFile = new SmbFile(remoteUrl);
            fileList = new ArrayList<>();

            if (!smbFile.exists()) {
                Log.i("msg", "没有文件");
            } else {
                SmbFile[] files = smbFile.listFiles();
                //转换成列表list
                Collections.addAll(fileList, files);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SmbException e) {
            e.printStackTrace();
        } finally {
            return fileList;
        }
    }

    /**
     * 创建文件夹
     *
     * @param remoteUrl
     * @param folderName
     * @return
     */
    public static boolean smbMkDir(String remoteUrl, String folderName) {
        SmbFile smbFile;
        boolean flag = false;
        try {
            // smb://userName:passWord@host/path/folderName
            smbFile = new SmbFile(remoteUrl + folderName);
            if (!smbFile.exists()) {
                smbFile.mkdir();
                flag = true;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SmbException e) {
            e.printStackTrace();
        } finally {
            return flag;
        }
    }


    /**
     * 上传文件
     *
     * @param remoteUrl     smb文件协议url
     * @param smbFolderPath smb文件夹路径
     * @param localFilePath 本地文件路径
     * @param fileName      本地文件名
     */
    public static boolean uploadFileToSmbFolder(String remoteUrl, String smbFolderPath, String localFilePath, String fileName) {

        InputStream inputStream = null;
        OutputStream outputStream = null;
        boolean flag = false;

        try {
            File localFile = new File(localFilePath);
            inputStream = new FileInputStream(localFile);
            // smb://userName:passWord@host/path/shareFolderPath/fileName
            SmbFile smbFile = new SmbFile(remoteUrl + smbFolderPath + "/" + fileName);
            smbFile.connect();
            outputStream = new SmbFileOutputStream(smbFile);
            byte[] buffer = new byte[4096];
            int len = 0; // 读取长度
            while ((len = inputStream.read(buffer, 0, buffer.length)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            // 刷新缓冲的输出流
            outputStream.flush();
            flag = true;
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
            return flag;
        }
    }

    /**
     * 下载文件到指定文件夹
     *
     * @param remoteUrl
     * @param shareFolderPath
     * @param fileName
     * @param localDir
     */
    public static boolean downloadFileToFolder(String remoteUrl, String shareFolderPath, String fileName, String localDir) {

        InputStream in = null;
        OutputStream out = null;
        boolean flag = false;

        try {
            SmbFile remoteFile = new SmbFile(remoteUrl + shareFolderPath + File.separator + fileName);
            File localFile = new File(localDir + File.separator + fileName);
            in = new BufferedInputStream(new SmbFileInputStream(remoteFile));
            out = new BufferedOutputStream(new FileOutputStream(localFile));
            byte[] buffer = new byte[1024];
            while (in.read(buffer) != -1) {
                out.write(buffer);
                buffer = new byte[1024];
            }
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return flag;
        }
    }

    /**
     * 删除文件
     *
     * @param remoteUrl
     * @param shareFolderPath
     * @param fileName
     */
    public static boolean deleteFile(String remoteUrl, String shareFolderPath, String fileName) {

        SmbFile SmbFile;
        boolean flag = false;

        try {
            // smb://userName:passWord@host/path/shareFolderPath/fileName
            SmbFile = new SmbFile(remoteUrl + shareFolderPath + "/" + fileName);
            if (SmbFile.exists()) {
                SmbFile.delete();
                flag = true;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SmbException e) {
            e.printStackTrace();
        } finally {
            return flag;
        }
    }


    /**
     * 获取文件类型
     *
     * @param smbFile
     * @return
     */
    public static FileType getFileType(SmbFile smbFile) {
        String fileName;

        try {

            if (smbFile.isDirectory()) {
                return FileType.directory;
            }
            fileName = smbFile.getName().toLowerCase();
        } catch (SmbException e) {
            fileName = null;
            e.printStackTrace();
        }

        if (fileName.endsWith(".mp3")) {
            return FileType.music;
        }

        if (fileName.endsWith(".mp4") || fileName.endsWith(".avi")
                || fileName.endsWith(".3gp") || fileName.endsWith(".mov")
                || fileName.endsWith(".rmvb") || fileName.endsWith(".mkv")
                || fileName.endsWith(".flv") || fileName.endsWith(".rm")) {
            return FileType.video;
        }

        if (fileName.endsWith(".txt") || fileName.endsWith(".log") || fileName.endsWith(".xml")) {
            return FileType.txt;
        }

        if (fileName.endsWith(".zip") || fileName.endsWith(".rar")) {
            return FileType.zip;
        }

        if (fileName.endsWith(".png") || fileName.endsWith(".gif")
                || fileName.endsWith(".jpeg") || fileName.endsWith(".jpg")) {
            return FileType.image;
        }

        if (fileName.endsWith(".apk")) {
            return FileType.apk;
        }

        return FileType.other;

    }

    /**
     * 文件按照名字排序
     */
    public static Comparator comparator = new Comparator<SmbFile>() {
        @Override
        public int compare(SmbFile file1, SmbFile file2) {

            try {

                if (file1.isDirectory() && file2.isFile()) {
                    return -1;
                } else if (file1.isFile() && file2.isDirectory()) {
                    return 1;
                } else {
                    return file1.getName().compareTo(file2.getName());
                }
            } catch (SmbException e) {
                e.printStackTrace();
                return 0;
            }
        }
    };
}
