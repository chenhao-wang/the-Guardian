package com.android.theguardian;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.theguardian.data.NewsContract;
import com.squareup.picasso.Picasso;

public class NewsAdapter extends CursorAdapter {

    public static class ViewHolder {
        public final ImageView thumbnail;
        public final TextView title;

        public ViewHolder(View view) {
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            title = (TextView) view.findViewById(R.id.web_title);
        }
    }

    public NewsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String thumbnailUrl = cursor.getString(cursor.getColumnIndex(NewsContract.NewsEntry.COLUMN_THUMBNAIL_URL));
        if (thumbnailUrl != null) {
            Picasso.with(context).load(thumbnailUrl).into(viewHolder.thumbnail);
        }
        String title = cursor.getString(cursor.getColumnIndex(NewsContract.NewsEntry.COLUMN_TITLE));
        viewHolder.title.setText(title);
    }
}
