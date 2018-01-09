package com.summertaker.blog46study;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.summertaker.blog46study.common.BaseActivity;
import com.summertaker.blog46study.common.BaseApplication;
import com.summertaker.blog46study.common.Config;
import com.summertaker.blog46study.data.Member;
import com.summertaker.blog46study.data.Team;
import com.summertaker.blog46study.util.SlidingTabLayout;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, MainFragment.Callback {

    private static final int REQUEST_PERMISSION_CODE = 100;

    private Toolbar mToolbar;

    private SlidingTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mPagerAdapter;
    private ArrayList<Team> mTeams = new ArrayList<>();
    private ArrayList<Member> mOshiMembers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mContext = getApplicationContext(); //MainActivity.this;

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runFragment("goTop");
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //goActivity();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //mGroupName = "";
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mSlidingTabLayout = findViewById(R.id.slidingTabs);

        mViewPager = findViewById(R.id.viewPager);

        //----------------------------------------------------------------------------
        // 런타임에 권한 요청
        // https://developer.android.com/training/permissions/requesting.html?hl=ko
        //----------------------------------------------------------------------------
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        }, REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    start();
                } else {
                    // permission denied
                    onPermissionDenied();
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    void onPermissionDenied() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage(R.string.access_denied);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }

    void start() {
        //mOshiMembers = BaseApplication.getInstance().loadMember(Config.PREFERENCE_KEY_OSHIMEMBERS);
        //BaseApplication.getInstance().setmOshimembers(mOshiMembers);

        if (mPagerAdapter == null) {
            init();
        }
    }

    @Override
    public void onResume() {
        //Log.e(mTag, ">>>>> onResume()...");
        super.onResume();

        if (mPagerAdapter == null) {
            init();
        } else {
            Collections.shuffle(mOshiMembers);
            mPagerAdapter.notifyDataSetChanged();
        }
    }

    private void init() {
        //mOshiMembers = BaseApplication.getInstance().getOshimembers();

        mOshiMembers = BaseApplication.getInstance().loadMember(Config.PREFERENCE_KEY_OSHIMEMBERS);
        Collections.shuffle(mOshiMembers);
        BaseApplication.getInstance().setmOshimembers(mOshiMembers);

        mPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                //mBaseToolbar.setTitle(mTitle + " (" + (position + 1) + "/" + mMemberList.size() + ")");
                //setAnswer(position);

                /*
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + mViewPager.getCurrentItem());
                if (fragment != null) {
                    MainFragment f = (MainFragment) fragment;
                    f.update();
                }
                */
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        //-------------------------------------------------------------------------------------------------------
        // 뷰페이저 간 이동 시 프레그먼트 자동으로 새로고침 방지
        // https://stackoverflow.com/questions/28494637/android-how-to-stop-refreshing-fragments-on-tab-change
        //-------------------------------------------------------------------------------------------------------
        mViewPager.setOffscreenPageLimit(mOshiMembers.size());

        mSlidingTabLayout.setViewPager(mViewPager);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(mContext, GroupActivity.class);
            startActivityForResult(intent, Config.REQUEST_CODE);
            return true;
        } else if (id == R.id.action_refresh) {
            runFragment("refresh");
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return MainFragment.newInstance(position); //, mOshiMembers.get(position).getBlogUrl());
        }

        @Override
        public int getCount() {
            return mOshiMembers.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mOshiMembers.get(position).getName(); // "Tab " + position;
        }
    }

    public void runFragment(String command) {
        //--------------------------------------------------------------------------------------------
        // 프레그먼트에 이벤트 전달하기
        // https://stackoverflow.com/questions/34861257/how-can-i-set-a-tag-for-viewpager-fragments
        //--------------------------------------------------------------------------------------------
        Fragment f = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + mViewPager.getCurrentItem());

        // based on the current position you can then cast the page to the correct Fragment class
        // and call some method inside that fragment to reload the data:
        //if (0 == mViewPager.getCurrentItem() && null != f) {
        if (f == null) {
            if ("goBack".equals(command)) {
                super.onBackPressed();
            }
        } else {
            MainFragment mf = (MainFragment) f;

            switch (command) {
                case "goTop":
                    mf.goTop();
                    break;
                case "refresh":
                    mf.refresh();
                    break;
            }
        }
    }

    @Override
    public void onError(String message) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.e(mTag, ">>>>> onActivityResult()...");

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Config.REQUEST_CODE && data != null) { // && resultCode == Activity.RESULT_OK) {
            boolean isDataChanged = data.getBooleanExtra("isDataChanged", false);
            if (isDataChanged) {
                //--------------------------------------------------------------------------------------
                // 이후에 onResume()이 실행되므로 mPageAdapter 값을 초기화
                // onResume() 에서 Tab 과 ViewPager 를 새로 고침 처리한다.
                //--------------------------------------------------------------------------------------
                mPagerAdapter = null;
            }
        }
    }
}
