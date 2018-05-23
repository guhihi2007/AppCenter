package cn.lt.android.main;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import cn.lt.android.LTApplication;
import cn.lt.android.db.AppEntity;
import cn.lt.android.entity.AppBriefBean;
import cn.lt.android.main.download.DownloadButton;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.statistics.StatisticsDataProductorImpl;
import cn.lt.android.statistics.StatisticsEventData;
import cn.lt.android.util.ImageloaderUtil;
import cn.lt.android.util.IntegratedDataUtil;
import cn.lt.appstore.R;
import cn.lt.pullandloadmore.BaseLoadMoreRecyclerAdapter;


/**
 * Created by wenchao on 2016/3/8.
 * 应用适配器
 */
public class AppAdapter extends BaseLoadMoreRecyclerAdapter<AppBriefBean, RecyclerView.ViewHolder> {

    private String mPageName;
    private String mID;
    private String mListTitle;

    public AppAdapter(Context context, String pageName, String id, String listTitle) {
        super(context);
        this.mPageName = pageName;
        this.mID = id;
        this.mListTitle = listTitle;
        LTApplication.instance.from_id = id;
    }

    @Override
    public RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_app, parent, false);
        return new AppViewHolder(itemView);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, final int position) {
        AppViewHolder h = (AppViewHolder) holder;
        final AppBriefBean bean = getList().get(position);

        ImageloaderUtil.loadImage(mContext, bean.getIcon_url(), h.icon);
        h.name.setText(TextUtils.isEmpty(bean.getAlias()) ? bean.getName() : bean.getAlias());
        int count = 0;
        try {
            count = Integer.parseInt(bean.getDownload_count());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        h.downloadCount.setText(IntegratedDataUtil.calculateCounts(count));
        long pkgSize = 0;
        try {
            pkgSize = Long.parseLong(bean.getPackage_size());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        h.appSize.setText(IntegratedDataUtil.calculateSizeMB(pkgSize));
        h.appDesc.setText(bean.getReviews());

        AppEntity downloadAppEntity = bean.getDownloadAppEntity();
        downloadAppEntity.p1 = 0;
        downloadAppEntity.p2 = position + 1;
        downloadAppEntity.resource_type = "app";
        h.downloadButton.setData(downloadAppEntity, mPageName, mID);

        h.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIController.goAppDetail(mContext, bean.isAdData(), bean.getAdMold(), bean.getId(), bean.getAppClientId(), bean.getApps_type(), mPageName, bean.getCategory(), bean.getDownload_url(), bean.getReportData());   //第四个是否需要传参
                StatisticsEventData eventData = StatisticsDataProductorImpl.produceStatisticsData(TextUtils.isEmpty(mListTitle) ? "普通列表" : mListTitle, bean.getId(), mPageName, position + 1, "");
                DCStat.clickEvent(eventData);
            }
        });


    }

    class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        TextView downloadCount;
        TextView appSize;
        TextView appDesc;
        DownloadButton downloadButton;
        View itemView;


        public AppViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            name = (TextView) itemView.findViewById(R.id.name);
            downloadCount = (TextView) itemView.findViewById(R.id.download_count);
            appSize = (TextView) itemView.findViewById(R.id.app_size);
            appDesc = (TextView) itemView.findViewById(R.id.app_desc);
            downloadButton = (DownloadButton) itemView.findViewById(R.id.download_button);
            this.itemView = itemView;

        }
    }


}
