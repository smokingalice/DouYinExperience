package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 10;//避免一直重装我一致更新版本，更行到了第10版。
    private static final String DB_NAME = "DouyinExperience.db";

    private static final String[] TITLES = {//标题库
            "周末去哪里？这家咖啡馆绝了！",
            "打工人午餐吃什么？这家轻食店闭眼冲！",
            "夏日降温神器？这杯冰饮喝到爽！",
            "租房改造怎么弄？500 元搞定温馨小窝！",
            "通勤穿搭没思路？这 3 套公式万能不出错！",
            "减脂期嘴馋怎么办？这些零食零负担！",
            "闺蜜约会去哪玩？这家甜品店超出片！",
            "新手学化妆难？3 步搞定伪素颜妆！",
            "周末遛娃去哪儿？这个公园好玩不费妈！",
            "熬夜党急救护肤？这瓶精华绝了！",
            "平价好物推荐？10 元内挖到宝！",
            "追剧零食选什么？这几款越吃越上头！",
            "居家清洁懒人法？10 分钟搞定全屋！",
            "学生党早餐吃什么？简单快手又营养！",
            "旅行拍照怎么拍？3 个技巧出大片！",
            "秋冬穿搭显胖？这件大衣显瘦 10 斤！",
            "办公室摸鱼神器？这玩意儿提升幸福感！",
            "脱发星人救星？这款洗发水亲测有效！",
            "家庭聚餐做什么？3 道硬菜零失败！",
            "小个子穿搭困扰？这套搭配显高 5cm！",
            "周末不想宅家？这个小众景点人少景美！"
    };


    private static final String[] IMAGES = {//图片库
            "https://images.pexels.com/photos/45201/kitty-cat-kitten-pet-45201.jpeg?auto=compress&cs=tinysrgb&w=500",
            "https://images.pexels.com/photos/312418/pexels-photo-312418.jpeg?auto=compress&cs=tinysrgb&w=500",
            "https://images.pexels.com/photos/869258/pexels-photo-869258.jpeg?auto=compress&cs=tinysrgb&w=500",
            "https://images.pexels.com/photos/376464/pexels-photo-376464.jpeg?auto=compress&cs=tinysrgb&w=500",
            "https://images.pexels.com/photos/774909/pexels-photo-774909.jpeg?auto=compress&cs=tinysrgb&w=500",
            "https://images.pexels.com/photos/1779487/pexels-photo-1779487.jpeg?auto=compress&cs=tinysrgb&w=500",
            "https://images.pexels.com/photos/169647/pexels-photo-169647.jpeg?auto=compress&cs=tinysrgb&w=500",
            "https://images.pexels.com/photos/15286/pexels-photo.jpg?auto=compress&cs=tinysrgb&w=500",
            "https://images.pexels.com/photos/607812/pexels-photo-607812.jpeg?auto=compress&cs=tinysrgb&w=500",
            "https://images.pexels.com/photos/2235130/pexels-photo-2235130.jpeg?auto=compress&cs=tinysrgb&w=500"
    };

    private static final String[] DETAILS = {//内容库
            "下班路上偶然拐进一条小巷，居然藏着这么治愈的小店！环境超舒服，已经列入常去清单～# 探店 #生活碎片",
            "周末不想宅家瞎晃，误打误撞找到这家宝藏店铺！东西好吃又平价，分享给喜欢发掘新地儿的朋友～# 周末日常 #美食分享",
            "今天阳光正好，约着朋友出门散步，偶遇一家颜值超高的甜品店！蛋糕颜值口感双在线，太惊喜啦～# 甜品 #日常打卡",
            "逛超市的时候发现楼下新开了家饮品店，点了杯招牌居然没踩雷！清爽不腻，推荐给附近的小伙伴～# 饮品推荐 #生活",
            "放假在家待不住，随便坐上一趟公交瞎逛，没想到在终点站附近发现这家神仙小店！氛围绝了，太适合放松～# 假期日常 #探店"
    };

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (username TEXT PRIMARY KEY, password TEXT)");
        db.execSQL("CREATE TABLE posts (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, image_url TEXT, author TEXT, description TEXT)");
        db.execSQL("CREATE TABLE likes (post_id INTEGER, username TEXT, PRIMARY KEY(post_id, username))");//初始化建表

        // 首次初始化 20 条
        generateBatchData(db, 20);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS posts");
        db.execSQL("DROP TABLE IF EXISTS likes");
        onCreate(db);
    }


    private void generateBatchData(SQLiteDatabase db, int count) {//随机生成帖子逻辑
        Random random = new Random();
        db.beginTransaction();
        try {
            for (int i = 0; i < count; i++) {
                ContentValues values = new ContentValues();
                values.put("title", TITLES[random.nextInt(TITLES.length)]);
                values.put("image_url", IMAGES[random.nextInt(IMAGES.length)]);
                values.put("author", "User" + random.nextInt(50));
                values.put("description", DETAILS[random.nextInt(DETAILS.length)]);
                long postId = db.insert("posts", null, values);
                // 随机生成点赞
                if (random.nextInt(10) < 4) {
                    int likeCount = random.nextInt(5);
                    for (int k = 0; k < likeCount; k++) {
                        ContentValues likeValues = new ContentValues();
                        likeValues.put("post_id", postId);
                        likeValues.put("username", "robot_" + random.nextInt(1000));
                        db.insert("likes", null, likeValues);
                    }
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }


    public void generateMoreRandomData(int count) {//随机生成更多帖子逻辑
        SQLiteDatabase db = this.getWritableDatabase();
        generateBatchData(db, count);
    }


    private List<PostItem> queryPosts(String sql, String[] args, String currentUser) {//转换
        List<PostItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, args);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String title = cursor.getString(1);
            String url = cursor.getString(2);
            String author = cursor.getString(3);
            String desc = cursor.getString(4);
            Cursor likeCursor = db.rawQuery("SELECT COUNT(*) FROM likes WHERE post_id=?", new String[]{String.valueOf(id)});
            likeCursor.moveToFirst();
            int count = likeCursor.getInt(0);
            likeCursor.close();
            Cursor myLikeCursor = db.rawQuery("SELECT COUNT(*) FROM likes WHERE post_id=? AND username=?", new String[]{String.valueOf(id), currentUser});
            myLikeCursor.moveToFirst();
            boolean isLiked = myLikeCursor.getInt(0) > 0;
            myLikeCursor.close();
            list.add(new PostItem(id, url, title, author, count, isLiked, desc));
        }
        cursor.close();
        return list;
    }

    public List<PostItem> getPostsByPage(String currentUser, int page, int pageSize) {//上拉加载
        int offset = page * pageSize;
        String sql = "SELECT * FROM posts ORDER BY id DESC LIMIT ? OFFSET ?";
        return queryPosts(sql, new String[]{String.valueOf(pageSize), String.valueOf(offset)}, currentUser);
    }

    public List<PostItem> getRandomPosts(String currentUser, int limit) {//下拉刷新
        String sql = "SELECT * FROM posts ORDER BY RANDOM() LIMIT ?";//随机获取模拟下拉刷新
        return queryPosts(sql, new String[]{String.valueOf(limit)}, currentUser);
    }

    public List<PostItem> getUserPosts(String username) {//获取个人主页数据
        String sql = "SELECT * FROM posts WHERE author = ? ORDER BY id DESC";
        return queryPosts(sql, new String[]{username}, username);
    }

    public boolean toggleLike(int postId, String username) {//点赞
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM likes WHERE post_id=? AND username=?", new String[]{String.valueOf(postId), username});
        boolean result;
        if (cursor.getCount() > 0) {
            db.delete("likes", "post_id=? AND username=?", new String[]{String.valueOf(postId), username});
            result = false;
        } else {
            ContentValues values = new ContentValues();
            values.put("post_id", postId);
            values.put("username", username);
            db.insert("likes", null, values);
            result = true;
        }
        cursor.close();
        return result;
    }

    public void addPost(String title, String author) {//发帖
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("author", author);
        values.put("image_url", "https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=500");
        values.put("description", "这是我新发布的一条动态。");
        db.insert("posts", null, values);
    }

    public boolean loginOrRegister(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username = ?", new String[]{username});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            String dbPass = cursor.getString(1);
            cursor.close();
            return password.equals(dbPass);
        } else {
            ContentValues values = new ContentValues();
            values.put("username", username);
            values.put("password", password);
            db.insert("users", null, values);
            cursor.close();
            return true;
        }
    }
}