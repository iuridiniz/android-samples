package com.igdium.webviewuploadphoto;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Scanner;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.webkit.MimeTypeMap;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

@SuppressLint("SetJavaScriptEnabled")
public class UploadPhoto extends Activity {

    private static final String APPNAME = "WebViewUploadPhoto";
    //private static final String HOME_URL = "http://172.20.20.5:9002/";
    private static final String HOME_URL = "http://172.16.0.57:9002/";
    private WebView webview;
    private JavaScriptInterface jsInterface;
    private static final int REQUEST_IMAGE_GALLERY_CODE = 1;
    private static final int REQUEST_IMAGE_CAMERA_CODE = 2;
    private class JavaScriptInterface {
        private static final String IOERROR = "IOERROR";
        private static final String NOTFOUND = "NOTFOUND";
        private static final String CANCELED = "CANCELED";
        private String jsCallBack;

        private void getPhotoFromGallery() {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, REQUEST_IMAGE_GALLERY_CODE);
        }
        
        private void getPhotoFromCamera() {
            Intent intent = new Intent();
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_IMAGE_CAMERA_CODE);
        }
        
        @SuppressWarnings("unused")
        public void alert(String msg) {
            Toast.makeText(UploadPhoto.this, msg, Toast.LENGTH_SHORT).show();
        }
       

        @SuppressWarnings("unused")
        public void takePhoto(String cb) {
            /* save callback */
            jsCallBack = cb;
            getPhotoFromCamera();
        }
        
        @SuppressWarnings("unused")
        public void pickPhoto(String cb) {
            /* save callback */
            jsCallBack = cb;
            getPhotoFromGallery();
        }
        
        private void doImageCallBack(String data, String filename, String err) {
            /* FIXME: SECURITY WARNING --> untrusted way to run a callback */
            data = escape(data);
            filename = escape(filename);
            err = escape(err);
            if (jsCallBack != null) {
                String call = "javascript:(%s)(%s, %s, %s)";
                call = String.format(call, jsCallBack, data, filename, err);
                //Log.d(APPNAME, "Running: " + call);
                webview.loadUrl(call);
                jsCallBack = null;
            }
        }

        private String escape(String str) {
            /* FIXME: MUST escape strings to javascript*/
            if (str == null) {
                str = "null";
            } else {
                str = "'" + str + "'";
            }
            return str;
        }

        private void userCancel() {
              doImageCallBack(null, null, CANCELED);
        }

        private byte[] getFileBytes(InputStream ios) throws IOException {
            /* got from: http://pastebin.com/rFBB03eV */
            ByteArrayOutputStream ous = null;
            //InputStream ios = null;
            try {
                byte[] buffer = new byte[4096];
                ous = new ByteArrayOutputStream();
                //ios = new FileInputStream(file);
                int read = 0;
                while ((read = ios.read(buffer)) != -1)
                    ous.write(buffer, 0, read);
            } finally {
                try {
                    if (ous != null)
                        ous.close();
                } catch (IOException e) {
                    // swallow, since not that important
                }
                try {
                    if (ios != null)
                        ios.close();
                } catch (IOException e) {
                    // swallow, since not that important
                }
            }
            return ous.toByteArray();
        }
        
        public void gotImage(Uri uri) {
            String data;
            String mime_type;
            InputStream is;
            ContentResolver cR = getContentResolver();
            Log.d(APPNAME, "Got Image: " + uri);
            if (uri == null) {
                doImageCallBack(null, null, NOTFOUND);
                return;
            }
            
            mime_type = cR.getType(uri);
            Log.d(APPNAME, "mime-type:" + mime_type);
            

            try {
                is = getContentResolver().openInputStream(uri);
                data = Base64.encodeBytes(getFileBytes(is));
                is.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                doImageCallBack(null, null, NOTFOUND);
                return;
            } catch (IOException e) {
                e.printStackTrace();
                doImageCallBack(null, null, IOERROR);
                return;
            }

            doImageCallBack("data:" + mime_type + ";base64," + data, uri.getLastPathSegment(), null);
            
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
        
        jsInterface = new JavaScriptInterface();
        webview.addJavascriptInterface(jsInterface, APPNAME);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_GALLERY_CODE || requestCode == REQUEST_IMAGE_CAMERA_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                Log.d(APPNAME, "User Cancelled REQUEST_IMAGE_GALLERY_CODE");
                jsInterface.userCancel();
            } else {
                jsInterface.gotImage(data.getData());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    
}
