package client.nas.find.com.nasclient.bean;

/**
 * @author Kevin-
 * @time 20181203
 * @description 设备信息类
 * @updateTime 20181203
 */

public class DeviceBean {

    private String ip;
    private String hostname;
    private String username;
    private String passwd;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }


    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
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
}
