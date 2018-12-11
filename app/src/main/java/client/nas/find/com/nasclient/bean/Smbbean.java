package client.nas.find.com.nasclient.bean;

/**
 * @author Kevin-
 * @time 20181211
 * @description 进行smb连接的时候所需的基本信息
 * @updateTime 20181211
 */

public class Smbbean {

    private String ip;
    private String group;
    private String username;
    private String passwd;
    private String folder;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }
}
