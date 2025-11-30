package com.example.myapplication;

import android.os.Handler;
import android.os.Looper;
import java.util.List;

public class PostRepository {

    private DatabaseHelper dbHelper;
    private Handler handler;

    public interface Callback<T> {
        void onSuccess(T data);
        void onError(String msg);
    }

    public PostRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.handler = new Handler(Looper.getMainLooper());
    }


    public void getHomePosts(String user, int page, int size, Callback<List<PostItem>> callback) {// 获取首页数据
        new Thread(() -> {
            try {
                Thread.sleep(100); // 模拟延迟
                List<PostItem> data = dbHelper.getPostsByPage(user, page, size);
                if (data.isEmpty()) {  // 模拟无限滚动逻辑
                    dbHelper.generateMoreRandomData(10);
                    data = dbHelper.getPostsByPage(user, page, size);
                }

                List<PostItem> finalData = data;
                handler.post(() -> callback.onSuccess(finalData));

            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> callback.onError(e.getMessage()));// 发生错误时，必须通知主线程！
            }
        }).start();
    }


    public void getRandomPosts(String user, int limit, Callback<List<PostItem>> callback) {//随机推荐
        new Thread(() -> {
            try {
                Thread.sleep(100);
                List<PostItem> data = dbHelper.getRandomPosts(user, limit);
                handler.post(() -> callback.onSuccess(data));
            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }


    public void getUserPosts(String user, Callback<List<PostItem>> callback) {// 获取个人主页数据
        new Thread(() -> {
            try {
                Thread.sleep(100);
                List<PostItem> data = dbHelper.getUserPosts(user);
                handler.post(() -> callback.onSuccess(data));
            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }


    public void toggleLike(int postId, String user, Callback<Boolean> callback) {//点赞
        new Thread(() -> {
            try {
                boolean isLiked = dbHelper.toggleLike(postId, user);
                handler.post(() -> callback.onSuccess(isLiked));
            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }


    public void addPost(String title, String user, Callback<Void> callback) {//发帖
        new Thread(() -> {
            try {
                dbHelper.addPost(title, user);
                Thread.sleep(200);
                handler.post(() -> callback.onSuccess(null));
            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }
}