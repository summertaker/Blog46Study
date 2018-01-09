package com.summertaker.blog46study;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.summertaker.blog46study.common.BaseDataAdapter;
import com.summertaker.blog46study.common.Config;
import com.summertaker.blog46study.data.Article;
import com.summertaker.blog46study.util.ImageUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainAdapter extends BaseDataAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<Article> mArticles = new ArrayList<>();

    public MainAdapter(Context context, ArrayList<Article> articles) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mArticles = articles;
    }

    @Override
    public int getCount() {
        return mArticles.size();
    }

    @Override
    public Object getItem(int position) {
        return mArticles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder = null;

        final Article article = mArticles.get(position);

        if (view == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            view = mLayoutInflater.inflate(R.layout.main_item, null);

            holder = new ViewHolder();
            holder.tvTitle = view.findViewById(R.id.tvTitle);
            holder.tvDate = view.findViewById(R.id.tvDate);
            holder.tvContent = view.findViewById(R.id.tvContent);
            //mContext.registerForContextMenu(holder.tvContent);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        /*
        String imageUrl = member.getPictureUrl(); // member.getThumbnail();

        if (imageUrl == null || imageUrl.isEmpty()) {
            holder.loLoading.setVisibility(View.GONE);
            holder.ivThumbnail.setImageResource(R.drawable.placeholder);
        } else {
            String fileName = Util.getUrlToFileName(imageUrl);
            File file = new File(Config.DATA_PATH, fileName);

            if (mIsCacheMode && file.exists()) {
                holder.loLoading.setVisibility(View.GONE);
                Picasso.with(mContext).load(file).into(holder.ivThumbnail);
                //Log.d(mTag, fileName + " local loaded.");
            } else {
                final RelativeLayout loLoading = holder.loLoading;

                Picasso.with(mContext).load(imageUrl).into(holder.ivThumbnail, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        loLoading.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        loLoading.setVisibility(View.GONE);
                        Log.e(mTag, "Picasso Image Load Error...");
                    }
                });

                Picasso.with(mContext).load(imageUrl).into(getTarget(fileName));
            }
        }

        holder.ivThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Member m = mMembers.get(position);
                mTeamInterface.onPicutreClick(m);
            }
        });
        */

        //holder.tvTitle.setText(article.getTitle());
        holder.tvTitle.setText(Html.fromHtml(article.getTitle(), Html.FROM_HTML_MODE_COMPACT));

        holder.tvDate.setText(article.getDate());

        //holder.tvContent.setText(article.getContent());
        //holder.tvContent.setText(Html.fromHtml(article.getContent(), Html.FROM_HTML_MODE_COMPACT));

        // https://medium.com/@rajeefmk/android-textview-and-image-loading-from-url-part-1-a7457846abb6
        Spannable html = ImageUtil.getSpannableHtmlWithImageGetter(mContext, holder.tvContent, article.getContent());
        //ImageUtil.setClickListenerOnHtmlImageGetter(html, new ImageUtil.Callback() {
        //    @Override
        //    public void onImageClick(String imageUrl) {
        //        //Log.e(mTag, "imageUrl: " + imageUrl);
        //        //viewImage(imageUrl);
        //    }
        //}, true);
        holder.tvContent.setText(html);
        holder.tvContent.setMovementMethod(LinkMovementMethod.getInstance()); // URL 클릭 시 이동

        return view;
    }

    //target to save
    private Target getTarget(final String fileName) {
        Target target = new Target() {

            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        boolean isSuccess;

                        File file = new File(Config.DATA_PATH, fileName);
                        if (file.exists()) {
                            isSuccess = file.delete();
                            //Log.d("==", fileName + " deleted.");
                        }
                        try {
                            isSuccess = file.createNewFile();
                            if (isSuccess) {
                                FileOutputStream ostream = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                                ostream.flush();
                                ostream.close();
                                //Log.d("==", fileName + " created.");
                            } else {
                                Log.e("==", fileName + " FAILED.");
                            }
                        } catch (IOException e) {
                            Log.e("IOException", e.getLocalizedMessage());
                        }
                    }
                }).start();
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Log.e(mTag, "IMAGE SAVE ERROR!!! onBitmapFailed()");
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        return target;
    }

    static class ViewHolder {
        TextView tvTitle;
        TextView tvDate;
        TextView tvContent;
    }
}

