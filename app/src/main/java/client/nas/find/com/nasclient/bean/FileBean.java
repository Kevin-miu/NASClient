package client.nas.find.com.nasclient.bean;

/**
 * @author Kevin-
 * @time
 * @description
 * @updateTime
 */

public class FileBean {

    private String name; //文件名
    private String path; //文件路径
    private FileType fileType; //文件类型
    private int childCount ; //子目录文件数量
    private long size ; //文件大小（对目录无效）
    private int holderType ; //

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getHolderType() {
        return holderType;
    }

    public void setHolderType(int holderType) {
        this.holderType = holderType;
    }
}
