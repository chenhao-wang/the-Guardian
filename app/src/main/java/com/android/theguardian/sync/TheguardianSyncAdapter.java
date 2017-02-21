package com.android.theguardian.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.android.theguardian.BuildConfig;
import com.android.theguardian.R;
import com.android.theguardian.data.NewsContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

public class TheguardianSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = TheguardianSyncAdapter.class.getSimpleName();

    public static final int SYNC_INTERVAL = 60 * 180;

    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    private static String mSection;

    private URL url;

    public TheguardianSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        try {

            final String FORECAST_BASE_URL =
                    "http://content.guardianapis.com/search?";
            final String QUERY_PARAM = "q";
            final String PAGE_SIZE_PARAM = "page-size";
            final String ORDER_BY_PARAM = "order-by";
            final String SHOW_FIELDS = "show-fields";
            final String APPID_PARAM = "api-key";

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, mSection)
                    .appendQueryParameter(PAGE_SIZE_PARAM, "20")
                    .appendQueryParameter(ORDER_BY_PARAM, "relevance")
                    .appendQueryParameter(SHOW_FIELDS, "thumbnail")
                    .appendQueryParameter(APPID_PARAM, BuildConfig.THEGUARDIAN_API_KEY)
                    .build();

            url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            forecastJsonStr = buffer.toString();
            getNewsDataFromJson(forecastJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getNewsDataFromJson(String forecastJsonStr)
            throws JSONException {

        try {
            JSONObject newsJson = new JSONObject(forecastJsonStr);
            JSONObject response = newsJson.getJSONObject("response");
            JSONArray results = response.getJSONArray("results");

            // Insert the new weather information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(results.length());

            for (int i = 0; i < results.length(); i++) {
                ContentValues weatherValues = new ContentValues();

                JSONObject singleNews = results.getJSONObject(i);
                String title = singleNews.getString("webTitle");
                String webUrl = singleNews.getString("webUrl");

                if (singleNews.has("fields")) {
                    JSONObject fields = singleNews.getJSONObject("fields");
                    String thumbnailUrl = fields.getString("thumbnail");
                    weatherValues.put(NewsContract.NewsEntry.COLUMN_THUMBNAIL_URL, thumbnailUrl);
                }


                weatherValues.put(NewsContract.NewsEntry.COLUMN_TITLE, title);
                weatherValues.put(NewsContract.NewsEntry.COLUMN_WEB_URL, webUrl);
                weatherValues.put(NewsContract.NewsEntry.COLUMN_SECTION, mSection);


                cVVector.add(weatherValues);
            }

            // add to database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);

                // delete old data so we don't build up an endless history
                getContext().getContentResolver().delete(NewsContract.NewsEntry.CONTENT_URI,
                        NewsContract.NewsEntry.COLUMN_SECTION + "=?",
                        new String[]{mSection});

                getContext().getContentResolver().bulkInsert(NewsContract.NewsEntry.CONTENT_URI, cvArray);
            }

            Log.d(LOG_TAG, "Sync Complete. " + cVVector.size() + " Inserted" + ",URL=" + url.toString());

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = NewsContract.CONTENT_AUTHORITY;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context, String title) {
        mSection = title;
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                NewsContract.CONTENT_AUTHORITY, bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        TheguardianSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, NewsContract.CONTENT_AUTHORITY, true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context, "");
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
