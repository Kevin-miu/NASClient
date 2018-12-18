package client.nas.find.com.nasclient.util;

import java.net.MalformedURLException;
import java.util.Comparator;

import client.nas.find.com.nasclient.bean.FileType;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * @author Kevin-
 * @time 20181210
 * @description SMB文件共享工具
 * @updateTime 20181210
 */

public class SmbFileUtil {

    private static String cacheDir = "cache";

    private static String mIp;
    private static String mUsername;
    private static String mPasswd;

    private SmbFile fetchSmbFile(String smbPath) {

        SmbFile rootSmbFile = null;
        try {
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(mIp, mUsername, mPasswd);
            rootSmbFile = new SmbFile(smbPath, auth);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return rootSmbFile;
    }

    /**
     * 使用该工具类前，必须初始化
     *
     * @param ip
     * @param username
     * @param passwd
     */
    public static void init(String ip, String username, String passwd) {
        mIp = ip;
        mUsername = username;
        mPasswd = passwd;
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


    /**
     * 打开图片资源
     *
     * @param smbPath
     */
    //    public static void openImage(Context context,String smbPath) {
    //
    //        //1. 先缓存
    //
    //
    //
    //        //2. 然后在本地中打开
    //        Uri path = Uri.fromFile(file);
    //        Intent intent = new Intent(Intent.ACTION_VIEW);
    //        intent.addCategory("android.intent.category.DEFAULT");
    //        intent.setDataAndType(path, "image/*");
    //        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    //        context.startActivity(intent);
    //    }

}
