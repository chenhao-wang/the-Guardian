/*
 * Copyright (C) 2013 Andreas Stuetz <andreas.stuetz@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.theguardian;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.android.theguardian.data.NewsContract;
import com.android.theguardian.sync.TheguardianSyncAdapter;

public class NewsFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener {

    private static final String ARG_TITLE = "title";

    private String title;

    private NewsAdapter mNewsAdapter;
    private ListView mListView;
    ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    public static NewsFragment newInstance(String title) {
        NewsFragment f = new NewsFragment();
        Bundle b = new Bundle();
        b.putString(ARG_TITLE, title);
        f.setArguments(b);
        if (title != null) {
            f.setTitle(title);
        }
        return f;
    }

    @Override
    public void initVariables(Bundle bundle) {
        title = bundle.getString(ARG_TITLE);
    }

    @Override
    protected View initViews(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_news, container, false);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
        mListView = (ListView) rootView.findViewById(R.id.list_view);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);

        return rootView;
    }

    @Override
    protected void initData() {


        mNewsAdapter = new NewsAdapter(getActivity(), null, 0);
        mListView.setAdapter(mNewsAdapter);
        mListView.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);
        onRefresh();

        TheguardianSyncAdapter.syncImmediately(getActivity(), title);
        getLoaderManager().initLoader(0, null, this);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = mNewsAdapter.getCursor();
                cursor.moveToPosition(position);
                String webUrl = cursor.getString(cursor.getColumnIndex(NewsContract.NewsEntry.COLUMN_WEB_URL));

                Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                detailIntent.putExtra("web_url", webUrl);
                startActivity(detailIntent);
            }
        });
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(getActivity(),
                NewsContract.NewsEntry.CONTENT_URI,
                null,
                NewsContract.NewsEntry.COLUMN_SECTION + "=?",
                new String[]{title},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        swipeRefreshLayout.setRefreshing(false);
        progressBar.setVisibility(View.GONE);
        mNewsAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        swipeRefreshLayout.setRefreshing(false);
        progressBar.setVisibility(View.GONE);
        mNewsAdapter.swapCursor(null);
    }

    public void refreshData(String title) {
        if (title != null) {
            this.title = title;

            Bundle args = getArguments();
            if (args != null) {
                args.putString(ARG_TITLE, title);
            }

            if (mListView != null) {
                mListView.setVisibility(View.GONE);
            }
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }

            if (isFragmentVisible()) {
                initData();
            } else {
                setForceLoad(true);
            }
        }
    }

    @Override
    protected void setDefaultFragmentTitle(String title) {

    }

    @Override
    public void onRefresh() {
        Log.v("News Fragment onRefresh", "success");
        TheguardianSyncAdapter.syncImmediately(getActivity(), title);
    }
}