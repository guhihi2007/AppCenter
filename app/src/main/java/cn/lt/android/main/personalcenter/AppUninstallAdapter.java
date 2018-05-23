package cn.lt.android.main.personalcenter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import cn.lt.android.entity.UninstallAppInfo;
import cn.lt.android.install.root.PackageUtils;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.IntegratedDataUtil;
import cn.lt.android.util.PkgSizeObserver;
import cn.lt.appstore.R;
import cn.lt.framework.util.TimeUtils;
import cn.lt.pullandloadmore.BaseLoadMoreRecyclerAdapter;

/**
 * Created by wenchao on 2016/3/15.
 */
public class AppUninstallAdapter extends BaseLoadMoreRecyclerAdapter<UninstallAppInfo, AppUninstallAdapter.AppUninstallViewHolder> {
    public AppUninstallAdapter(Context context) {
        super(context);
    }

    @Override
    public AppUninstallViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_app_uninstall, parent, false);
        return new AppUninstallViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(final AppUninstallViewHolder h, int position) {
        final UninstallAppInfo packageInfo = getList().get(position);
        h.icon.setImageDrawable(packageInfo.icon);
        h.name.setText(packageInfo.name);
        if (packageInfo.firstInstallTime > System.currentTimeMillis()) {
            h.installTime.setText(TimeUtils.getTime(packageInfo.firstInstallTime, TimeUtils.DATE_FORMAT_DATE));
        } else {
            h.installTime.setText(TimeUtils.getTimeAgo(mContext, packageInfo.firstInstallTime) + mContext.getString(R.string.install));
        }
        h.uninstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            PackageUtils.uninstallNormal(mContext, packageInfo.packageName);
            }
        });

        if (packageInfo.size <= 0) {
            h.size.setText(R.string.computing);

            h.size.setTag(packageInfo.packageName);
            AppUtils.queryPackageSize(mContext, packageInfo.packageName, new PkgSizeObserver.OnPackageSizeListener() {
                @Override
                public void computeComplete(String packageName, long cacheSize, long dataSize, long codeSize) {
                    long totalSize = cacheSize + dataSize + codeSize;
                    packageInfo.size = totalSize;

                    Object tag = h.size.getTag();
                    if (tag != null && tag.toString().equals(packageName)) {
                        h.size.setText(IntegratedDataUtil.calculateSizeMB(totalSize));
                    }
                }
            });
        } else {
            h.size.setText(IntegratedDataUtil.calculateSizeMB(packageInfo.size));
        }

    }

    class AppUninstallViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        TextView size;
        TextView installTime;
        TextView uninstall;

        AppUninstallViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            name = (TextView) itemView.findViewById(R.id.name);
            size = (TextView) itemView.findViewById(R.id.size);
            installTime = (TextView) itemView.findViewById(R.id.install_time);
            uninstall = (TextView) itemView.findViewById(R.id.uninstall);
        }
    }

}
