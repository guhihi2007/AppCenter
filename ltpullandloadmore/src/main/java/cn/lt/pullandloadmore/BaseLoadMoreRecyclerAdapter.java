package cn.lt.pullandloadmore;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.LinkedList;
import java.util.List;

import cn.lt.pullandloadmore.util.LogUtils;

/**
 * Created on 15/8/23.
 */
public abstract class BaseLoadMoreRecyclerAdapter<T, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter {
    public static final int TYPE_HEADER = Integer.MAX_VALUE;
    public static final int TYPE_FOOTER = Integer.MAX_VALUE - 1;
    public static final int TYPE_ITEM   = 0;
    private boolean hasFooter;//设置是否显示Footer
    private boolean hasHeader;//设置是否显示Header
    private View footerView;
    private View headerView;
    private final List<T> mList = new LinkedList<T>();
    protected Context mContext;

    public BaseLoadMoreRecyclerAdapter(Context context) {
        this.mContext = context;
    }

    //数据itemViewHolder 实现
    public abstract VH onCreateItemViewHolder(ViewGroup parent, int viewType);

    //数据itemViewHolder 实现
    public abstract void onBindItemViewHolder(final VH holder, int position);

    public int getViewType(int position) {
        return TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER) {//底部 加载view
            return new FooterViewHolder(footerView);
        } else if (viewType == TYPE_HEADER) {
            return new HeaderViewHolder(headerView);
        } else {
            //数据itemViewHolder
            return onCreateItemViewHolder(parent, viewType);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder( RecyclerView.ViewHolder holder, int position) {
//        if (holder instanceof FooterViewHolder) {
         if (position == getBasicItemCount() + (hasHeader ? 1 : 0) && hasFooter) {
             LogUtils.i("kkk", "要绑定底布局了：" );
        } else if (hasHeader && position == 0) {
            //更新headerview
        } else {
            int realPosition = position - (hasHeader ? 1 : 0);
            onBindItemViewHolder((VH) holder, realPosition);
        }
    }


    @Override
    public int getItemViewType(int position) {
        if (position == getBasicItemCount() + (hasHeader ? 1 : 0) && hasFooter) {
            return TYPE_FOOTER;
        } else if (position == 0 && hasHeader) {
            return TYPE_HEADER;
        }
        return getViewType(hasHeader ? position - 1 : position);//0
    }

    public List<T> getList() {
        return mList;
    }

    public void setList(List<T> list) {
        this.mList.clear();
        if (list != null) {
            this.mList.addAll(list);
        }
        this.notifyDataSetChanged();
    }

    public void appendToList(List<T> list) {
        if (list == null) {
            return;
        }
        mList.addAll(list);
        this.notifyDataSetChanged();
    }

    public void append(T t) {
        if (t == null) {
            return;
        }
        mList.add(t);
        this.notifyDataSetChanged();
    }

    public void appendToTop(T item) {
        if (item == null) {
            return;
        }
        mList.add(0, item);
        this.notifyDataSetChanged();
    }

    public void appendToTopList(List<T> list) {
        if (list == null) {
            return;
        }
        mList.addAll(0, list);
        this.notifyDataSetChanged();
    }


    public void remove(int position) {
        if (position < mList.size() - 1 && position >= 0) {
            mList.remove(position);
        }
        this.notifyDataSetChanged();
    }

    public void clear() {
        mList.clear();
        this.notifyDataSetChanged();
    }

    public int getBasicItemCount() {
        return mList.size();
    }

    @Override
    public int getItemCount() {
        return getBasicItemCount() + (hasFooter ? 1 : 0) + (hasHeader ? 1 : 0);
    }

    public T getItem(int position) {
        int realPosition = position;
        if (hasHeader) {
            realPosition--;
        }
        //若超出下标越界了.说明有footView
        if (realPosition < 0 || realPosition > mList.size() - 1) {
            return null;
        }
        return mList.get(realPosition);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public boolean hasFooter() {
        return hasFooter;
    }

    public void setHasFooter(boolean hasFooter) {
        if (this.hasFooter != hasFooter) {
            this.hasFooter = hasFooter;
            notifyDataSetChanged();
        }
    }

    /**
     * 设置底部是否可见
     */
    public void setFooterHint() {
        if(this.getBasicItemCount()<5){
           hasFooter=false;
        }
    }

    /**
     * 不显示头尾UI
     */
    public void setNoFooterAndHeader() {
            hasFooter=false;
            hasHeader=false;
    }

    /**
     * 不显示头尾UI
     */
    public void setNoHeader() {
        hasHeader=false;
    }

    /**
     * 不显示头尾UI
     */
    public void setNoFooter() {
        hasFooter=false;
    }

    public boolean hasHeader() {
        return hasHeader;
    }

    public void addHeaderView(View headerView) {
        this.hasHeader = true;
        this.headerView = headerView;
        notifyDataSetChanged();
    }
    public void addFooterView(View footerView) {
        this.hasFooter=true;
        this.footerView = footerView;
        notifyDataSetChanged();
    }

    /**
     * 承载外界传进来的刷新头布局
     */
    class HeaderViewHolder extends RecyclerView.ViewHolder {

        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }
    /**
     * 承载外界传进来的加载底布局
     */
    class FooterViewHolder extends RecyclerView.ViewHolder {

        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }
}
