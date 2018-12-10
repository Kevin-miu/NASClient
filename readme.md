# NAS客户端

本项目是一个基于android的NAS客户端，主要包括以下三个功能：

- 局域网设备IP扫描
- SMB文件共享客户端

## 使用须知

本项目所使用的其他依赖如下，请自行添加

```java
implementation 'com.android.support:support-v4:26.1.0'
implementation 'com.yanzhenjie:permission:1.0.4'
implementation files('libs/jcifs-1.3.19.jar')
implementation 'com.github.bumptech.glide:glide:3.7.0'
```

## 局域网设备IP扫描

局域网IP扫描的基本原理就是：

1. 设备连接局域网，获取本机的IP地址和子网掩码，通过IP地址和子网掩码计算出网络号（网段）

2. 设置主机号从1到最大，计算其个数N

3. 新建N个线程，每个线程去ping 所设置主机号的IP地址，如果能够ping通则代表局域网存在该IP

4. 线程运行的过程中，注意主线程的等待，否则主线程先运行完毕则出错

## SMB文件共享客户端

通过支持openWrt操作系统的路由器搭载U盘，可以搭建私人文件共享NAS。

基于samba协议搭建NAS文件共享服务端的方法亲供参考[该博客](https://blog.csdn.net/a791693310/article/details/84584680)

有了服务端就应该有客户端。在安卓平台上，ES文件浏览器的局域网功能可以暂时充当smb客户端，但如果要自行开发的话就必须使用smb的java库`jcift-1.3.19.jar`（此库已包含在项目源码中）

jcifs的开发方法类似java文件操作功能，它的资源url定位：smb://{user}:{password}@{host}/{path}，

其中，smb为协议名，user和password分别为共享文件机子的登陆名和密码，@后面是要访问的资源的主机名或IP地址。最后是资源的共享文件夹名称和共享资源名。

例如smb://administrator:122122@192.168.0.22/test/response.txt。

注意：如果密码中有像“@”这种特殊字符的情况，就要通过

```java
NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("192.168.1.103", "Administrator", "19921103");
```

进行登录验证。

SMB文件的获取、新建、写入、删除操作均很简单，看代码就能懂，不作详述。

## 使用效果