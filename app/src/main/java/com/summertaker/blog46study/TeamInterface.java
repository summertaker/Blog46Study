package com.summertaker.blog46study;

import android.widget.CheckBox;

import com.summertaker.blog46study.data.Member;

public interface TeamInterface {

    void onPicutreClick(Member member);

    void onLikeClick(CheckBox checkBox, Member member);

    void onNameClick(Member member);
}
