package client.nas.find.com.nasclient.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.hb.dialog.myDialog.ActionSheetDialog;

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
import client.nas.find.com.nasclient.task.SmbDeleteTask;
import client.nas.find.com.nasclient.task.SmbDownloadTask;
import client.nas.find.com.nasclient.task.SmbMkdirTask;
import client.nas.find.com.nasclient.task.SmbTask;
import client.nas.find.com.nasclient.task.SmbUploadTask;
import client.nas.find.com.nasclient.util.CommomUtil;
import client.nas.find.com.nasclient.util.FileChooseUtil;
import client.nas.find.com.nasclient.util.LocalFileUtil;
import client.nas.find.com.nasclient.util.SmbFileUtil;

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
    private RecyclerView titleRecylerView;
    private RecyclerView fileRecylerView;
    private LinearLayout noFileContainer;
    private FloatingActionButton floatBtn;
    private ProgressDialog mProgress;

    //定义适配器
    private TitleAdapter mTitleAdapter;
    private FileAdapter mFileAdapter;

    //自定义变量（辅助）
    private String username;
    private String passwd;
    private String ip;

    private static final int MAX = 100;
    private final String smbRootPath = "smb://OPENWRT/FamilyCloud/";
    private final String localRootPath = Environment.getExternalStorageDirectory().getAbsolutePath();

    private List<FileBean> fileBeanList;

    private OnSwitchFragment onSwitchFragment;

    /**
     * 设置context
     *
     * @param context
     */
    public void setContext(Context context) {
        this.context = context;
    }


    public interface OnSwitchFragment {
        void switchFragmentTocCloudFragment();
    }

    public void setOnSwitchFragment(OnSwitchFragment onSwitchFragment) {
        this.onSwitchFragment = onSwitchFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_file, container, false);

        initView();

        return view;
    }

    /**
     * 初始化操作
     */
    private void initView() {

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

        //设置悬浮按钮
        floatBtn = view.findViewById(R.id.float_btn);

        //设置悬浮按钮的监听事件
        floatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //打开文件管理器
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
            }
        });

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
                    final FileBean fileBean = (FileBean) mFileAdapter.getItem(position);
                    FileType fileType = fileBean.getFileType();

                    final String smbFullPath = fileBean.getPath();
                    final String smbPath = smbFullPath.substring(0, smbFullPath.lastIndexOf("/") + 1);

                    //Log.i("msg", "smbFullPath:" + smbFullPath);
                    // Log.i("msg", "smbPath:" + smbPath);

                    if (fileType != null && fileType != FileType.directory) {
                        ActionSheetDialog dialog = new ActionSheetDialog(context).builder().setTitle("请选择")
                                .addSheetItem("下载", null, new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        downloadFile(smbFullPath, localRootPath);
                                    }
                                }).addSheetItem("新建文件夹", null, new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        mkdir(smbPath);
                                    }
                                }).addSheetItem("删除", null, new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int i) {
                                        deleteFile(smbFullPath);
                                    }
                                });
                        dialog.show();
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

        //初始化SmbFileUtil工具类
        SmbFileUtil.init(ip, username, passwd);

        refreshTitleState("FamilyCloud", smbRootPath);
        //smbFullPath = smbRootPath;
        getSmbFiles(smbRootPath);

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
     * 处理文件管理器的选择事件，并上传
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String localPath = FileChooseUtil.getInstance(context).getChooseFileResultPath(uri);

            List<TitlePathBean> titlePathList = (List<TitlePathBean>) mTitleAdapter.getAdapterData();
            String remotePath = titlePathList.get(titlePathList.size() - 1).getPath();
            Log.i("msg", "remotePath:" + remotePath);

            uploadFile(remotePath, localPath);
        }

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

            @Override
            public void nullSmbFileInMyTask() {
                CommomUtil.Toast("用户名或密码错误");
            }
        });

        smbTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");

    }


    /**
     * 上传文件
     *
     * @param remotePath 远程文件夹路径（注意不包括文件）
     * @param localPath  本地文件路径
     */
    private void uploadFile(final String remotePath, String localPath) {

        mProgress = new ProgressDialog(context);//准备方法里初始进度条

        String remoteFullPath = remotePath + localPath.substring(localPath.lastIndexOf("/") + 1, localPath.length());

        SmbUploadTask uploadTask = new SmbUploadTask(remoteFullPath, localPath,
                username, passwd, ip, context, new SmbUploadTask.OnFinishUpload() {
            @Override
            public void onStart() {

                mProgress.setMax(MAX);
                mProgress.setTitle("正在执行中...");
                mProgress.setMessage("正在上传文件,请稍等...");
                mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgress.setIndeterminate(false);
                mProgress.setCancelable(false);
                //mProgress.setProgress(0);
                mProgress.show();
            }

            @Override
            public void onSuccess() {
                mProgress.dismiss();
                getSmbFiles(remotePath);
                CommomUtil.Toast("upload successfully.");
            }

            @Override
            public void onFail() {
                mProgress.dismiss();
                getSmbFiles(remotePath);
                CommomUtil.Toast("upload fail.");
            }

            @Override
            public void onProgress(int progress) {
                mProgress.setProgress(progress);
            }
        });

        uploadTask.execute();
    }


    /**
     * 下载文件
     *
     * @param remoteFullPath 远程文件
     * @param localPath      本地文件夹
     */
    private void downloadFile(String remoteFullPath, String localPath) {

        //String filename = remoteFullPath.substring(remoteFullPath.lastIndexOf("/") + 1, remoteFullPath.length());

        final String remotePath = remoteFullPath.substring(0, remoteFullPath.lastIndexOf("/") + 1);

        //String locaFullPath = localPath + filename;

        mProgress = new ProgressDialog(context);//准备方法里初始进度条

        SmbDownloadTask downloadTask = new SmbDownloadTask(remoteFullPath, localPath,
                username, passwd, ip, new SmbDownloadTask.OnFinishDownload() {
            @Override
            public void onStart() {

                mProgress.setMax(MAX);
                mProgress.setTitle("正在执行中...");
                mProgress.setMessage("正在下载文件,请稍等...");
                mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgress.setIndeterminate(false);
                mProgress.setCancelable(false);
                //mProgress.setProgress(0);
                mProgress.show();
            }

            @Override
            public void onSuccess() {
                mProgress.dismiss();
                getSmbFiles(remotePath);
                CommomUtil.Toast("download successfully.");
            }

            @Override
            public void onFail() {
                mProgress.dismiss();
                getSmbFiles(remotePath);
                CommomUtil.Toast("download fail.");
            }

            @Override
            public void onProgress(int progress) {
                mProgress.setProgress(progress);
            }
        });

        downloadTask.execute();
    }


    /**
     * 删除文件，远程路径，注意是文件路径
     *
     * @param remoteFullPath 远程文件路径
     */
    private void deleteFile(final String remoteFullPath) {

        SmbDeleteTask smbDeleteTask = new SmbDeleteTask(remoteFullPath, username,
                passwd, ip, new SmbDeleteTask.OnFinishDelete() {
            @Override
            public void onSuccess() {
                getSmbFiles(remoteFullPath.substring(0, remoteFullPath.lastIndexOf("/") + 1));
                CommomUtil.Toast("delete successfully");
            }

            @Override
            public void onFail() {
                CommomUtil.Toast("delete fail");
            }
        });

        smbDeleteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");

    }

    /**
     * 新建文件夹
     *
     * @param remotePath 远程路径，注意不是文件路径，而是目录路径
     */
    private void mkdir(final String remotePath) {

        int num = (int) (Math.random() * 10);
        String remoteFullNewPath = remotePath + "新文件夹" + num + "/";

        SmbMkdirTask smbMkdirTask = new SmbMkdirTask(remoteFullNewPath, username,
                passwd, ip, new SmbMkdirTask.OnFinishMkdir() {
            @Override
            public void onSuccess() {
                getSmbFiles(remotePath);
                CommomUtil.Toast("新建文件夹成功");
            }

            @Override
            public void onFail() {
                CommomUtil.Toast("无法新建文件夹");
            }
        });

        smbMkdirTask.execute();
    }

    /**
     * 在该fragment中回退到上一页
     */
    public void back() {

        List<TitlePathBean> titlePathList = (List<TitlePathBean>) mTitleAdapter.getAdapterData();
        if (titlePathList.size() == 1) {
            //跳回cloudfragment
            onSwitchFragment.switchFragmentTocCloudFragment();
        } else {
            //跳回上一页
            mTitleAdapter.removeItem(titlePathList.size() - 1);
            getSmbFiles(titlePathList.get(titlePathList.size() - 1).getPath());
        }
    }
}
