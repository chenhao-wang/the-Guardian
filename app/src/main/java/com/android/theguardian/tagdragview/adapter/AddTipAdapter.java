package com.android.theguardian.tagdragview.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.theguardian.R;
import com.android.theguardian.tagdragview.bean.SimpleTitleTip;

import java.util.List;


public class AddTipAdapter extends BaseAdapter {

    private List<SimpleTitleTip> tips;

    public AddTipAdapter() {
    }

    @Override
    public int getCount() {
        if (tips == null) {
            return 0;
        }
        return tips.size();
    }

    @Override
    public Object getItem(int position) {
        return tips.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = View.inflate(parent.getContext(), R.layout.view_add_item, null);
        ((TextView) view.findViewById(R.id.tagview_title)).setText((((SimpleTitleTip) (tips.get(position))).getTip()));
        return view;
    }

    public List<SimpleTitleTip> getData() {
        return tips;
    }

    public void setData(List<SimpleTitleTip> iDragEntities) {
        this.tips = iDragEntities;
    }

    public void refreshData() {
        notifyDataSetChanged();
    }
}
