package client.nas.find.com.nasclient.other;

import android.os.SystemClock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Kevin-
 * @time 20181204
 * @description 局域网内IP发现（该类有错，暂时没有）
 * @updateTime 20181204
 */

public class IpScanner extends Thread {

    private int mPort = -1;  //启动端口
    private ScannerLogger mScannerLogger = null; //扫描日志
    private String mCommandStr = "ifconfig";  //命令行指令
    private ScanCallback mScanCallback = null;  //扫描回调（找到/找不到）
    private static final String LO_IP = "127.0.0.1"; //本地IP
    private Vector<String> mLocalNetIps = new Vector<>(); //局域网设备IP集合
    private Set<String> mResult = new HashSet<>();  //局域网在线设备IP集合（扫描之后的结果）
    private AtomicInteger mLocalNetCount = new AtomicInteger(0); //局域网设备IP的数量
    private AtomicBoolean mCallbackCalled = new AtomicBoolean(false);  //回调结果
    private int mExpendThreadNumber = 0;   //手动设置的线程数量
    private long mTimeOut = 1000l;    //手动设置的连接超时
    private long start;  //起始时间

    /**
     * 构造函数
     *
     * @param port     启动端口
     * @param callback 扫描回调
     */
    public IpScanner(int port, ScanCallback callback) {
        mPort = port;
        if (callback == null) {
            throw new IllegalArgumentException("Params callback can't be null!");
        }
        mScanCallback = callback;
    }

    /**
     * 设置连接超时
     *
     * @param time
     * @return
     */
    public IpScanner setTimeOut(long time) {
        this.mTimeOut = time;
        return this;
    }

    /**
     * 设置连接线程数量
     *
     * @param number
     * @return
     */
    public IpScanner setExpendThreadNumber(int number) {
        this.mExpendThreadNumber = number;
        return this;
    }

    /**
     * 设置命令行执行指令
     *
     * @param command
     * @return
     */
    public final IpScanner setCommandLine(String command) {
        if (command == null) {
            return this;
        }
        this.mCommandStr = command;
        return this;
    }

    /**
     * 定义（静态）回调接口，必须实现以下回调方法
     */
    public static interface ScanCallback {
        public void onFound(Set<String> ip, String hostIp, int port);

        public void onNotFound(String hostIp, int port);
    }

    /**
     * 设置扫描日志对象
     *
     * @param logger
     * @return
     */
    public IpScanner setScannerLogger(ScannerLogger logger) {
        this.mScannerLogger = logger;
        return this;
    }

    /**
     * 打印日志（内部方法）
     *
     * @param log
     */
    private void printLog(String log) {
        if (this.mScannerLogger != null) {
            this.mScannerLogger.onScanLogPrint(log);
        }
    }

    /**
     * 静态内部接口
     */
    public static interface ScannerLogger {
        public void onScanLogPrint(String msg);
    }

    /**
     * 开始扫描
     */
    public void startScan() {
        //记录起始时间
        start = SystemClock.uptimeMillis();
        if (this.mPort > 0 && mScanCallback != null) {
            //线程启动
            this.start();
        }
    }

    /**
     * 判断是否为局域网地址（内部方法）
     *
     * @param firstWord IP地址第一网段
     * @return
     */
    private boolean isLocalServer(String firstWord) {
        if ("10".equals(firstWord) || "192".equals(firstWord) || "172".equals(firstWord)) {
            return true;
        }
        return false;
    }

    /**
     * 静态内部类：IP操作
     */
    private static class Ip {
        int position = -1;
        int[] addr = new int[4];

        //分步骤添加IP地址各个网段
        public void push(String addrWord) {
            position++;
            if (position >= 4) {
                throw new IllegalArgumentException("Ip only 4 addr word");
            }
            addr[position] = Integer.parseInt(addrWord);
        }

        //整型转成字符串
        public String toIpString(int v) {
            StringBuilder builder = new StringBuilder();
            int p = ((v & 0xff000000) >> 24);
            if (p < 0) {
                p += 256;
            }
            builder.append(p).append('.')
                    .append((v & 0x00ff0000) >> 16).append('.')
                    .append((v & 0x0000ff00) >> 8).append('.')
                    .append((v & 0x000000ff))
            ;
            return builder.toString();
        }

