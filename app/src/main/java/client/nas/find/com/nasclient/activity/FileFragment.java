package client.nas.find.com.nasclient.activity;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import client.nas.find.com.nasclient.R;
import client.nas.find.com.nasclient.adapter.FileAdapter;
import client.nas.find.com.nasclient.adapter.TitleAdapter;
import client.nas.find.com.nasclient.adapter.base.RecyclerViewAdapter;
import client.nas.find.com.nasclient.adapter.holder.FileHolder;
import client.nas.find.com.nasclient.bean.FileBean;
import client.nas.find.com.nasclient.bean.FileType;
import client.nas.find.com.nasclient.bean.TitlePathBean;
import client.nas.find.com.nasclient.task.LocalTask;
import client.nas.find.com.nasclient.task.SmbTask;
import client.nas.find.com.nasclient.util.LocalFileUtil;

/**
 * @author Kevin-
 * @time 20181206
 * @description 文件Fragment
 * @updateTime 20181206
 */

public class FileFragment extends Fragment {

    //定义相关控件
    private View view;
    private Context context;
    private HomeActivity mHomeActivity;
    private RecyclerView titleRecylerView;
    private RecyclerView fileRecylerView;
    private LinearLayout noFileContainer;

    //定义适配器
    private TitleAdapter mTitleAdapter;
    private FileAdapter mFileAdapter;

    //自定义变量（辅助）
    private File rootFile;
    private String rootPath;

    private String username;
    private String passwd;
    private String ip;

    private final String smbRootPath = "smb://OPENWRT/FamilyCloud/";
    private String smbFullPath;

    private List<FileBean> fileBeanList;

    private CloudFragment cloudFragment;

    /**
     * 设置context
     *
     * @param context
     */
    public void setContext(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_file, container, false);

