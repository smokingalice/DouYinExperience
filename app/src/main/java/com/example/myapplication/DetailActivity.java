package com.example.myapplication;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        PostItem item = (PostItem) getIntent().getSerializableExtra("POST_DATA");

        if (item == null) return;

        ImageView ivImage = findViewById(R.id.iv_detail_image);
        ImageView ivAvatar = findViewById(R.id.iv_detail_avatar);
        TextView tvTitle = findViewById(R.id.tv_detail_title);
        TextView tvUser = findViewById(R.id.tv_detail_user);
        TextView tvDesc = findViewById(R.id.tv_detail_desc);

        tvTitle.setText(item.title);
        tvUser.setText(item.userName);
        tvDesc.setText(item.description);

        Glide.with(this)
                .load(item.imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivImage);

        Glide.with(this)
                .load(item.avatarUrl)
                .apply(RequestOptions.circleCropTransform())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivAvatar);
    }
}