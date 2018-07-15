package com.posco.fss.smartsafetyapp.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.posco.fss.smartsafetyapp.R;
import com.posco.fss.smartsafetyapp.core.AppEnv;

/**
 * class WebContentFragment
 * 각 모듈별 화면중 웹화면을 처리하는 플래그먼트
 * 대쉬보드, 위험물체크(블루투스)를 빼고 대부분의 모니터링은 웹데이터를 통해 구동한다.
 */
public class WebContentFragment extends Fragment {
    private WebView webview;
    private ProgressBar progress;
    private DashBoardActivity context;

    public WebContentFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = (DashBoardActivity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web_content, container, false);
        webview = (WebView) view.findViewById(R.id.webView);
        progress = (ProgressBar) view.findViewById(R.id.progressBar1);

        //로딩바 show
        if (progress != null) {
            progress.setVisibility(View.VISIBLE);
        }

        String title = getArguments().getString("title");
        context.changeTitleView(title);

        CookieSyncManager.getInstance().startSync();

        //초기화
        initWebView();

        //url get
        String url = getArguments().getString("url");

        if (url != null) {
            webview.loadUrl(url);
        }

        return view;
    }

    private void initWebView() {
        webview.setVisibility(View.VISIBLE);
        webview.setBackgroundColor(Color.WHITE);
        webview.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
        webview.addJavascriptInterface(new fssApp(), "fssApp");
        webview.setVerticalScrollBarEnabled(true);
        webview.setHorizontalScrollBarEnabled(true);
        webview.requestFocus(View.FOCUS_DOWN);
        webview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!view.hasFocus()) {
                            view.requestFocus();
                        }
                        break;
                }
                return false;
            }
        });
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportMultipleWindows(true);
        //webSettings.setPluginsEnabled(true);
        //webSettings.setSupportZoom(true);
        //webSettings.setBuiltInZoomControls(true);
        webview.setWebViewClient(new MyWebViewClient());
        setWebChromeClient();
        clear();
    }

    public void onBackWebpage(){
        if (webview != null
                && webview.getVisibility() == View.VISIBLE) {
            if (webview.canGoBack()) {
                webview.goBack();
            } else {
                DashBoardActivity activity = (DashBoardActivity)context;
                activity.mainViewShow();
            }
        }
    }


    private final class MyWebViewClient extends WebViewClient {
        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //Log.d("wv", "s:" + url);
            if ("about:blank".equals(url)) {
                return false;
            }

            if (url.startsWith("http://") || url.startsWith("https://")) {
                webview.loadUrl(url);
                return true;
            }


            if(url.startsWith("fsf://")){
                //앱스키마를 콜한 로직
                //Intent called = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                //startActivity(called);
                return true;
            }

            return false;
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            final Uri uri = request.getUrl();
            return false;
        }


        @Override
        public void onPageFinished(WebView view, String url) {

            //webview.loadUrl("javascript:fssApp.resize(document.body.getBoundingClientRect().height)");

            //로딩바 hide
            if (progress != null) {
                progress.setVisibility(View.INVISIBLE);
            }

            super.onPageFinished(view, url);

        }
    }

    private void setWebChromeClient() {
        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message,
                                     final android.webkit.JsResult result) {
                if (message == null || "null".equals(message) || "[error]\n\n".equals(message)) {
                    result.cancel();
                    return true;
                }
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("알림")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                new AlertDialog.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        result.confirm();
                                    }
                                }).setCancelable(false).create();
                dialog.show();
                //alert창 컬러변경을 위한설정
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);


                return true;
            }


            @Override
            public boolean onJsConfirm(WebView view, String url,
                                       String message, final android.webkit.JsResult result) {
                /*new AlertDialog.Builder(view.getContext())
                        .setTitle((titlePage == null ? "" : "'" + titlePage +  "' ") + "페이지 내용")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        result.confirm();
                                    }
                                })
                        .setNegativeButton(android.R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        result.cancel();
                                    }
                                }).create().show();*/

                return true;
            }

            @Override
            public void onCloseWindow(WebView window) {
                webview.removeView(window);
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog,
                                          boolean isUserGesture, Message resultMsg) {
                return false;
            }

        });
    }

    @Override
    public void onPause() {
        super.onPause();
        CookieSyncManager.getInstance().stopSync();
    }

    @Override
    public void onResume() {
        super.onResume();
        CookieSyncManager.getInstance().startSync();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(webview != null){
            webview = null;
        }
    }

    public void clear() {
        webview.clearCache(true);
        webview.clearHistory();
    }

    private class fssApp{
        @JavascriptInterface
        public void onMessagePrint(String message){
            Log.i("MESSAGE TAG", "message : " + message);
        }

        @JavascriptInterface
        public void resize(final float height) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webview.setLayoutParams(new LinearLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels,
                            (int) (height * getResources().getDisplayMetrics().density)));
                }
            });
        }

    }

}