        initView();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //强制将本fragment与HomeActivity绑定，但是存在内存泄露的风险
        this.mHomeActivity = (HomeActivity) context;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        //解绑,存在内存泄露的风险
        //this.mHomeActivity = null;
    }

    /**
     * 初始化操作
     */
    private void initView() {

        cloudFragment = new CloudFragment();
        cloudFragment.setContext(context);

        //设置titleRecylerView属性
        titleRecylerView = view.findViewById(R.id.title_recycler);
        titleRecylerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        mTitleAdapter = new TitleAdapter(context, new ArrayList<TitlePathBean>());
        titleRecylerView.setAdapter(mTitleAdapter);

        //设置fileRecylerView
        fileRecylerView = view.findViewById(R.id.file_recycler);
        fileRecylerView.setLayoutManager(new LinearLayoutManager(context));
        mFileAdapter = new FileAdapter(context, fileBeanList);
        fileRecylerView.setAdapter(mFileAdapter);

        noFileContainer = view.findViewById(R.id.no_file_container);

        //文件适配器设置单击监听事件
        mFileAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                if (viewHolder instanceof FileHolder) {
                    FileBean fileBean = fileBeanList.get(position);
                    FileType fileType = fileBean.getFileType();

                    if (fileType == FileType.directory) {
                        getSmbFiles(fileBean.getPath());

                        refreshTitleState(fileBean.getName(), fileBean.getPath());

                    } else if (fileType == FileType.apk) {
                        //安装app
                        LocalFileUtil.openAppIntent(context, new File(fileBean.getPath()));
                    } else if (fileType == FileType.image) {
                        //打开图片
                        LocalFileUtil.openImageIntent(context, new File(fileBean.getPath()));
                    } else if (fileType == FileType.txt) {
                        //打开txt文档
                        LocalFileUtil.openTextIntent(context, new File(fileBean.getPath()));
                    } else if (fileType == FileType.music) {
                        //打开音乐
                        LocalFileUtil.openMusicIntent(context, new File(fileBean.getPath()));
                    } else if (fileType == FileType.video) {
                        //打开视频
                        LocalFileUtil.openVideoIntent(context, new File(fileBean.getPath()));
                    } else {
                        //打开应用资源
                        LocalFileUtil.openApplicationIntent(context, new File(fileBean.getPath()));
                    }
                }
            }
        });

        //文件适配器设置长点击监听事件
        mFileAdapter.setOnItemLongClickListener(new RecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                if (viewHolder instanceof FileHolder) {
                    FileBean fileBean = (FileBean) mFileAdapter.getItem(position);
                    FileType fileType = fileBean.getFileType();

                    if (fileType != null && fileType != FileType.directory) {
                        LocalFileUtil.sendFile(context, new File(fileBean.getPath()));
                    }
                }
                return false;
            }
        });

        mTitleAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                TitlePathBean titlePathBean = (TitlePathBean) mTitleAdapter.getItem(position);

                getSmbFiles(titlePathBean.getPath());

                int count = mTitleAdapter.getItemCount();
                int removeCount = count - position - 1;
                for (int i = 0; i < removeCount; i++) {
                    mTitleAdapter.removeLast();
                }
            }
        });

        //获取上一个fragment传入的数据
        username = getArguments().getString("username");
        passwd = getArguments().getString("passwd");
        ip = getArguments().getString("ip");
        //        username = "wifish";
        //        passwd = "findlab404";
        //        ip = "192.168.1.1";

        refreshTitleState("FamilyCloud", smbRootPath);

        smbFullPath = smbRootPath;

        getSmbFiles(smbFullPath);

    }

    /**
     * 刷新根目录列表
     *
     * @param title
     * @param path
     */
    private void refreshTitleState(String title, String path) {
        TitlePathBean titlePathBean = new TitlePathBean();
        titlePathBean.setNameState(title + " > ");
        titlePathBean.setPath(path);
        mTitleAdapter.addItem(titlePathBean);
        titleRecylerView.smoothScrollToPosition(mTitleAdapter.getItemCount());
    }

    /**
     * 获取本地根目录里的文件
     *
     * @param path
     */
    public void getFiles(String path) {

        rootFile = new File(path + File.separator);

        LocalTask localTask = new LocalTask(rootFile);

        //重写回调接口，在MyTask中完成对UI的操作
        localTask.setMyTaskUIOperation(new LocalTask.MyTaskUIOperation() {
            @Override
            public void setFileBeanListByMyTask(List<FileBean> list) {
                fileBeanList = list;
            }

            @Override
            public void setEmptyContainerByMyTask() {
                //如果没有文件则显示空布局
                if (fileBeanList.size() > 0) {
                    noFileContainer.setVisibility(View.GONE);
                } else {
                    noFileContainer.setVisibility(View.VISIBLE);
                }
                mFileAdapter.refresh(fileBeanList);
            }
        });

        localTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
    }

    /**
     * 获取smb文件根目录的文件
     */
    public void getSmbFiles(String smbPath) {

        SmbTask smbTask = new SmbTask(smbPath, username, passwd, ip);

        smbTask.setMyTaskUIOperation(new SmbTask.MyTaskUIOperation() {
            @Override
            public void setFileBeanListByMyTask(List<FileBean> list) {
                fileBeanList = list;
            }

            @Override
            public void setEmptyContainerByMyTask() {
                //如果没有文件则显示空布局
                if (fileBeanList.size() > 0) {
                    noFileContainer.setVisibility(View.GONE);
                } else {
                    noFileContainer.setVisibility(View.VISIBLE);
                }
                mFileAdapter.refresh(fileBeanList);
            }
        });

        smbTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");

    }

    /**
     * 在该fragment中回退到上一页
     */
    public void back() {

        List<TitlePathBean> titlePathList = (List<TitlePathBean>) mTitleAdapter.getAdapterData();
        if (titlePathList.size() == 1) {
            //跳回cloudfragment
            mHomeActivity.getFragmentManager().beginTransaction().replace(R.id.container_layout, cloudFragment).commit();
            mHomeActivity = null;
        } else {
            //跳回上一页
            mTitleAdapter.removeItem(titlePathList.size() - 1);
            getSmbFiles(titlePathList.get(titlePathList.size() - 1).getPath());
        }
    }
}
