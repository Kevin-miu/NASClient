package client.nas.find.com.nasclient.other;

import android.util.Log;

/**
 * @author Kevin-
 * @time 20181204
 * @description 日志打印类（辅助调试，暂时没有）
 * @updateTime 20181204
 */

public class SimpleLogger implements IpScanner.ScannerLogger {

    @Override
    public void onScanLogPrint(String msg) {
        Log.v("cdw", ">>>" + msg);
    }
}
