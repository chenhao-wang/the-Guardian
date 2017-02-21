package com.android.theguardian.data;


import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class NewsContract {
    public static final String CONTENT_AUTHORITY = "com.android.theguardian";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_NEWS = "news";

    public static final class NewsEntry implements BaseColumns{

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendEncodedPath(PATH_NEWS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NEWS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NEWS;

        public static final String TABLE_NAME = "news";

        public static final String COLUMN_SECTION = "section";

        public static final String COLUMN_TITLE = "title";

        public static final String COLUMN_THUMBNAIL_URL="thumbnail";

        public static final String COLUMN_WEB_URL = "web_url";

    }

}