        //IP地址转成整型数据
        public int toInt() {
            if (!isFull()) {
                throw new IllegalArgumentException("Ip only 4 addr word");
            }
            int v = 0;
            v |= (addr[0] & 0xff) << 24;
            v |= (addr[1] & 0xff) << 16;
            v |= (addr[2] & 0xff) << 8;
            v |= (addr[3] & 0xff);
            return v;
        }

        //重设
        public void reset() {
            this.position = -1;
        }

        //判断是否put够4个网段
        public boolean isFull() {
            return this.position == 3;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (int p = 0; p <= 3; p++) {
                builder.append(addr[p]);
                if (p < 3) {
                    builder.append('.');
                }
            }
            return builder.toString();
        }
    }

    /**
     * 静态内部类：子网掩码操作
     */
    private static class Mask {
        int position = -1;
        int[] mask = new int[4];

        //分步骤添加子网掩码各个字段，共4个
        public void push(String maskWord) {
            position++;
            if (position >= 4) {
                throw new IllegalArgumentException("Mask code only 4 mask word");
            }
            mask[position] = Integer.parseInt(maskWord);
        }

        //将子网掩码转成整型
        public int toInt() {
            if (!isFull()) {
                throw new IllegalArgumentException("Ip only 4 addr word");
            }
            int v = 0;
            v |= (mask[0] & 0xff) << 24;
            v |= (mask[1] & 0xff) << 16;
            v |= (mask[2] & 0xff) << 8;
            v |= (mask[3] & 0xff);
            return v;
        }

        //将16进制显示转成10进制显示
        private int getValueByChar(char c) {
            if (c >= '0' && c <= '9') {
                return c - '0';
            }
            if (c >= 'a' && c <= 'f') {
                return 10 + (c - 'a');
            }
            return 0;
        }

        //将两个16进制的字符移位相加（拼接）
        private int parserInt(char c1, char c2) {
            int sum = getValueByChar(c1);
            sum = sum << 4;
            sum += getValueByChar(c2);
            return sum;
        }

        //解析16进制，得到分网段显示的子网掩码
        public void parserHex(String hex) {
            char[] code = new char[2];
            int p;
            for (int index = 0; index < 8; index++) {
                code[0] = hex.charAt(index);
                p = index >> 1;
                index++;
                code[1] = hex.charAt(index);
                mask[p] = parserInt(code[0], code[1]);
            }
            position = 3;
        }

        //重设
        public void reset() {
            this.position = -1;
        }

        //判断子网掩码是否够4个字段
        public boolean isFull() {
            return this.position == 3;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (int p = 0; p <= 3; p++) {
                builder.append(mask[p]);
                if (p < 3) {
                    builder.append('.');
                }
            }
            return builder.toString();
        }
    }

