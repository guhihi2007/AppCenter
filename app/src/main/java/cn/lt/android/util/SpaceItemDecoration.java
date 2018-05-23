package cn.lt.android.util;

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by wenchao on 2015/11/26.
 * recycleView水平垂直间隔,支持GridLayoutManager 和 LinearLayoutManager
 */
public class SpaceItemDecoration extends RecyclerView.ItemDecoration{
    private int horizonalSpace;
    private int veritalSpace;
    private boolean hasHeader;

    public SpaceItemDecoration(int horizonalSpace, int veritalSpace){
        this.horizonalSpace = horizonalSpace;
        this.veritalSpace = veritalSpace;
    }

    public SpaceItemDecoration(int horizonalSpace, int veritalSpace, boolean hasHeader){
        this.horizonalSpace = horizonalSpace;
        this.veritalSpace = veritalSpace;
        this.hasHeader = hasHeader;
    }

    public void setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildPosition(view);
        int itemCount = parent.getAdapter().getItemCount();
        if(hasHeader) {
            if (position == 0) {
                return;
            }
            position--;
            itemCount--;
        }

        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if(layoutManager instanceof GridLayoutManager){
            //不在第一行
            int spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
            if(position / spanCount != 0){
                outRect.top = veritalSpace/2;
            }

            //不在最后一行
            int rowCount = (int) Math.ceil(itemCount*1.0f/spanCount);
            int currentRow = (int) Math.ceil((position+1)*1.0f/spanCount);
            if(currentRow != rowCount){
                outRect.bottom = veritalSpace/2;
            }

            //不在第一列
            if((position+1)%spanCount != 1){
                outRect.left = horizonalSpace/2;
            }

            //不在最后一列
            if((position+1)%spanCount != 0){
                outRect.right = horizonalSpace/2;
            }

        }else if(layoutManager instanceof LinearLayoutManager){
            if(((LinearLayoutManager) layoutManager).getOrientation() == LinearLayoutManager.HORIZONTAL){
                //水平
                int space = horizonalSpace/2;
                if(position!=0){
                    outRect.left = space;
                }
                if(position!= itemCount-1){
                    outRect.right = space;
                }
            }else{
                //垂直
                int space = veritalSpace/2;
                if(position!=0){
                    outRect.top = space;
                }
                if(position!=itemCount-1){
                    outRect.bottom = space;
                }
            }
        }
    }
}
