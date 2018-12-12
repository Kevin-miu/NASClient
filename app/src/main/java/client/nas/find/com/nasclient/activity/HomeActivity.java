package client.nas.find.com.nasclient.activity;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import java.util.ArrayList;
import java.util.List;

import client.nas.find.com.nasclient.R;
import client.nas.find.com.nasclient.util.CommomUtil;

/**
 * @author Kevin-
 * @time 20181130
 * @description 主页面activity，作为3个fragment的容器
 * @updateTime
 */

public class HomeActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE_WRITE_EXTERNAL_STORAGE = 100;
    private static final String CURRENT_FRAGMENT = "STATE_FRAGMENT_SHOW";

    CloudFragment cloudFragment;
    PointFragment pointFragment;
    SettingFragment settingFragment;
    FileFragment fileFragment;

    private Fragment currentFragment;
    private FragmentManager manager;
    private String[] fragmentTagList = {"CloudFragment", "PointFragment", "SettingFragment", "FileFragment"};
    private List<Fragment> fragmentList = new ArrayList<>();

    private int currentIndex = 0;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.navigation_cloud:
                    switchFragment(fragmentList.get(0), fragmentTagList[0]);
                    return true;
                case R.id.navigation_point:
                    switchFragment(fragmentList.get(1), fragmentTagList[1]);
                    return true;
                case R.id.navigation_setting:
                    switchFragment(fragmentList.get(2), fragmentTagList[2]);
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
        CommomUtil.init(HomeActivity.this);

        //申请权限，参考https://github.com/Kevin-miu/MPermission
        //由于涉及访问和写入存储空间和SD卡，在android6.0之后需要现实申请权限，否则报错
        AndPermission.with(this).requestCode(PERMISSION_CODE_WRITE_EXTERNAL_STORAGE)
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE).send();

        // 下方菜单栏监听事件，已经重写
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //获取manager
        manager = getFragmentManager();

        //内存重启时调用
        if (savedInstanceState != null) {

            //获取到“内存重启”时保留下来的索引
            currentIndex = savedInstanceState.getInt(CURRENT_FRAGMENT, 0);
            reView(currentIndex);

        } else {
            initView();
        }

    }


    /**
     * 初始化
     */
    private void initView() {

        // 下方菜单栏监听事件，已经重写
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        cloudFragment = new CloudFragment();
        pointFragment = new PointFragment();
        settingFragment = new SettingFragment();
        fileFragment = null;
        //fileFragment = new FileFragment();

        cloudFragment.setContext(HomeActivity.this);
        pointFragment.setContext(HomeActivity.this);
        settingFragment.setContext(HomeActivity.this);
        //fileFragment.setContext(HomeActivity.this);

        cloudFragment.setOnSwitchFragment(new CloudFragment.OnSwitchFragment() {
            @Override
            public void switchFragmentToFileFragment(Fragment fragment) {
                //更新fragmentList
                fileFragment = (FileFragment) fragment;

                fileFragment.setOnSwitchFragment(new FileFragment.OnSwitchFragment() {
                    @Override
                    public void switchFragmentTocCloudFragment() {
                        switchFragment(fragmentList.get(0), fragmentTagList[0]);
                    }
                });

                fragmentList.add(3, fileFragment);

                switchFragment(fragmentList.get(3), fragmentTagList[3]);
            }
        });

        fragmentList.add(0, cloudFragment);
        fragmentList.add(1, pointFragment);
        fragmentList.add(2, settingFragment);
        //fragmentList.add(3, fileFragment);

        //获取当前（默认）的fragment
        currentFragment = fragmentList.get(0);

        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.container_layout, currentFragment, fragmentTagList[0]);
        transaction.commitAllowingStateLoss();

    }


    /**
     * 当内存重启的时候，调用该方法
     *
     * @param index
     */
    private void reView(int index) {

        fragmentList.removeAll(fragmentList);

        fragmentList.add(manager.findFragmentByTag(fragmentTagList[0]));
        fragmentList.add(manager.findFragmentByTag(fragmentTagList[1]));
        fragmentList.add(manager.findFragmentByTag(fragmentTagList[2]));
        fragmentList.add(manager.findFragmentByTag(fragmentTagList[3]));

        FragmentTransaction transaction = manager.beginTransaction();

        for (int i = 0; i < fragmentList.size(); i++) {
            if (i == index) {
                transaction.show(fragmentList.get(i));
            } else {
                transaction.hide(fragmentList.get(i));
            }
        }

        transaction.commitAllowingStateLoss();
        currentFragment = fragmentList.get(index);

    }

    /**
     * 切换fragment
     *
     * @param to
     * @param tag
     */
    private void switchFragment(Fragment to, String tag) {
        if (currentFragment != to) {
            FragmentTransaction transaction = manager.beginTransaction();

            if (!(currentFragment instanceof FileFragment)) {
                if (!to.isAdded() ) {
                    //没有被添加
                    //隐藏当前的，添加新的，显示新的
                    transaction.hide(currentFragment).add(R.id.container_layout, to, tag).show(to);
                } else {
                    //已经添加
                    //隐藏当前的，显示新的
                    transaction.hide(currentFragment).show(to);
                }
            } else {
                //如果当前fragment是fileFragment的话就移除
                if (!to.isAdded() ) {
                    //没有被添加
                    //隐藏当前的，添加新的，显示新的
                    transaction.remove(currentFragment).add(R.id.container_layout, to, tag).show(to);
                } else {
                    //已经添加
                    //隐藏当前的，显示新的
                    transaction.remove(currentFragment).show(to);
                }
            }
            currentFragment = to;
            transaction.commitAllowingStateLoss();
        }
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
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        //activity在销毁时让fragment也跟着销毁
        outState.putInt(CURRENT_FRAGMENT, currentIndex);
        //super.onSaveInstanceState(outState, outPersistentState);

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