    /**
     * 从输入流中解析出IP和子网掩码
     *
     * @param is   输入流
     * @param ip   Ip对象
     * @param mask Mask对象
     */
    private void parser(InputStream is, Ip ip, Mask mask) {
        BufferedReader reader = null;
        try {
            //封装buffer
            reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder ipBuilder = new StringBuilder();
            StringBuilder maskBuilder = new StringBuilder();
            String firstWord = null;
            String word = null;
            //按照行读取
            String line = reader.readLine();
            while (line != null) {
                line = line.toLowerCase();
                //从文本中解析IP
                if (!ip.isFull()) {
                    firstWord = null;
                    int index = line.indexOf("inet ");
                    if (index >= 0) {
                        //ip在inet 后面 ，可在linux中输入ifconfig查看
                        index += "inet ".length();
                        char c;
                        while (index < line.length()) {
                            c = line.charAt(index);
                            //读取到空格或非数字，下一步
                            if (ipBuilder.length() == 0 && (Character.isWhitespace(c) || !Character.isDigit(c))) {
                                index++;
                                continue;
                            } else if (c == '.') {
                                //读取到.符，说明至少存入一个字段，根据第一字段判断是否为局域网IP地址
                                word = ipBuilder.toString();
                                ipBuilder.delete(0, ipBuilder.length());
                                if (firstWord == null) {
                                    firstWord = word;
                                    if (!isLocalServer(firstWord)) {
                                        break;
                                    }
                                }
                                if (ip.isFull()) {
                                    ip.reset();
                                    break;
                                }
                                ip.push(word);
                                continue;
                            } else if (!Character.isDigit(c)) {
                                //读取到不是数字，且确定缺少最后一个网段，可以确定ipBuilder存放的就是最后一个网段
                                if (ip.position == 2) {
                                    if (ipBuilder.length() <= 0) {
                                        ip.reset();
                                        break;
                                    } else {
                                        word = ipBuilder.toString();
                                        ipBuilder.delete(0, ipBuilder.length());
                                        ip.push(word);
                                    }
                                    break;
                                } else {
                                    ip.reset();
                                    ipBuilder.delete(0, ipBuilder.length());
                                    break;
                                }
                            }
                            ipBuilder.append(c);
                            index++;
                        }
                    }
                }

                word = null;
                //解析子网掩码同理
                if (ip.isFull() && !mask.isFull()) {
                    try {
                        boolean isParserByHex = false;
                        int index = line.indexOf("mask");
                        if (index >= 0) {
                            index += "mask".length();
                            char c;
                            while (index < line.length()) {
                                c = line.charAt(index);
                                if (maskBuilder.length() == 0 && (Character.isWhitespace(c) || !Character.isDigit(c))) {
                                    index++;
                                    continue;
                                } else if (Character.isDigit(c)) {
                                    if (mask.position < 0 && maskBuilder.length() == 0) {
                                        if (c == '0') { //parse by hex
                                            index++;
                                            c = line.charAt(index);
                                            if (c == 'x') {
                                                int start = index + 1;
                                                maskBuilder.append(line.substring(start, start + "ffffffff".length()));
                                                isParserByHex = true;
                                                break;
                                            } else {
                                                maskBuilder.delete(0, maskBuilder.length());
                                                mask.reset();
                                                break;
                                            }
                                        } else {
                                            isParserByHex = false;
                                        }
                                    }
                                } else if ('.' == c) {
                                    word = maskBuilder.toString();
                                    maskBuilder.delete(0, maskBuilder.length());
                                    mask.push(word);
                                    continue;
                                } else if (!Character.isDigit(c)) {
                                    if (mask.position == 2) {
                                        if (maskBuilder.length() > 0) {
                                            mask.push(maskBuilder.toString());
                                            maskBuilder.delete(0, maskBuilder.length());
                                            break;
                                        } else {
                                            throw new IllegalArgumentException();
                                        }
                                    } else {
                                        throw new IllegalArgumentException();
                                    }
                                }
                                maskBuilder.append(c);
                                index++;
                            }
                            if (isParserByHex && maskBuilder.length() > 0) {
                                mask.parserHex(maskBuilder.toString());
                                break;
                            }
                        }
                    } catch (Exception e) {
                        maskBuilder.delete(0, maskBuilder.length());
                        mask.reset();
                    }
                }
                if (ip.isFull() && mask.isFull()) {
                    break;
                }
                line = reader.readLine();
            }
        } catch (Exception e) {
            //TODO
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            //1.执行命令：ifconfig
            Process process = Runtime.getRuntime().exec(mCommandStr);
            Ip ip = new Ip();
            Mask mask = new Mask();
            String ipHost = null;
            //2.解析命令的输出结果，得到ip和mask子网掩码
            parser(process.getInputStream(), ip, mask);
            ipHost = ip.toString();
            if (ip.isFull() && mask.isFull()) {
                printLog("host ip:" + ipHost);
                printLog("host mask:" + mask.toString());
                int v = mask.toInt();
                int vip = ip.toInt();
                //3.根据ip和子网掩码获取网段
                int begin = (vip & v);
                v = ~v;
                int ipValue;
                for (int index = 2; index < v; index++) {
                    ipValue = begin + index;
                    if (ipValue == vip) {
                        continue;
                    } else {
                        //4.获取到本地所有ip（2~255）
                        String ipStr = ip.toIpString(ipValue);
                        mLocalNetIps.add(ipStr);
                    }
                }
                //5.启动多线程，用本机去访问局域网所有IP
                dispatchThreads(ipHost);
            }
        } catch (Exception e) {
            if (mScanCallback != null) {
                this.mScanCallback.onNotFound(LO_IP, this.mPort);
            }
        }
    }

