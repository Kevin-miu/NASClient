package client.nas.find.com.nasclient.activity.base;

import android.app.Application;

/**
 * @author Kevin-
 * @time
 * @description
 * @updateTime
 */

public class NASApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //必须jcifs.smb.client.dfs.disabled 这个属性一定记得设置true，其默认值是false，不然连接会非常非常的慢。
        // jcifs.smb.client.dfs.soTimeout、jcifs.smb.client.responseTimeout 这两个属性可以设的稍微大点，避免网络不稳定带来的连接中断。
        System.setProperty("jcifs.smb.client.dfs.disabled", "true");
        System.setProperty("jcifs.smb.client.soTimeout", "1000000");
        System.setProperty("jcifs.smb.client.responseTimeout", "30000");
    }
}
