package client.nas.find.com.nasclient.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import client.nas.find.com.nasclient.R;

/**
 * @author Kevin-
 * @time 20181205
 * @description 通用弹窗类, 参考：https://github.com/xiaoxiaoqingyi/mine-android-repository
 * @updateTime 20181205
 */

public class CommonDialog extends Dialog implements View.OnClickListener {

    //定义控件
    private TextView dialogTitleTxt;
    private TextView dialogSubmitTxt;
    private TextView dialogCancelTxt;

    private EditText dialogUsernameEd;
    private EditText dialogPasswdEd;

    private CheckBox dialogCheckBox;

    //定义显示变量
    private Context context;
    private String content;
    private OnCloseListener listener;
    private String positiveName;
    private String negativeName;
    private String title;

    private String username;
    private String passwd;
    private Boolean isChecked;

    public CommonDialog(Context context) {
        super(context);
        this.context = context;
    }

    public CommonDialog(Context context, int themeResId, String content) {
        super(context, themeResId);
        this.context = context;
        this.content = content;
    }

    public CommonDialog(Context context, int themeResId, String content, OnCloseListener listener) {
        super(context, themeResId);
        this.context = context;
        this.content = content;
        this.listener = listener;
    }

    protected CommonDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context = context;
    }

    public CommonDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public CommonDialog setPositiveButton(String name) {
        this.positiveName = name;
        return this;
    }

    public CommonDialog setNegativeButton(String name) {
        this.negativeName = name;
        return this;
    }

    public CommonDialog setUsernameEd(String username) {
        this.username = username;
        return this;
    }

    public CommonDialog setPasswdEd(String passwd) {
        this.passwd = passwd;
        return this;
    }

    public CommonDialog setCheckBox(boolean isChecked) {
        this.isChecked = isChecked;
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_common);
        setCanceledOnTouchOutside(false);
        initView();
    }

    private void initView() {
        dialogTitleTxt = findViewById(R.id.dialog_title);
        dialogSubmitTxt = findViewById(R.id.dialog_submit);
        dialogCancelTxt = findViewById(R.id.dialog_cancel);

        // 设置确认/取消键的点击事件
        dialogSubmitTxt.setOnClickListener(this);
        dialogCancelTxt.setOnClickListener(this);

        dialogUsernameEd = findViewById(R.id.dialog_username);
        dialogPasswdEd = findViewById(R.id.dialog_passwd);

        dialogCheckBox = findViewById(R.id.dialog_checkbox);

        //自定义设置标题、确认键、取消键
        if (!TextUtils.isEmpty(positiveName)) {
            dialogSubmitTxt.setText(positiveName);
        }

        if (!TextUtils.isEmpty(negativeName)) {
            dialogCancelTxt.setText(negativeName);
        }

        if (!TextUtils.isEmpty(title)) {
            dialogTitleTxt.setText(title);
        }

        if (!TextUtils.isEmpty(username)) {
            dialogUsernameEd.setText(username);
        }

        if (!TextUtils.isEmpty(passwd)) {
            dialogPasswdEd.setText(passwd);
        }

        if (isChecked != null) {
            dialogCheckBox.setChecked(isChecked);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_cancel:
                if (listener != null) {
                    listener.onClick(this, false, null, null, false);
                }
                this.dismiss();
                break;
            case R.id.dialog_submit:
                if (listener != null) {
                    //获取输入值
                    username = dialogUsernameEd.getText().toString().trim();
                    passwd = dialogPasswdEd.getText().toString().trim();
                    isChecked = dialogCheckBox.isChecked();

                    listener.onClick(this, true, username, passwd, isChecked);

                    this.dismiss();
                }
                break;
        }
    }

    public interface OnCloseListener {
        void onClick(Dialog dialog, boolean confirm, String username, String passwd, boolean isCheck);

    }

}
