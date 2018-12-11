package client.nas.find.com.nasclient.activity.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import client.nas.find.com.nasclient.R;
import client.nas.find.com.nasclient.adapter.base.MultiAdapter;
import client.nas.find.com.nasclient.util.CommomUtil;

/**
 * @author Kevin-
 * @time 20181205
 * @description 参考：https://github.com/xinzhazha/MultiView
 * @updateTime 20181205
 */

public class MultiView extends ViewGroup {

    private static final String TAG = "MultiView";
    private int TEXT_NUM_COLOR = 0xffffffff;
    private int TEXT_NUM_BACKGROUND_COLOR = 0x33000000;

    private int childWidth, childHeight;
    private int divideSpace; //默认2dp
    private int placeholder;
    private MultiAdapter mAdapter;
    private int childCount;

    //不通过Adapter设置图片
    private List<String> mData; //针对

    private TextView mTextNum;

    public MultiView(Context context) {
        this(context, null);
    }

    public MultiView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        CommomUtil.init(context);

        mData = new ArrayList<>();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MultiView);
        divideSpace = (int) typedArray.getDimension(R.styleable.MultiView_divideSpace, CommomUtil.dip2px(2));
        placeholder = typedArray.getResourceId(R.styleable.MultiView_placeholder, -1);
        typedArray.recycle();
    }

    //测量自己的大小
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.i(TAG, "onMeasure");

        childCount = getChildCount();

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = width;

        if (childCount == 0) {
            width = 0;
            height = 0;
        } else if (childCount == 1) {
            childWidth = width - divideSpace * 2;
        } else if (childCount == 2) {
            childWidth = (width - divideSpace * 3) / 2;
            height = childWidth + divideSpace * 2;
        } else if (childCount == 4) {
            childWidth = (width - divideSpace * 3) / 2;
            height = childWidth * 2 + divideSpace * 3;
        } else {
            /**
             * 九宫格模式
             */
            childWidth = (width - divideSpace * 4) / 3;
            if (childCount < 9) {
                if (childCount % 3 == 0) {
                    height = childWidth * childCount / 3 + divideSpace * (childCount / 3 + 1);
                } else {
                    height = childWidth * (childCount / 3 + 1) + divideSpace * (childCount / 3 + 2);
                }
            } else {
                height = width;
            }
        }

        childHeight = childWidth;

        /**
         * 全所有的child都用AT_MOST模式，而child的width和height仅仅只是建议
         */
        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY);
        measureChildren(childWidthMeasureSpec, childHeightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.i(TAG, "onLayout");

        if (childCount == 1) {
            getChildAt(0).layout(divideSpace, divideSpace, childWidth + divideSpace, childWidth + divideSpace);
        } else if (childCount == 2) {
            getChildAt(0).layout(divideSpace, divideSpace, (childWidth + divideSpace), (childWidth + divideSpace));
            getChildAt(1).layout((childWidth + divideSpace * 2), divideSpace, childWidth * 2 + divideSpace * 2, (childWidth + divideSpace));
        } else if (childCount == 4) {
            for (int i = 0; i < 4; i++) {
                getChildAt(i).layout(divideSpace * (i % 2 + 1) + childWidth * (i % 2), i / 2 * childWidth + divideSpace * (i / 2 + 1),
                        divideSpace * (i % 2 + 1) + childWidth * (i % 2 + 1), divideSpace * (i / 2 + 1) + (i / 2 + 1) * childWidth);
            }
        } else {
            if (childCount <= 9) {
                for (int i = 0; i < childCount; i++) {
                    getChildAt(i).layout(divideSpace * (i % 3 + 1) + childWidth * (i % 3), i / 3 * childWidth + divideSpace * (i / 3 + 1),
                            divideSpace * (i % 3 + 1) + childWidth * (i % 3 + 1), divideSpace * (i / 3 + 1) + (i / 3 + 1) * childWidth);
                }
            } else {
                for (int i = 0; i < 9; i++) {
                    getChildAt(i).layout(divideSpace * (i % 3 + 1) + childWidth * (i % 3), i / 3 * childWidth + divideSpace * (i / 3 + 1),
                            divideSpace * (i % 3 + 1) + childWidth * (i % 3 + 1), divideSpace * (i / 3 + 1) + (i / 3 + 1) * childWidth);
                }
                getChildAt(9).layout(divideSpace * 3 + childWidth * 2, 2 * childWidth + divideSpace * 3,
                        divideSpace * 3 + childWidth * 3, divideSpace * 3 + 3 * childWidth);
            }
        }
    }

    /**
     * 设置adapter，同时设置注册MessageNotify
     */
    public void setAdapter(MultiAdapter adapter) {
        this.mAdapter = adapter;
        addViews();
        adapter.attachView(this);
    }

    /**
     * 添加adapter中所有的view
     */
    public void addViews() {

        //记得每次刷新view时，要删除其他的view
        removeAllViews();

        if (mAdapter.getCount() > 9) {
            for (int i = 0; i < 9; i++) {
                configView(i);
            }
            addOverNumView(9);
        } else {
            for (int i = 0; i < mAdapter.getCount(); i++) {
                configView(i);
            }
        }
    }

    public void configView(final int position) {
        View item = mAdapter.getView(this, position);
        if (item.getParent() != null && item.getParent() instanceof ViewGroup) {
            ((ViewGroup) item.getParent()).removeView(item);
        }
        if (item.getParent() == null) {
            addView(item);
            mAdapter.setData(mAdapter.getItem(position));
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAdapter.setOnItemClick(position);
                }
            });
        }
    }

    public void addView(int position) {

        if (position > 8) {
            addOverNumView(9);
            return;
        }
        addView(mAdapter.getView(this, position));
        mAdapter.setData(mAdapter.getItem(position));

    }

    public void clear() {
        mData.clear();
        removeAllViews();
    }


    /**
     * 设置最后一个view
     */
    public void addOverNumView(int position) {

        mTextNum = new TextView(getContext());
        mTextNum.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mTextNum.setTextSize(24);
        mTextNum.setTextColor(TEXT_NUM_COLOR);
        mTextNum.setBackgroundColor(TEXT_NUM_BACKGROUND_COLOR);
        mTextNum.setGravity(Gravity.CENTER);

        mTextNum.setText("+" + (mAdapter.getCount() - 9));


        addView(mTextNum, position);
        Log.i(TAG, "添加最后一个view");
    }
}
