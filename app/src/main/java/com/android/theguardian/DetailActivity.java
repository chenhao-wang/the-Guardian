package com.android.theguardian;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        String webUrl = intent.getStringExtra("web_url");
        WebView webView =(WebView) findViewById(R.id.web_view);
        webView.loadUrl(webUrl);
    }
}
