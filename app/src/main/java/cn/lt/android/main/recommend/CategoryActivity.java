package cn.lt.android.main.recommend;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.base.BaseAppCompatActivity;
import cn.lt.android.entity.AppCatBean;
import cn.lt.android.main.Item;
import cn.lt.android.main.UIController;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ShowRefreshLoadingUtils;
import cn.lt.android.widget.ActionBar;
import cn.lt.appstore.R;
import cn.lt.framework.util.NetWorkUtils;
import cn.lt.framework.util.ScreenUtils;
import cn.lt.pullandloadmore.LoadingLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by wenchao on 2016/3/1.
 * 推荐分类界面
 */
public class CategoryActivity extends BaseAppCompatActivity implements AdapterView.OnItemClickListener {
    @Override
    public void setPageAlias() {
        setmPageAlias(Constant.PAGE_SINGLE_CATEGORY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        setStatusBar();
        assignViews();
        requestData();
    }
    private ActionBar mActionBar;
    private LoadingLayout mLoadingLayout;
    private RecyclerView mRecyclerView;
    private CategoryAdapter mAdapter;

    private void assignViews() {
        mActionBar = (ActionBar) findViewById(R.id.actionBar);
        mLoadingLayout = (LoadingLayout) findViewById(R.id.loadingLayout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        mLoadingLayout.setOnRetryClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoadingLayout.showLoading();
                requestData();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new CategoryAdapter(this,getPageAlias());
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.addHeaderView(createHeaderView());

        mActionBar.setTitle(getString(R.string.category));
        mLoadingLayout.showLoading();
    }


    private View createHeaderView() {
        View view = new View(this);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, ScreenUtils.dpToPxInt(this, 8)));
        return view;
    }

    /**
     * 把服务器数据 转换成需要显示的数据集合
     *
     * @param baseBeanList
     */
    void updateView(List<BaseBean> baseBeanList) {
        if (baseBeanList == null || baseBeanList.size() == 0) return;
        List<Item> itemList = new ArrayList<>();
        List<AppCatBean> twoCatItem = new ArrayList<>();
        for (int i = 0; i < baseBeanList.size(); i++) {
            BaseBean baseBean = baseBeanList.get(i);
            if (baseBean.getLtType().equals("category_group_name")) {
                itemList.add(new Item(CategoryAdapter.TYPE_LABEL, baseBean));
            } else if (baseBean.getLtType().equals("category")) {
                if (twoCatItem.size() >= 2) {
                    twoCatItem = new ArrayList<>();
                }
                twoCatItem.add((AppCatBean) baseBean);
                if (twoCatItem.size() >= 2) {//若达到2个
                    itemList.add(new Item(CategoryAdapter.TYPE_CATEGORY_TWO, twoCatItem));
                } else if (i + 1 < baseBeanList.size()) {//下一个是父节点
                    BaseBean nextBean = baseBeanList.get(i + 1);
                    if (nextBean.getLtType().equals("category_group_name")) {
                        itemList.add(new Item(CategoryAdapter.TYPE_CATEGORY_TWO, twoCatItem));
                        //需要重置
                        twoCatItem = new ArrayList<>();
                    }
                } else if (i + 1 == baseBeanList.size()) {//到了最后一个
                    itemList.add(new Item(CategoryAdapter.TYPE_CATEGORY_TWO, twoCatItem));
                }


            }
        }
        mAdapter.setList(itemList);
    }


    void requestData() {
        if (!NetWorkUtils.isConnected(this)) {
            ShowRefreshLoadingUtils.showLoadingForNoNet(mLoadingLayout);
            return;
        }

        NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<List<BaseBean>>() {
            @Override
            public void onResponse(Call<List<BaseBean>> call, Response<List<BaseBean>> response) {
                List<BaseBean> baseBeanList = response.body();
                updateView(baseBeanList);
                mLoadingLayout.showContent();
                if (baseBeanList == null || baseBeanList.size() == 0) {
                    mLoadingLayout.showEmpty();
                }

            }

            @Override
            public void onFailure(Call<List<BaseBean>> call, Throwable t) {
                ShowRefreshLoadingUtils.showLoadingForNotGood(mLoadingLayout);
            }
        }).bulid().requestCatsList();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object o = view.getTag(R.id.click_type);
        AppCatBean data = null;
        if ("label01".equals(o)) {
            data = (AppCatBean) view.getTag(R.id.click_date);
            LogUtils.i("zzz", "分类跳转信息" + data.getId() + "/" + data.getType());
            UIController.goAppDetail(this,false, "", data.getId(), "",data.getType(), getPageAlias(),"","");
        } else if ("label02".equals(o)) {
            data = (AppCatBean) view.getTag(R.id.click_date);
            UIController.goAppDetail(this,false, "", data.getId(), data.getType(),"", getPageAlias(),"","");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mActionBar.setPageName(getPageAlias());
    }
}
