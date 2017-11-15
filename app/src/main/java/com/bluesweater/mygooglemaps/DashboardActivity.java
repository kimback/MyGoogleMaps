package com.bluesweater.mygooglemaps;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.CookieSyncManager;
import android.widget.TextView;

import com.bluesweater.mygooglemaps.core.ApplicationMaps;
import com.bluesweater.mygooglemaps.core.MapsPreference;

import layout.NaviDrawFragment;

public class DashboardActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView titleText;
    private Fragment contentFragment;
    private NaviDrawFragment naviDrawFragment;
    private DrawerLayout drawerLayout;
    private int currDisplay = 0; //default
    private MapsPreference pref;

    //메인 플래그먼트에 띄울 컨텐츠
    public static final int DISPLAY_CONTENT_MAIN = 0;
    public static final int DISPLAY_CONTENT_GOGO_SNOWWORLD = 1;
    public static final int DISPLAY_CONTENT_APP_SETTING = 2;
    public static final int DISPLAY_CONTENT_APP_LOGOUT = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.i("상태TAG","--------------------DashBoardActivity create----------------------");
        setContentView(R.layout.activity_dashboard);
        pref = ApplicationMaps.getMapsPreference();
        ApplicationMaps.getApps().setDashboardActivity(this);

        //레이아웃초기화 및 로드
        initLayout();

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //포그라운드 상태에서 intent activity 되어졌을때 called
    }

    @Override
    protected void onRestart() {
        //Log.i("상태TAG","--------------------DashBoardActivity restart----------------------");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        //Log.i("상태TAG","--------------------DashBoardActivity resume----------------------");
        super.onResume();
        CookieSyncManager.getInstance().startSync();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CookieSyncManager.getInstance().stopSync();
    }

    @Override
    protected void onStop() {
        //Log.i("상태TAG","--------------------DashBoardActivity stop----------------------");
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        //그밖에 상황(뎁스들안에있을때)
        if (checkBackPressed()) {
            return;
        }

        //앱 나갈때
        else {
            new AlertDialog.Builder(this, R.style.Theme_AppCompat_Dialog)
                    .setTitle("종료하시겠습니까?")
                    .setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {

                                    finish();
                                }
                            })
                    .setNegativeButton("취소",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                }
                            }).show();
        }
        // super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.sample_actions, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //홈 메뉴 선택시
            case android.R.id.home:
                //레프트 메뉴 오픈
                drawerLayout.openDrawer(Gravity.START, true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    private boolean checkBackPressed() {
        //main은 웹뷰
        if(currDisplay == DashboardActivity.DISPLAY_CONTENT_MAIN) {
            WebContentFragment wcf = (WebContentFragment) contentFragment;

            if(wcf.isBackWebpage()){
                wcf.onBackWebpage();
                return true;
            }

            return false;

        }else if(currDisplay == DashboardActivity.DISPLAY_CONTENT_GOGO_SNOWWORLD){
            mainViewShow();
            return true;

        }else{
            return false;
        }
    }

    public void mainViewShow(){
        //String userId = pref.getLoginId();

        //메인 컨텐츠 로드
        String url = ApplicationMaps.mainUrl;
                //+ "&selectedSkiResort=" + pref.getSelectedSkiResort() + "&userId=" + userId;
        displayContent(DISPLAY_CONTENT_MAIN, url, "내정보");

    }

    /**
     * 메인액티비티의 화면을 구성한다
     */
    private void initLayout(){
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        titleText = (TextView) findViewById(R.id.toolbar_title_text);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        //툴바셋팅
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        //커스텀 네비드로우 작업
        naviDrawFragment = (NaviDrawFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_navi);
        //네비드로우 사이즈가 화면에 3/1만 차지하도록
        int width = getResources().getDisplayMetrics().widthPixels / 3;
        ViewGroup.LayoutParams params = naviDrawFragment.getView().getLayoutParams();
        params.width = (width * 2);
        naviDrawFragment.getView().setLayoutParams(params);
        naviDrawFragment.setUp(R.id.fragment_navi, drawerLayout, toolbar);

        CookieSyncManager.createInstance(this);

        //메인 컨텐츠 로드
        mainViewShow();
        //네비드로우 셋팅 (디폴트 드로우 메뉴 사용시)
        //setupNaviDrawer(drawerLayout);
    }

    /**
     * displayContent
     * @param display
     * 메인화면에 바디는 플래그먼트로 구성하여 각 모듈별 화면 교체
     */
    public void displayContent(int display, String url, String title){
        switch (display){

            case DISPLAY_CONTENT_MAIN :
                //메인 대쉬보드
                currDisplay = display;
                contentFragment = new WebContentFragment();

                //웹url
                Bundle bundle1 = new Bundle();
                bundle1.putString("title", title);
                bundle1.putString("url", url);
                contentFragment.setArguments(bundle1);

                chageFragmentView(contentFragment);
                break;

            case DISPLAY_CONTENT_GOGO_SNOWWORLD :
                //웹뷰컨텐츠
                currDisplay = display;
                contentFragment = new SnowGoGoFragment();

                //웹url
                Bundle bundle2 = new Bundle();
                bundle2.putString("title", title);
                bundle2.putString("url", url);
                contentFragment.setArguments(bundle2);

                chageFragmentView(contentFragment);
                break;

            case DISPLAY_CONTENT_APP_SETTING :
                //앱 설정
                currDisplay = display;
                //Intent appSettingIntent = new Intent(this, SettingActivity.class);
                //startActivityForResult(appSettingIntent, 0);
                break;

            case DISPLAY_CONTENT_APP_LOGOUT :
                //로그아웃처리
                if(pref.getLoginId() != ""){
                    pref.setLoginId("");
                    pref.setSelectedSkiResort("");
                    pref.appPrefSave();

                    //로그인 페이지로 intent
                    Intent loginIntent = new Intent(this, LoginWorkActivity.class);
                    startActivity(loginIntent);
                    finish();
                }
                break;

            default:
                break;

        }

    }


    public void changeTitleView(String title){
        titleText.setText(title);
    }

    public int getCurrDisplay() {
        return currDisplay;
    }

    public void setCurrDisplay(int currDisplay) {
        this.currDisplay = currDisplay;
    }

    private void chageFragmentView(Fragment contentFragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_layout, contentFragment);
        fragmentTransaction.commit();
    }


    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onActivityResult(int, int,
     * android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if(resultCode == 1){
                    // 셋팅화면에서 들어온 값 처리할 경우
                }
                break;

        }
    }

    @Override
    protected void onDestroy() {
        //Log.i("상태TAG","--------------------DashBoardActivity destroy----------------------");
        super.onDestroy();
        if (contentFragment != null) {
            contentFragment = null;
        }
        ApplicationMaps.getApps().closeApplication();

    }
}