    /**
     * 为每一个线程设置访问通道，全部都是由本机IP发起请求
     *
     * @param iphost
     */
    private void dispatchThreads(String iphost) {

        //1.设置主selector
        SelectChannelAction mainSelectAction = new SelectChannelAction(iphost, this.mPort, 0);
        //2.按照线程数，设置其他selector
        SelectChannelAction[] actions = new SelectChannelAction[mExpendThreadNumber + 1];
        actions[0] = mainSelectAction;
        for (int index = 1; index < mExpendThreadNumber + 1; index++) {
            actions[index] = new SelectChannelAction(iphost, this.mPort, 0);
        }
        int index = 0;
        SelectChannelAction action = null;
        //3.建立socket通道
        for (String ip : mLocalNetIps) {
            index %= actions.length;
            action = actions[index];
            action.addChannel(ip);
        }

        //4.开启线程发起通道连接（注意线程数量）
        for (index = 1; index < mExpendThreadNumber + 1; index++) {
            actions[index].start();
        }
        //5.主通道统计
        mainSelectAction.run();

    }

    /**
     *
     */
    public class SelectChannelAction implements Runnable {

        String ipHost;
        int port;
        int index;
        private Map<SelectionKey, String> channels = new HashMap<>();
        private Selector selector = null;


        public SelectChannelAction(String ipHost, int port, int index) {
            this.ipHost = ipHost;
            this.port = port;
            this.index = index;
            try {
                selector = Selector.open();
            } catch (IOException e) {
            }
        }

        public void start() {
            new Thread(this, "Select#" + index).start();
        }

        public void addChannel(String ip) {
            try {
                //建立面向流的连接socket
                SocketChannel socketChannel = SocketChannel.open();
                socketChannel.configureBlocking(false);
                SelectionKey key = socketChannel.register(selector, SelectionKey.OP_CONNECT);
                //socket连接到对方的ip:port
                socketChannel.connect(new InetSocketAddress(ip, this.port));
                channels.put(key, ip);
                mLocalNetCount.incrementAndGet();
            } catch (Exception e) {
            }
        }

        @Override
        public void run() {
            long startTime = SystemClock.uptimeMillis();
            while (true) {
                try {
                    //1.获取到已建立连接通道的key，如果没有会阻塞等待
                    int key = this.selector.select(100);
                    //printLog(Thread.currentThread()+"select over "+key+":"+channels.size());
                    if (key == 0) {
                        //异常：没有发起建立连接通道，退出循环
                        if (channels.size() == 0) {
                            break;
                        } else {
                            //异常：连接超时或者（）
                            if (Math.abs(SystemClock.uptimeMillis() - startTime) > mTimeOut && channels.size() <= 2) {
                                printLog("call timeout!!!");
                                int size = channels.size() - 1;
                                channels.clear();
                                while (size >= 0) {
                                    mLocalNetCount.decrementAndGet();
                                    size--;
                                }
                                break;
                            }
                        }
                        continue;
                    }
                    //2.获取选择器的selectedKey集合
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectedKeys.iterator();
                    SelectionKey sKey = null;
                    while (iterator.hasNext()) {
                        //3.统计建立socket连接的对方的IP
                        sKey = iterator.next();
                        if (sKey.isConnectable()) {
                            SocketChannel channel = (SocketChannel) sKey.channel();
                            String ip = this.channels.remove(sKey);
                            try {
                                startTime = SystemClock.uptimeMillis();
                                if (channel.finishConnect()) {
                                    mResult.add(ip);
                                }
                                channel.close();
                            } catch (Exception e) {
                            }
                        }
                        sKey.cancel();
                        iterator.remove();
                        mLocalNetCount.decrementAndGet();
                    }
                    printLog(Thread.currentThread() + "size = " + this.channels.size());
                    if (this.channels.size() == 0) {
                        break;
                    }
                } catch (Exception e) {
                    //printLog("select exception "+e);
                }
            }
            try {
                this.selector.close();
            } catch (Exception e) {
            }
            int v = mLocalNetCount.get();
            printLog(Thread.currentThread() + " count = " + v);
            if (v == 0 && !mCallbackCalled.getAndSet(true)) {
                printLog("scan use time = " + (SystemClock.uptimeMillis() - start) / 1000 + "s");
                if (mResult.size() > 0) {
                    mScanCallback.onFound(mResult, this.ipHost, mPort);
                } else {
                    mScanCallback.onNotFound(this.ipHost, mPort);
                }
            }
            Thread.currentThread().interrupt();
        }
    }

}
