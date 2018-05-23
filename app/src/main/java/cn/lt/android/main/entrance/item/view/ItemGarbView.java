package cn.lt.android.main.entrance.item.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import cn.lt.android.LogTAG;
import cn.lt.android.entity.AppBriefBean;
import cn.lt.android.entity.AppTopicBean;
import cn.lt.android.main.entrance.data.ItemData;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.util.DensityUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.appstore.R;

/**
 * Created by Administrator on 2016/6/12.
 */
public class ItemGarbView extends ItemView {
    public LinearLayout mChangePadding;

    private LinearLayout mGameRootView;
    private LinearLayout mContainer;
    private TextView mNameTV, mTitleTV;
    private ViewStub viewStub_title_root;

    public ItemGarbView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ItemGarbView(Context context, String pageName) {
        super(context, pageName);
        init();
    }

    public ItemGarbView(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    @Override
    public void fillView(ItemData<? extends BaseBean> bean, int position) {
        if (bean != null) {
            /* 初始化标题内容*/
            ItemData<AppTopicBean> mData = (ItemData<AppTopicBean>) bean;
            AppTopicBean appTopicBean = mData.getmData();

            if (appTopicBean.getPositionType() == AppTopicBean.IS_FIRST || appTopicBean.getPositionType() == AppTopicBean.IS_ONLY) {
                String textColor = appTopicBean.getTitle_color();
                if (appTopicBean.getTopic_name().length() == 0 || appTopicBean.getTopic_title().length() == 0 || textColor.length() == 0) {
                    if(mContainer != null) {
                        mContainer.setVisibility(View.GONE);
                    }
                } else {
                    showTopicTitle();
                    mNameTV.setText(appTopicBean.getTopic_name());
                    mTitleTV.setText(appTopicBean.getTopic_title());
                    try {
                        mTitleTV.setBackgroundColor(Color.parseColor(textColor));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if(mContainer != null) {
                    mContainer.setVisibility(View.GONE);
                }
            }

            if (appTopicBean.getPositionType() == AppTopicBean.IS_LAST || appTopicBean.getPositionType() == AppTopicBean.IS_ONLY) {
                ((LayoutParams) mChangePadding.getLayoutParams()).bottomMargin = getResources().getDimensionPixelOffset(R.dimen.padding_eight_dp);
            } else {
                ((LayoutParams) mChangePadding.getLayoutParams()).bottomMargin = 0;
            }

            List<AppBriefBean> apps = appTopicBean.getBriefApps();


            boolean showBigger = mGameRootView.getChildCount() > apps.size();
            SquareAppView lastChild = (SquareAppView) mGameRootView.getChildAt(mGameRootView.getChildCount() - 1);
            if (showBigger && lastChild.getVisibility() == VISIBLE) {
                lastChild.setVisibility(GONE);
            } else if (!showBigger && lastChild.getVisibility() == GONE){
                lastChild.setVisibility(VISIBLE);
            }

            for (int i = 0; i < apps.size(); i++) {
                ItemData<AppBriefBean> itemData = new ItemData<>(apps.get(i));

                SquareAppView squareAppView = (SquareAppView) mGameRootView.getChildAt(i);
                squareAppView.setStyle(showBigger);
                squareAppView.fillView(itemData, position);
            }

//            boolean showBigger = mGameRootView.getChildCount() > apps.size();
//            for (int i = 0; i < mGameRootView.getChildCount(); i++) {
//                SquareAppView squareAppView = (SquareAppView) mGameRootView.getChildAt(i);
//                if (i < apps.size()) {
//                    if (squareAppView.getVisibility() == GONE) {
//                        squareAppView.setVisibility(VISIBLE);
//                    }
//
//                    squareAppView.setStyle(showBigger);
//                    ItemData<AppBriefBean> itemData = new ItemData<>(apps.get(i));
//                    squareAppView.fillView(itemData, position);
//                } else {
//                    if (squareAppView.getVisibility() == VISIBLE) {
//                        squareAppView.setVisibility(GONE);
//                    }
//                }
//
//            }


        }
    }

    private void showTopicTitle() {
        if(mContainer == null) {
            mContainer = (LinearLayout) viewStub_title_root.inflate();
            mNameTV = (TextView) mContainer.findViewById(R.id.tv_title);
            mTitleTV = (TextView) mContainer.findViewById(R.id.tv_name);
        }
        mContainer.setVisibility(View.VISIBLE);
    }

    private void init() {
        long t1 = System.currentTimeMillis();
        LayoutInflater.from(getContext()).inflate(R.layout.layout_item_app_all, this);
        mChangePadding = (LinearLayout) findViewById(R.id.change_padding);
        viewStub_title_root = (ViewStub) findViewById(R.id.viewStub_title_root);
        mGameRootView = (LinearLayout) findViewById(R.id.llt_root_game_square);


        LayoutParams lp = new LayoutParams(DensityUtil.getScreenSize(getContext())[0], ViewGroup.LayoutParams.MATCH_PARENT);
        mGameRootView.setLayoutParams(lp);
        for (int i = 0; i < 4; i++) {
            SquareAppView gameView = new SquareAppView(getContext(), mPageName);
            android.widget.LinearLayout.LayoutParams gameLP = new android.widget.LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT);
            gameLP.weight = 1;
            mGameRootView.addView(gameView, gameLP);
        }
        LogUtils.i(LogTAG.youhuaLog, "ItemGarbView.init() = " + (System.currentTimeMillis() - t1));
    }




}
