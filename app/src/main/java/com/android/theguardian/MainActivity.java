package com.android.theguardian;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.theguardian.sync.TheguardianSyncAdapter;
import com.android.theguardian.tagdragview.EasyTipDragView;
import com.android.theguardian.tagdragview.TipDataModel;
import com.android.theguardian.tagdragview.bean.SimpleTitleTip;
import com.android.theguardian.tagdragview.bean.Tip;
import com.android.theguardian.tagdragview.widget.TipItemView;
import com.astuetz.PagerSlidingTabStrip;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback {
    public static final String PREFS_TAG = "tag_selected";
    public static final String[] ALL_TAGS = {"world", "US", "home", "politics", "opinion", "sports", "soccer", "tech", "arts", "lifestyle", "fashion",
            "business", "travel", "envir", "science", "media", "video", "podcasts"};
    public static final int ALL_TAGS_SIZE = ALL_TAGS.length;

    private final Handler handler = new Handler();

    private PagerSlidingTabStrip tabs;
    private ViewPager pager;
    private MyPagerAdapter adapter;

    private EasyTipDragView easyTipDragView;


    private List<String> tagSelected = new ArrayList<>();

    InterstitialAd mInterstitialAd;

    public static GoogleApiClient mGoogleApiClient;

    private TextView addressField;

    private Location mLastLocation;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*set default tags*/
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.PREFS_TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("0", "world");
        editor.putString("1", "US");
        editor.putString("2", "home");
        editor.putString("3", "politics");
        editor.putString("4", "opinion");
        editor.putString("5", "sports");
        editor.putString("6", "soccer");
        editor.putString("7", "tech");
        editor.putString("8", "arts");
        editor.putString("9", "lifestyle");
        editor.putString("10", "fashion");
        editor.putString("11", "business");
        editor.putString("12", "travel");
        editor.putString("13", "envir");
        editor.putString("14", "science");
        editor.putString("15", "media");
        editor.putString("16", "video");
        editor.putString("17", "podcasts");
        editor.apply();

        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setOffscreenPageLimit(0);

        adapter = new MyPagerAdapter(getSupportFragmentManager());
        tagSelected = Utils.getSelectedTags(this);
        adapter.init(tagSelected);
        pager.setAdapter(adapter);
        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        pager.setPageMargin(pageMargin);

        tabs.setViewPager(pager);

        easyTipDragView = (EasyTipDragView) findViewById(R.id.easy_tip_drag_view);
        //设置已包含的标签数据
        easyTipDragView.setAddData(TipDataModel.getAddTips(this));
        //设置可以添加的标签数据
        easyTipDragView.setDragData(TipDataModel.getDragTips(this));
        //在easyTipDragView处于非编辑模式下点击item的回调（编辑模式下点击item作用为删除item）
        easyTipDragView.setSelectedListener(new TipItemView.OnSelectedListener() {
            @Override
            public void onTileSelected(Tip entity, int position, View view) {
                toast(((SimpleTitleTip) entity).getTip());
            }
        });
        //设置每次数据改变后的回调（例如每次拖拽排序了标签或者增删了标签都会回调）
        easyTipDragView.setDataResultCallback(new EasyTipDragView.OnDataChangeResultCallback() {
            @Override
            public void onDataChangeResult(ArrayList<SimpleTitleTip> tips) {
//                Log.i("heheda", tips.toString());
                tagSelected.clear();
                SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.PREFS_TAG, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                int dragSize = tips.size();
                editor.putInt("size", dragSize);
                List<String> addTags = new ArrayList<String>(Arrays.asList(ALL_TAGS));

                for (int i = 0; i < tips.size(); i++) {
                    String tag = tips.get(i).getTip();
                    addTags.remove(tag);
                    editor.putString(String.valueOf(i), tag);
                    tagSelected.add(tag);
                }
                for (int i = 0; i < addTags.size(); i++) {
                    String tag = addTags.get(i);
                    editor.putString(String.valueOf(i + dragSize), tag);
                }
                editor.apply();
                adapter.init(tagSelected);
                adapter.notifyDataSetChanged();
                tabs.notifyDataSetChanged();

            }
        });

        easyTipDragView.setOnCompleteCallback(new EasyTipDragView.OnCompleteCallback() {
            @Override
            public void onComplete(ArrayList<SimpleTitleTip> tips) {
                toast("Final Tags：" + tips.toString());
            }
        });

        TheguardianSyncAdapter.initializeSyncAdapter(this);
        /*Ad service*/
        AdView mAdView = (AdView) findViewById(R.id.adView);

        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);

        // Instantiate the InterstitialAd object
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");

        // Create the AdListener
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });

        requestNewInterstitial();

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        addressField = (TextView) findViewById(R.id.location_text_view);
    }

    @Override
    protected void onRestart() {
        mGoogleApiClient.reconnect();
        super.onResume();
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.v("MainActivity", "Google Api onConnected");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        } else {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            getLocation();
        }
    }

    private void getLocation() {

        if (mLastLocation != null) {
            //You had this as int. It is advised to have Lat/Loing as double.
            double lat = mLastLocation.getLatitude();
            double lng = mLastLocation.getLongitude();

            Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
            StringBuilder builder = new StringBuilder();
            try {
                List<Address> address = geoCoder.getFromLocation(lat, lng, 1);
                int maxLines = address.get(0).getMaxAddressLineIndex();
                for (int i = 0; i < maxLines; i++) {
                    String addressStr = address.get(0).getAddressLine(i);
                    builder.append(addressStr);
                    builder.append(" ");
                }

                String finalAddress = builder.toString(); //This is the complete address.
                addressField.setText(finalAddress); //This will display the final address.
            } catch (IOException e) {
                // Handle IOException
            } catch (NullPointerException e) {
                // Handle NullPointerException
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    mGoogleApiClient.reconnect();
//                    getLocation();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    // Request new interstitial
    private void requestNewInterstitial() {
        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_add_tab:

                easyTipDragView.open();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void toast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (easyTipDragView.isOpen()) {
                    if (!easyTipDragView.onKeyBackDown()) {

                    }
                    return true;
                }

                break;
        }
        return super.onKeyDown(keyCode, event);
    }

}

