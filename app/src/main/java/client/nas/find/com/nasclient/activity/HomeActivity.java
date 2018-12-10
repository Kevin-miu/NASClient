package client.nas.find.com.nasclient.activity;

import android.Manifest;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import java.util.List;

import client.nas.find.com.nasclient.R;
import client.nas.find.com.nasclient.util.Util;

/**
 * @author Kevin-
 * @time 20181130
 * @description 主页面activity，作为3个fragment的容器
 * @updateTime
 */

public class HomeActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE_WRITE_EXTERNAL_STORAGE = 100;

    CloudFragment cloudFragment;
    PointFragment pointFragment;
    SettingFragment settingFragment;
    FileFragment fileFragment;
    Fragment currentFragment;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.navigation_cloud:
                    //Toast.makeText(HomeActivity.this, "点击云盘", Toast.LENGTH_LONG).show();
                    //禁止返回键退出
                    //getFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.container_layout, cloudFragment).commit();
                    getFragmentManager().beginTransaction().replace(R.id.container_layout, cloudFragment).commit();
                    return true;
                case R.id.navigation_point:
                    //Toast.makeText(HomeActivity.this, "点击积分", Toast.LENGTH_LONG).show();
                    getFragmentManager().beginTransaction().replace(R.id.container_layout, pointFragment).commit();
                    return true;
                case R.id.navigation_setting:
                    //Toast.makeText(HomeActivity.this, "点击设置", Toast.LENGTH_LONG).show();
                    getFragmentManager().beginTransaction().replace(R.id.container_layout, settingFragment).commit();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //初始化Util
        Util.init(HomeActivity.this);

        //申请权限，参考https://github.com/Kevin-miu/MPermission
        //由于涉及访问和写入存储空间和SD卡，在android6.0之后需要现实申请权限，否则报错
        AndPermission.with(this).requestCode(PERMISSION_CODE_WRITE_EXTERNAL_STORAGE)
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE).send();

        //初始化控件
        initView();

    }


    /**
     * 初始化
     */
    private void initView() {

        cloudFragment = new CloudFragment();
        pointFragment = new PointFragment();
        settingFragment = new SettingFragment();

        //设置Context
        cloudFragment.setContext(HomeActivity.this);
        pointFragment.setContext(HomeActivity.this);
        settingFragment.setContext(HomeActivity.this);

        fileFragment = new FileFragment();
        fileFragment.setContext(HomeActivity.this);

        // 下方菜单栏监听事件，已经重写
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //设置初始Fragment
        getFragmentManager().beginTransaction().replace(R.id.container_layout, cloudFragment).commit();

    }


    /**
     * 手机按键的监听事件
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            //按下返回键
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                Log.e("Tag", "点击了返回键");
                //确认当前处于哪个Fragment
                currentFragment = getFragmentManager().findFragmentById(R.id.container_layout);

                if (currentFragment instanceof CloudFragment) {
                    cloudFragment.exit();
                }

                if (currentFragment instanceof PointFragment) {
                    pointFragment.exit();
                }

                if (currentFragment instanceof SettingFragment) {
                    settingFragment.exit();
                }

                if (currentFragment instanceof FileFragment) {
                    fileFragment.back();
                }

                return true;

            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 只需要调用这一句，其它的交给AndPermission吧，最后一个参数是PermissionListener。
        AndPermission.onRequestPermissionsResult(requestCode, permissions, grantResults, listener);
    }

    private PermissionListener listener = new PermissionListener() {
        @Override
        public void onSucceed(int requestCode, List<String> grantedPermissions) {
            // 权限申请成功回调。
            if (requestCode == PERMISSION_CODE_WRITE_EXTERNAL_STORAGE) {

            }
        }

        @Override
        public void onFailed(int requestCode, List<String> deniedPermissions) {
            // 权限申请失败回调。
            AndPermission.defaultSettingDialog(HomeActivity.this, PERMISSION_CODE_WRITE_EXTERNAL_STORAGE)
                    .setTitle("权限申请失败")
                    .setMessage("我们需要的一些权限被您拒绝或者系统发生错误申请失败，请您到设置页面手动授权，否则功能无法正常使用！")
                    .setPositiveButton("好，去设置")
                    .show();
        }
    };

}
