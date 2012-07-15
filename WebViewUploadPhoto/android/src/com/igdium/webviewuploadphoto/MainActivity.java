package com.igdium.webviewuploadphoto;

import android.net.Uri;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity {

    private static final String APPNAME = "WebViewUploadPhoto";
    private static final String HOME_URL = "http://172.20.20.5:9002/";
    private WebView webview;
    
    private class JavaScriptInterface {
       @SuppressWarnings("unused")
       public void alert(String msg) {
           Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
       }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        webview = (WebView) findViewById(R.id.webView);
        webview.loadUrl(HOME_URL);
        
        /* allow javascript */
        WebSettings ws = webview.getSettings();
        ws.setJavaScriptEnabled(true);

        /* use console.log */
        webview.setWebChromeClient(new WebChromeClient() {
            public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                Log.d(APPNAME, message + " -- From line "
                                            + lineNumber + " of "
                                            + sourceID);
              }
        });
        
        /* parse urls */ 
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d(APPNAME, "URL is " + url);
                if (url.startsWith(HOME_URL)) {
                    // This is my web site, so do not override; let my WebView load the page
                    return false;
                }
                // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }
        });
        
        /* setup javascript interface */
        webview.addJavascriptInterface(new JavaScriptInterface(), APPNAME);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if (keyCode == KeyEvent.KEYCODE_BACK && webview.canGoBack()) {
            webview.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    
}
