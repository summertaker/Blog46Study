package com.summertaker.blog46study;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.summertaker.blog46study.common.BaseApplication;
import com.summertaker.blog46study.common.BaseFragment;
import com.summertaker.blog46study.common.Config;
import com.summertaker.blog46study.data.Article;
import com.summertaker.blog46study.data.Member;
import com.summertaker.blog46study.parser.Keyakizaka46Parser;
import com.summertaker.blog46study.parser.Nogizaka46Parser;
import com.summertaker.blog46study.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainFragment extends BaseFragment {

    private Callback mCallback;

    private int mPosition;
    private String mBlogUrl;

    private ArrayList<Article> mArticles = new ArrayList<>();
    private MainAdapter mAdapter;
    private ListView mListView;

    private boolean mIsRefreshMode = false;

    public interface Callback {
        void onError(String message);
    }

    public MainFragment() {
    }

    public static MainFragment newInstance(int position) {
        MainFragment fragment = new MainFragment();

        Bundle args = new Bundle();
        args.putInt("position", position);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_fragment, container, false);

        mContext = inflater.getContext();

        Bundle bundle = getArguments();
        mPosition = bundle.getInt("position", 0);

        //----------------------------------------------------------
        // mBlogUrl 을 Activity 에서 받으면 계속 같은 값이 들어온다.
        // 새로 고침을 위해 이 곳에서 직접 값을 설정한다.
        //----------------------------------------------------------
        ArrayList<Member> members = BaseApplication.getInstance().getOshimembers();
        mBlogUrl = members.get(mPosition).getBlogUrl();
        //Log.e(mTag, "mBlogUrl: " + mBlogUrl);

        //----------------------------------------------------------
        // Activity 에서의 새로 고침 적용을 위해 초기화
        //----------------------------------------------------------
        //mArticles.clear();

        mAdapter = new MainAdapter(mContext, mArticles);

        mListView = rootView.findViewById(R.id.listView);
        mListView.setAdapter(mAdapter);

        loadData(mBlogUrl);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity;
        if (context instanceof Activity) {
            activity = (Activity) context;

            try {
                mCallback = (Callback) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement Listener for Fragment.");
            }
        }
    }

    private void loadData(String url) {
        String fileName = Util.getUrlToFileName(url) + ".html";
        //Log.e(mTag, "fileName: " + fileName);

        File file = new File(Config.DATA_PATH, fileName);
        if (file.exists() && !mIsRefreshMode) {
            Date lastModDate = new Date(file.lastModified());
            //Log.e(mTag, "File last modified: " + lastModDate.toString());

            boolean isSameDate = Util.isSameDate(lastModDate, Calendar.getInstance().getTime());
            if (isSameDate) {
                //Log.e(mTag, ">>>>> parseData()...");
                parseData(Util.readFile(fileName));
            } else {
                //Log.e(mTag, ">>>>> requestData()...");
                requestData(url);
            }
        } else {
            //Log.e(mTag, ">>>>> requestData()...");
            requestData(url);
        }
    }

    private void requestData(final String url) {
        //Log.e(mTag, "url: " + url);

        StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.d(mTag, response.toString());
                writeData(url, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Util.alert(mContext, getString(R.string.error), error.getMessage(), null);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                //headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("User-agent", Config.USER_AGENT_DESKTOP);
                return headers;
            }
        };

        BaseApplication.getInstance().addToRequestQueue(strReq, mVolleyTag);
    }

    private void writeData(String url, String response) {
        Util.writeToFile(Util.getUrlToFileName(url) + ".html", response);
        parseData(response);
    }

    private void parseData(String response) {
        if (response.isEmpty()) {
            Util.alert(mContext, getString(R.string.error), "response is empty.", null);
        } else {
            mArticles.clear();

            if (mBlogUrl.contains("nogizaka46")) {
                Nogizaka46Parser nogizaka46Parser = new Nogizaka46Parser();
                nogizaka46Parser.parseBlogDetail(response, mArticles);
            } else if (mBlogUrl.contains("keyakizaka46")) {
                Keyakizaka46Parser keyakizaka46Parser = new Keyakizaka46Parser();
                keyakizaka46Parser.parseBlogDetail(response, mArticles);
            }

            renderData();
        }
    }

    public void renderData() {
        mAdapter.notifyDataSetChanged();
    }

    public void goTop() {
        //Log.e(mTag, "goTop()..." + mPosition);

        mListView.setSelection(0);
        //mListView.smoothScrollToPosition(0);
        //mListView.setSelectionAfterHeaderView();
    }

    public void refresh() {
        //Log.e(mTag, "refresh()..." + mPosition);

        //mAdapter.notifyDataSetChanged();

        mIsRefreshMode = true;

        loadData(mBlogUrl);
    }
}

