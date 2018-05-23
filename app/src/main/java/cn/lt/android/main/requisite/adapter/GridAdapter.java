package cn.lt.android.main.requisite.adapter;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.main.requisite.RequisiteActivity.RequisiteItem;
import cn.lt.android.main.requisite.widget.RequisiteGameView;


public class GridAdapter extends BaseAdapter {

    private List<RequisiteItem> mList;


    @SuppressLint("UseSparseArrays")
    public GridAdapter(List<RequisiteItem> list) {
        setList(list);
    }

    public List<RequisiteItem> getList() {
        return mList;
    }

    public void setList(List<RequisiteItem> list) {
        if (list == null) {
            mList = new ArrayList<>();
        } else {
            mList = list;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GameHolder gameView = null;

        if (convertView == null) {
            gameView = new GameHolder();
            convertView = new RequisiteGameView(parent.getContext());
            gameView.view = (RequisiteGameView) convertView;
            convertView.setTag(gameView);
        } else {
            gameView = (GameHolder) convertView.getTag();
        }
        fillItem(gameView, position);
        return convertView;
    }

    private void fillItem(GameHolder gameView, int position) {
        try {
            gameView.view.fillView(mList.get(position));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class GameHolder {
        public RequisiteGameView view;
    }

}
