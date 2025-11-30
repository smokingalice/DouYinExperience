package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import java.util.List;

public class WaterfallAdapter extends RecyclerView.Adapter<WaterfallAdapter.ViewHolder> {

    private Context context;
    private List<PostItem> dataList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onLikeClick(int position, PostItem item);
        void onPostClick(PostItem item);
    }

    public WaterfallAdapter(Context context, List<PostItem> dataList, OnItemClickListener listener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;
    }

    public List<PostItem> getDataList() {
        return dataList;
    }

    public void resetData(List<PostItem> newData) {
        this.dataList.clear();
        this.dataList.addAll(newData);
        notifyDataSetChanged();
    }//下拉刷新

    public void appendData(List<PostItem> newData) {
        int startPos = this.dataList.size();
        this.dataList.addAll(newData);
        notifyItemRangeInserted(startPos, newData.size());
    }//上拉加载

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.waterfall_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PostItem item = dataList.get(position);// 1. 根据位置拿出要显示的那条数据
        holder.tvTitle.setText(item.title);
        holder.tvUser.setText(item.userName);
        holder.tvLikeCount.setText(String.valueOf(item.likeCount));
        if (item.isLiked) {
            holder.ivLikeIcon.setImageResource(R.drawable.ic_heart_filled);
        } else {
            holder.ivLikeIcon.setImageResource(R.drawable.ic_heart_outline);
        }
        holder.layoutLike.setOnClickListener(v -> {
            listener.onLikeClick(position, item);
            boolean isNowLiked = item.isLiked;
            if (isNowLiked) {
                holder.ivLikeIcon.setImageResource(R.drawable.ic_heart_filled);
                holder.ivLikeIcon.setScaleX(0.8f);
                holder.ivLikeIcon.setScaleY(0.8f);// 起始状态略微缩小
                holder.ivLikeIcon.animate()
                        .scaleX(1.2f) // 先重置大小 -> 快速放大到 1.2倍 -> 回弹到 1.0倍
                        .scaleY(1.2f)
                        .setDuration(300)
                        .setInterpolator(new android.view.animation.OvershootInterpolator(4f))
                        .withEndAction(() -> {
                            holder.ivLikeIcon.animate()
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .setDuration(100)
                                    .start();
                        })
                        .start();
            } else {
                holder.ivLikeIcon.setImageResource(R.drawable.ic_heart_outline);
                holder.ivLikeIcon.setScaleX(1.0f);
                holder.ivLikeIcon.setScaleY(1.0f);
                holder.ivLikeIcon.animate().cancel();
            }
        });
        Glide.with(context)
                .load(item.imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存图片，切到详情页加载更快
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.ivCover);
        Glide.with(context)
                .load(item.avatarUrl)
                .apply(RequestOptions.circleCropTransform())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.ivAvatar);
        holder.itemView.setOnClickListener(v -> listener.onPostClick(item));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover, ivAvatar, ivLikeIcon;
        TextView tvTitle, tvUser, tvLikeCount;
        View layoutLike;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvTitle = itemView.findViewById(R.id.tv_title);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvUser = itemView.findViewById(R.id.tv_username);
            ivLikeIcon = itemView.findViewById(R.id.iv_like_icon);
            tvLikeCount = itemView.findViewById(R.id.tv_like_count);
            layoutLike = itemView.findViewById(R.id.layout_like);
        }
    }
}