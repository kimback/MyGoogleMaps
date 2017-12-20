package com.bluesweater.mygooglemaps;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bluesweater.mygooglemaps.core.ApplicationMaps;
import com.bluesweater.mygooglemaps.core.MapsPreference;

public class NaviDrawFragment extends Fragment {
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private View containerView;
    private DashboardActivity context;

    //navi btn define
    private RelativeLayout naviMenuProfile;
    private RelativeLayout naviMenuBtn0;
    private RelativeLayout naviMenuBtn1;
    private RelativeLayout naviMenuBtn2;
    private RelativeLayout naviMenuBtn9;
    private RelativeLayout naviMenuBtn10;
    private RelativeLayout naviMenuLogout;
    private TextView txtUserId;

    private MapsPreference pref = ApplicationMaps.getMapsPreference();


    public NaviDrawFragment(){

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = (DashboardActivity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_navi_draw, container, false);
        initLayout(layout);

        return layout;
    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout, final Toolbar toolbar) {
        containerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, toolbar, R.string.draw_open, R.string.draw_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActivity().invalidateOptionsMenu();

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                toolbar.setAlpha(1 - slideOffset / 2);
            }
        };

        //mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();

            }
        });

    }

    private void initLayout(View v){
        txtUserId = (TextView) v.findViewById(R.id.text_userId);
        txtUserId.setText(pref.getLoginId() + " 님");

        naviMenuProfile = (RelativeLayout) v.findViewById(R.id.navi_menu_profile);
        naviMenuProfile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                clickAction(v);
            }
        });

        naviMenuBtn0 = (RelativeLayout) v.findViewById(R.id.navi_menu_btn0);
        naviMenuBtn0.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                clickAction(v);
            }
        });

        naviMenuBtn1 = (RelativeLayout) v.findViewById(R.id.navi_menu_btn1);
        naviMenuBtn1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                clickAction(v);
            }
        });

        naviMenuBtn2 = (RelativeLayout) v.findViewById(R.id.navi_menu_btn2);
        naviMenuBtn2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                clickAction(v);
            }
        });

        naviMenuBtn9 = (RelativeLayout) v.findViewById(R.id.navi_menu_btn9);
        naviMenuBtn9.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                clickAction(v);
            }
        });

        naviMenuLogout = (RelativeLayout) v.findViewById(R.id.navi_menu_logout);
        naviMenuLogout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                clickAction(v);
            }
        });

        naviMenuBtn10 = (RelativeLayout) v.findViewById(R.id.navi_menu_btn10);
        naviMenuBtn10.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                clickAction(v);
            }
        });
    }

    private void clickAction(View v){
        switch (v.getId()) {
            case R.id.navi_menu_profile :
                mDrawerLayout.closeDrawers();
                //프로필 클릭시 처리
                break;

            //전체전투력 (웹뷰) total rank
            case R.id.navi_menu_btn0 :
                //menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                context.mainViewShow("RANK");
                break;

            //개인전투력(웹뷰) private rank
            case R.id.navi_menu_btn1 :
                //menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                context.mainViewShow("MYDATA");
                break;

            //그룹관리화면
            case R.id.navi_menu_btn10 :
                //준비중 . . .
                //mDrawerLayout.closeDrawers();
                //context.displayContent(DashboardActivity.DISPLAY_CONTENT_GROUP_CREATE, "", "그룹관리");
                break;


            //전투력측정기
            case R.id.navi_menu_btn2 :
                //menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                context.displayContent(DashboardActivity.DISPLAY_CONTENT_GOGO_SNOWWORLD, "", "전투력측정기");
                break;

            //기기설정
            case R.id.navi_menu_btn9 :
                //menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                context.displayContent(DashboardActivity.DISPLAY_CONTENT_APP_SETTING, "", "설정");
                break;

            //로그아웃
            case R.id.navi_menu_logout :
                //menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                context.displayContent(DashboardActivity.DISPLAY_CONTENT_APP_LOGOUT, "", "로그아웃");
                break;


        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
