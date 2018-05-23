package cn.lt.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.main.UIController;
import cn.lt.appstore.R;

/**
 * Created by ltbl on 2016/4/22.
 */
public class SearchHistoryView extends LinearLayout implements View.OnClickListener {
    private List<String> hotSearchBeanList = new ArrayList<>();
    private TextView mTV[] = new TextView[9];
    private View line01, line02, line03, line04, line05, line06, divider, divider02;
    private TitleViewCallBack callBack = null;
    private LinearLayout mNumTwo, mNumThree;

    public SearchHistoryView(Context context) {
        super(context);
        initView(context);
    }

    public SearchHistoryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public SearchHistoryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context mContext) {
        LayoutInflater.from(mContext).inflate(R.layout.search_history, this);
        mNumTwo = (LinearLayout) findViewById(R.id.ll_numTwo);
        mNumThree = (LinearLayout) findViewById(R.id.ll_numThree);
        mTV[0] = (TextView) findViewById(R.id.tv01);
        mTV[1] = (TextView) findViewById(R.id.tv02);
        mTV[2] = (TextView) findViewById(R.id.tv03);
        mTV[3] = (TextView) findViewById(R.id.tv04);
        mTV[4] = (TextView) findViewById(R.id.tv05);
        mTV[5] = (TextView) findViewById(R.id.tv06);
        mTV[6] = (TextView) findViewById(R.id.tv07);
        mTV[7] = (TextView) findViewById(R.id.tv08);
        mTV[8] = (TextView) findViewById(R.id.tv09);
        line01 = findViewById(R.id.line01);
        line02 = findViewById(R.id.line02);
        line03 = findViewById(R.id.line03);
        line04 = findViewById(R.id.line04);
        line05 = findViewById(R.id.line05);
        line06 = findViewById(R.id.line06);
        divider = findViewById(R.id.divider01);
        divider02 = findViewById(R.id.divider02);
        mTV[0].setOnClickListener(this);
        for (int i = 0; i < 9; i++) {
            mTV[i].setOnClickListener(this);
        }
    }

    public void setData(List<String> list) {
        this.hotSearchBeanList.clear();
        this.hotSearchBeanList.addAll(list);
        drawTitle();
    }

    private void drawTitle() {
        int size = hotSearchBeanList.size();
        switch (size) {
            case 1:
                mTV[0].setVisibility(View.VISIBLE);
                mTV[0].setText(hotSearchBeanList.get(0));
                mTV[1].setVisibility(View.INVISIBLE);
                mTV[2].setVisibility(View.INVISIBLE);
                mTV[3].setVisibility(View.INVISIBLE);
                mTV[4].setVisibility(View.INVISIBLE);
                mTV[5].setVisibility(View.INVISIBLE);
                mTV[6].setVisibility(View.INVISIBLE);
                mTV[7].setVisibility(View.INVISIBLE);
                line01.setVisibility(View.VISIBLE);
                line02.setVisibility(View.INVISIBLE);
                line03.setVisibility(View.INVISIBLE);
                line04.setVisibility(View.INVISIBLE);
                line05.setVisibility(View.INVISIBLE);
                line06.setVisibility(View.INVISIBLE);
                divider.setVisibility(View.INVISIBLE);
                mNumTwo.setVisibility(View.GONE);
                mNumThree.setVisibility(View.GONE);
                break;
            case 2:
                mTV[0].setVisibility(View.VISIBLE);
                mTV[1].setVisibility(View.VISIBLE);
                mTV[2].setVisibility(View.INVISIBLE);
                mTV[3].setVisibility(View.INVISIBLE);
                mTV[4].setVisibility(View.INVISIBLE);
                mTV[5].setVisibility(View.INVISIBLE);
                mTV[6].setVisibility(View.INVISIBLE);
                mTV[7].setVisibility(View.INVISIBLE);
                line01.setVisibility(View.VISIBLE);
                line02.setVisibility(View.VISIBLE);
                line03.setVisibility(View.INVISIBLE);
                line04.setVisibility(View.INVISIBLE);
                line05.setVisibility(View.INVISIBLE);
                line06.setVisibility(View.INVISIBLE);
                mTV[0].setText(hotSearchBeanList.get(0));
                mTV[1].setText(hotSearchBeanList.get(1));
                divider.setVisibility(View.INVISIBLE);
                mNumTwo.setVisibility(View.GONE);
                mNumThree.setVisibility(View.GONE);
                break;
            case 3:
                mTV[0].setVisibility(View.VISIBLE);
                mTV[1].setVisibility(View.VISIBLE);
                mTV[2].setVisibility(View.VISIBLE);
                mTV[3].setVisibility(View.INVISIBLE);
                mTV[4].setVisibility(View.INVISIBLE);
                mTV[5].setVisibility(View.INVISIBLE);
                mTV[6].setVisibility(View.INVISIBLE);
                mTV[7].setVisibility(View.INVISIBLE);
                mTV[0].setText(hotSearchBeanList.get(0));
                mTV[1].setText(hotSearchBeanList.get(1));
                mTV[2].setText(hotSearchBeanList.get(2));
                line01.setVisibility(View.VISIBLE);
                line02.setVisibility(View.VISIBLE);
                line03.setVisibility(View.VISIBLE);
                line04.setVisibility(View.INVISIBLE);
                line05.setVisibility(View.INVISIBLE);
                line06.setVisibility(View.INVISIBLE);
                divider.setVisibility(View.INVISIBLE);
                mNumTwo.setVisibility(View.GONE);
                mNumThree.setVisibility(View.GONE);
                break;
            case 4:
                mTV[0].setVisibility(View.VISIBLE);
                mTV[1].setVisibility(View.VISIBLE);
                mTV[2].setVisibility(View.VISIBLE);
                mTV[3].setVisibility(View.VISIBLE);
                mTV[4].setVisibility(View.INVISIBLE);
                mTV[5].setVisibility(View.INVISIBLE);
                mTV[6].setVisibility(View.INVISIBLE);
                mTV[7].setVisibility(View.INVISIBLE);
                mTV[0].setText(hotSearchBeanList.get(0));
                mTV[1].setText(hotSearchBeanList.get(1));
                mTV[2].setText(hotSearchBeanList.get(2));
                mTV[3].setText(hotSearchBeanList.get(3));
                line01.setVisibility(View.VISIBLE);
                line02.setVisibility(View.VISIBLE);
                line03.setVisibility(View.VISIBLE);
                line04.setVisibility(View.INVISIBLE);
                line05.setVisibility(View.INVISIBLE);
                line06.setVisibility(View.INVISIBLE);
                divider.setVisibility(View.INVISIBLE);
                mNumTwo.setVisibility(View.GONE);
                mNumThree.setVisibility(View.GONE);
                break;
            case 5:

                mTV[0].setVisibility(View.VISIBLE);
                mTV[1].setVisibility(View.VISIBLE);
                mTV[2].setVisibility(View.VISIBLE);
                mTV[3].setVisibility(View.VISIBLE);
                mTV[4].setVisibility(View.VISIBLE);
                mTV[5].setVisibility(View.INVISIBLE);
                mTV[6].setVisibility(View.INVISIBLE);
                mTV[7].setVisibility(View.INVISIBLE);
                mTV[0].setText(hotSearchBeanList.get(0));
                mTV[1].setText(hotSearchBeanList.get(1));
                mTV[2].setText(hotSearchBeanList.get(2));
                mTV[3].setText(hotSearchBeanList.get(3));
                mTV[4].setText(hotSearchBeanList.get(4));
                line01.setVisibility(View.VISIBLE);
                line02.setVisibility(View.VISIBLE);
                line03.setVisibility(View.VISIBLE);
                line04.setVisibility(View.VISIBLE);
                line05.setVisibility(View.INVISIBLE);
                line06.setVisibility(View.INVISIBLE);
                divider.setVisibility(View.VISIBLE);
                mNumTwo.setVisibility(View.VISIBLE);
                mNumThree.setVisibility(View.GONE);
                break;
            case 6:
                mTV[0].setVisibility(View.VISIBLE);
                mTV[1].setVisibility(View.VISIBLE);
                mTV[2].setVisibility(View.VISIBLE);
                mTV[3].setVisibility(View.VISIBLE);
                mTV[4].setVisibility(View.VISIBLE);
                mTV[5].setVisibility(View.VISIBLE);
                mTV[6].setVisibility(View.INVISIBLE);
                mTV[7].setVisibility(View.INVISIBLE);
                mTV[0].setText(hotSearchBeanList.get(0));
                mTV[1].setText(hotSearchBeanList.get(1));
                mTV[2].setText(hotSearchBeanList.get(2));
                mTV[3].setText(hotSearchBeanList.get(3));
                mTV[4].setText(hotSearchBeanList.get(4));
                mTV[5].setText(hotSearchBeanList.get(5));
                line01.setVisibility(View.VISIBLE);
                line02.setVisibility(View.VISIBLE);
                line03.setVisibility(View.VISIBLE);
                line04.setVisibility(View.VISIBLE);
                line05.setVisibility(View.VISIBLE);
                line06.setVisibility(View.INVISIBLE);
                divider.setVisibility(View.VISIBLE);
                mNumTwo.setVisibility(View.VISIBLE);
                mNumThree.setVisibility(View.GONE);
                break;
            case 7:
                mTV[0].setVisibility(View.VISIBLE);
                mTV[1].setVisibility(View.VISIBLE);
                mTV[2].setVisibility(View.VISIBLE);
                mTV[3].setVisibility(View.VISIBLE);
                mTV[4].setVisibility(View.VISIBLE);
                mTV[5].setVisibility(View.VISIBLE);
                mTV[6].setVisibility(View.VISIBLE);
                mTV[7].setVisibility(View.INVISIBLE);
                mTV[0].setText(hotSearchBeanList.get(0));
                mTV[1].setText(hotSearchBeanList.get(1));
                mTV[2].setText(hotSearchBeanList.get(2));
                mTV[3].setText(hotSearchBeanList.get(3));
                mTV[4].setText(hotSearchBeanList.get(4));
                mTV[5].setText(hotSearchBeanList.get(5));
                mTV[6].setText(hotSearchBeanList.get(6));
                line01.setVisibility(View.VISIBLE);
                line02.setVisibility(View.VISIBLE);
                line03.setVisibility(View.VISIBLE);
                line04.setVisibility(View.VISIBLE);
                line05.setVisibility(View.VISIBLE);
                line06.setVisibility(View.VISIBLE);
                divider.setVisibility(View.VISIBLE);
                divider02.setVisibility(View.VISIBLE);
                mNumTwo.setVisibility(View.VISIBLE);
                break;
            case 8:
                mTV[0].setVisibility(View.VISIBLE);
                mTV[1].setVisibility(View.VISIBLE);
                mTV[2].setVisibility(View.VISIBLE);
                mTV[3].setVisibility(View.VISIBLE);
                mTV[4].setVisibility(View.VISIBLE);
                mTV[5].setVisibility(View.VISIBLE);
                mTV[6].setVisibility(View.VISIBLE);
                mTV[7].setVisibility(View.VISIBLE);
                mTV[0].setText(hotSearchBeanList.get(0));
                mTV[1].setText(hotSearchBeanList.get(1));
                mTV[2].setText(hotSearchBeanList.get(2));
                mTV[3].setText(hotSearchBeanList.get(3));
                mTV[4].setText(hotSearchBeanList.get(4));
                mTV[5].setText(hotSearchBeanList.get(5));
                mTV[6].setText(hotSearchBeanList.get(6));
                mTV[7].setText(hotSearchBeanList.get(7));
                line01.setVisibility(View.VISIBLE);
                line02.setVisibility(View.VISIBLE);
                line03.setVisibility(View.VISIBLE);
                line04.setVisibility(View.VISIBLE);
                line05.setVisibility(View.VISIBLE);
                line06.setVisibility(View.INVISIBLE);
                divider.setVisibility(View.VISIBLE);
                divider02.setVisibility(View.VISIBLE);
                mNumTwo.setVisibility(View.VISIBLE);
                break;
            case 9:
                mTV[0].setVisibility(View.VISIBLE);
                mTV[1].setVisibility(View.VISIBLE);
                mTV[2].setVisibility(View.VISIBLE);
                mTV[3].setVisibility(View.VISIBLE);
                mTV[4].setVisibility(View.VISIBLE);
                mTV[5].setVisibility(View.VISIBLE);
                mTV[6].setVisibility(View.VISIBLE);
                mTV[7].setVisibility(View.VISIBLE);
                mTV[8].setVisibility(View.VISIBLE);
                mTV[0].setText(hotSearchBeanList.get(0));
                mTV[1].setText(hotSearchBeanList.get(1));
                mTV[2].setText(hotSearchBeanList.get(2));
                mTV[3].setText(hotSearchBeanList.get(3));
                mTV[4].setText(hotSearchBeanList.get(4));
                mTV[5].setText(hotSearchBeanList.get(5));
                mTV[6].setText(hotSearchBeanList.get(6));
                mTV[7].setText(hotSearchBeanList.get(7));
                mTV[8].setText(hotSearchBeanList.get(8));
                line01.setVisibility(View.VISIBLE);
                line02.setVisibility(View.VISIBLE);
                line03.setVisibility(View.VISIBLE);
                line04.setVisibility(View.VISIBLE);
                line05.setVisibility(View.VISIBLE);
                line06.setVisibility(View.VISIBLE);
                divider.setVisibility(View.VISIBLE);
                divider02.setVisibility(View.VISIBLE);
                mNumTwo.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv01:
//                ((SearchActivity) getContext()).gotoSearchResultFragment(hotSearchBeanList.get(0));
//                break;
            case R.id.tv02:
//                ((SearchActivity) getContext()).gotoSearchResultFragment(hotSearchBeanList.get(1));
//                break;
            case R.id.tv03:
//                ((SearchActivity) getContext()).gotoSearchResultFragment(hotSearchBeanList.get(2));
//                break;
            case R.id.tv04:
//                ((SearchActivity) getContext()).gotoSearchResultFragment(hotSearchBeanList.get(3));
//                break;
            case R.id.tv05:
//                ((SearchActivity) getContext()).gotoSearchResultFragment(hotSearchBeanList.get(4));
//                break;
            case R.id.tv06:
//                ((SearchActivity) getContext()).gotoSearchResultFragment(hotSearchBeanList.get(5));
//                break;
            case R.id.tv07:
//                ((SearchActivity) getContext()).gotoSearchResultFragment(hotSearchBeanList.get(6));
//                break;
            case R.id.tv08:
//                ((SearchActivity) getContext()).gotoSearchResultFragment(hotSearchBeanList.get(7));
//                break;
            case R.id.tv09:
//                ((SearchActivity) getContext()).gotoSearchResultFragment(hotSearchBeanList.get(8));
                UIController.goAppDetail(v.getContext(), false, "", "10000038", null, "software", "", "", "");
                break;
        }
    }

    public interface TitleViewCallBack {
        void onClick(String info);
    }

    public TitleViewCallBack getCallBack() {
        return callBack;
    }

    public void setCallBack(TitleViewCallBack callBack) {
        this.callBack = callBack;
    }
}
