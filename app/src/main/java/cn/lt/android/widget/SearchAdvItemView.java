package cn.lt.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.LTApplication;
import cn.lt.android.entity.HotSearchBean;
import cn.lt.android.main.UIController;
import cn.lt.android.main.search.SearchActivity;
import cn.lt.android.main.search.SearchActivityUtil;
import cn.lt.android.statistics.DCStat;
import cn.lt.appstore.R;

/**
 * Created by ltbl on 2016/4/22.
 */
public class SearchAdvItemView extends LinearLayout implements View.OnClickListener {
    private List<HotSearchBean> hotSearchBeanList = new ArrayList<>();
    private TextView[] mTV = new TextView[3];
    private Context mContext;
    private String itemType;
    private int lineCount;


    public SearchAdvItemView(Context context, String type,int lineCount) {
        super(context);
        this.mContext = context;
        this.itemType = type;
        this.lineCount = lineCount;
        initView(context);
    }

    public SearchAdvItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initView(context);
    }

    public SearchAdvItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView(context);
    }

    private void initView(Context mContext) {
        LayoutInflater.from(mContext).inflate(R.layout.search_item_view, this);
        mTV[0] = (TextView) findViewById(R.id.tv01);
        mTV[1] = (TextView) findViewById(R.id.tv02);
        mTV[2] = (TextView) findViewById(R.id.tv03);
        mTV[0].setOnClickListener(this);
        mTV[1].setOnClickListener(this);
        mTV[2].setOnClickListener(this);
    }

    public void setData(List<HotSearchBean> list) {
        this.hotSearchBeanList.clear();
        this.hotSearchBeanList.addAll(list);
        drawTitle();
    }

    private void drawTitle() {
        mTV[0].setText(hotSearchBeanList.get(0).getTitle());
        mTV[1].setText(hotSearchBeanList.get(1).getTitle());
        mTV[2].setText(hotSearchBeanList.get(2).getTitle());
    }

    public void setColor() {
        mTV[0].setTextColor(mContext.getResources().getColor(R.color.orange));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv01:
                jump(v, hotSearchBeanList.get(0),lineCount * 3 + 1);
                break;
            case R.id.tv02:
                jump(v, hotSearchBeanList.get(1),lineCount * 3 + 2);
                break;
            case R.id.tv03:
                jump(v, hotSearchBeanList.get(2),lineCount * 3 + 3);
                break;
        }
    }

    private void jump(View v, HotSearchBean appDetailBean,int pos) {
        LTApplication.instance.word = appDetailBean.getTitle();
        if (appDetailBean == null) return;
        if ("HOT".equals(itemType)) {
            ((SearchActivity) getContext()).gotoSearchResultFragment(appDetailBean.getTitle(),"");
            DCStat.searchRecommendClickEvent(appDetailBean.getTitle(), "HOT",pos);
        } else {
            UIController.goAppDetail(v.getContext(), false, "", String.valueOf(appDetailBean.getId()), null, appDetailBean.getApps_type(), "", "", "",appDetailBean.getReportData());
            SearchActivityUtil.saveSearchValue(appDetailBean.getTitle());
            DCStat.searchRecommendClickEvent(appDetailBean.getTitle(), appDetailBean.getApps_type(),pos);
        }
    }
}
