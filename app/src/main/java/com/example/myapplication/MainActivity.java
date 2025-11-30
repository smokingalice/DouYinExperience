package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PostRepository repository;
    private String currentUser;

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNav;

    private SwipeRefreshLayout swipeRefreshHome;
    private RecyclerView rvHome;
    private WaterfallAdapter adapterHome;
    private StaggeredGridLayoutManager homeLayoutManager;
    private boolean isHomeLoading = false;
    private boolean isSingleColumn = false;
    private int homePage = 0;
    private final int PAGE_SIZE = 8;

    private RecyclerView rvProfile;
    private ImageView ivProfileAvatar;
    private TextView tvProfileName;
    private WaterfallAdapter adapterProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demoactivate);

        currentUser = getIntent().getStringExtra("USER");
        if (currentUser == null) currentUser = "LocalUser";

        DatabaseHelper db = new DatabaseHelper(this);
        repository = new PostRepository(db);

        ImageView btnSwitchLayout = findViewById(R.id.btn_switch_layout);
        bottomNav = findViewById(R.id.bottom_navigation);
        viewPager = findViewById(R.id.view_pager);

        viewPager.setAdapter(new MainPagerAdapter());
        viewPager.setOffscreenPageLimit(1);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    bottomNav.getMenu().findItem(R.id.nav_home).setChecked(true);
                } else {
                    bottomNav.getMenu().findItem(R.id.nav_profile).setChecked(true);
                    loadProfileData();
                }
            }
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                viewPager.setCurrentItem(0, true);
                return true;
            } else if (itemId == R.id.nav_profile) {
                viewPager.setCurrentItem(1, true);
                return true;
            }
            return false;
        });

        btnSwitchLayout.setOnClickListener(v -> switchLayoutMode());
    }

    class MainPagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == 0) {//如果是第 0 页，加载 page_home.xml经验页布局
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_home, parent, false);
                return new HomeViewHolder(view);
            } else {//如果是第 1 页，加载 page_profile.xml个人页布局
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_profile, parent, false);
                return new ProfileViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {// 启动激活绑定数据
            if (position == 0) {
                initHomeLogic(holder.itemView);
            } else {
                initProfileLogic(holder.itemView);
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }//总页数

        @Override
        public int getItemViewType(int position) {
            return position;
        }//当前是哪一页

        class HomeViewHolder extends RecyclerView.ViewHolder {
            public HomeViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }//加载经验页

        class ProfileViewHolder extends RecyclerView.ViewHolder {
            public ProfileViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }//加载个人主页
    }

    private void initHomeLogic(View rootView) {
        swipeRefreshHome = rootView.findViewById(R.id.swipe_refresh);
        rvHome = rootView.findViewById(R.id.recycler_view_home);
        FloatingActionButton fab = rootView.findViewById(R.id.fab_add);

        fab.setOnClickListener(v -> showAddPostDialog());

        homeLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);//创建瀑布流管理
        homeLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);//防止布局乱跳
        rvHome.setLayoutManager(homeLayoutManager);
        rvHome.setItemAnimator(null);

        firstLoadHome();

        swipeRefreshHome.setOnRefreshListener(() ->
                new Handler().postDelayed(this::refreshHomeRandomly, 500)
        );

        rvHome.addOnScrollListener(new PreloadScrollListener());
    }

    private class PreloadScrollListener extends RecyclerView.OnScrollListener {//预加载和永远花不到底的优化
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            homeLayoutManager.invalidateSpanAssignments();//重新整理
            if (newState == RecyclerView.SCROLL_STATE_IDLE) preloadImages();//滚动静止时预加载
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            int[] lastPositions = new int[homeLayoutManager.getSpanCount()];
            homeLayoutManager.findLastVisibleItemPositions(lastPositions);
            int lastVisibleItemPos = findMax(lastPositions);
            int total = homeLayoutManager.getItemCount();
            if ((total - lastVisibleItemPos) <= 4 && !isHomeLoading && total > 0) {//剩余未读数量
                loadMoreHomeData();//加载新数据
            }
        }
    }

    private void initProfileLogic(View rootView) {//个人主页初始化
        ivProfileAvatar = rootView.findViewById(R.id.iv_profile_avatar);
        tvProfileName = rootView.findViewById(R.id.tv_profile_name);
        rvProfile = rootView.findViewById(R.id.recycler_view_profile);
        android.widget.Button btnLogout = rootView.findViewById(R.id.btn_logout);

        tvProfileName.setText(currentUser);
        String avatarUrl = "https://api.dicebear.com/7.x/avataaars/png?seed=" + currentUser;
        Glide.with(this).load(avatarUrl).apply(RequestOptions.circleCropTransform()).into(ivProfileAvatar);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        rvProfile.setLayoutManager(layoutManager);
        rvProfile.setItemAnimator(null);

        btnLogout.setOnClickListener(v -> {//设置登出功能
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        loadProfileData();//加载个人页方法
    }

    private void firstLoadHome() {//首次加载
        if (swipeRefreshHome != null) swipeRefreshHome.setRefreshing(true);
        repository.getHomePosts(currentUser, 0, PAGE_SIZE, new PostRepository.Callback<List<PostItem>>() {
            @Override
            public void onSuccess(List<PostItem> data) {
                adapterHome = new WaterfallAdapter(MainActivity.this, data, new WaterfallAdapter.OnItemClickListener() {
                    @Override
                    public void onLikeClick(int position, PostItem item) {
                        handleLike(item, position, adapterHome);
                    }
                    @Override
                    public void onPostClick(PostItem item) {
                        navigateToDetail(item);
                    }
                });
                rvHome.setAdapter(adapterHome);
                if (swipeRefreshHome != null) swipeRefreshHome.setRefreshing(false);
            }
            @Override
            public void onError(String msg) {
                if (swipeRefreshHome != null) swipeRefreshHome.setRefreshing(false);
            }
        });
    }

    private void refreshHomeRandomly() {//下拉刷新
        repository.getRandomPosts(currentUser, PAGE_SIZE, new PostRepository.Callback<List<PostItem>>() {
            @Override
            public void onSuccess(List<PostItem> data) {
                if (adapterHome != null) {
                    adapterHome.resetData(data);
                } else {
                    firstLoadHome();
                    return;
                }
                homePage = 0;
                if (swipeRefreshHome != null) swipeRefreshHome.setRefreshing(false);
                Toast.makeText(MainActivity.this, "已推荐最新内容", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String msg) {
                if (swipeRefreshHome != null) swipeRefreshHome.setRefreshing(false);
                Toast.makeText(MainActivity.this, "刷新失败: " + msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMoreHomeData() {//无限滚动时生成更多
        isHomeLoading = true;
        repository.getHomePosts(currentUser, homePage + 1, PAGE_SIZE, new PostRepository.Callback<List<PostItem>>() {
            @Override
            public void onSuccess(List<PostItem> data) {
                if (data.size() > 0) {
                    adapterHome.appendData(data);
                    homePage++;
                }
                isHomeLoading = false;
            }
            @Override
            public void onError(String msg) {
                isHomeLoading = false;
            }
        });
    }

    private void loadProfileData() {//加载个人主页
        repository.getUserPosts(currentUser, new PostRepository.Callback<List<PostItem>>() {
            @Override
            public void onSuccess(List<PostItem> data) {
                if (adapterProfile == null) {
                    adapterProfile = new WaterfallAdapter(MainActivity.this, data, new WaterfallAdapter.OnItemClickListener() {
                        @Override
                        public void onLikeClick(int position, PostItem item) {
                            handleLike(item, position, null);
                            loadProfileData();
                        }

                        @Override
                        public void onPostClick(PostItem item) {
                            navigateToDetail(item);
                        }
                    });
                    rvProfile.setAdapter(adapterProfile);
                } else {
                    adapterProfile.resetData(data);
                }
            }

            @Override
            public void onError(String msg) {
            }
        });
    }

    private void handleLike(PostItem item, int pos, WaterfallAdapter adapter) {//更新点赞数据
        boolean newStatus = !item.isLiked;
        item.isLiked = newStatus;
        item.likeCount = newStatus ? item.likeCount + 1 : item.likeCount - 1;
        if (adapter != null) adapter.notifyItemChanged(pos);
        repository.toggleLike(item.id, currentUser, new PostRepository.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
            }

            @Override
            public void onError(String msg) {
            }
        });
    }

    private void switchLayoutMode() {
        isSingleColumn = !isSingleColumn;//按键按下后自动切换模式
        homeLayoutManager.setSpanCount(isSingleColumn ? 1 : 2);//模式判断
        if (adapterHome != null) adapterHome.notifyItemRangeChanged(0, adapterHome.getItemCount());//强制重绘
        Toast.makeText(this, isSingleColumn ? "单列模式" : "双列模式", Toast.LENGTH_SHORT).show();
    }

    private void preloadImages() {//预加载的方法
        if (adapterHome == null) return;
        List<PostItem> dataList = adapterHome.getDataList();
        if (dataList == null || dataList.isEmpty()) return;
        int[] lastPositions = new int[homeLayoutManager.getSpanCount()];
        homeLayoutManager.findLastVisibleItemPositions(lastPositions);//定位最下方的在哪个位置
        int lastVisiblePos = findMax(lastPositions);
        for (int i = 1; i <= 6; i++) {
            int targetPos = lastVisiblePos + i;// 算出目标位置，比如当前看到第 11 个，那我要加载第 12,13,...个
            if (targetPos < dataList.size()) {// 不能超出总数
                Glide.with(MainActivity.this)
                        .load(dataList.get(targetPos).imageUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .preload();//只加载不显示，存到缓存里即可
            }
        }
    }

    private void showAddPostDialog() {
        final EditText input = new EditText(this);
        input.setHint("分享你的经验...");
        input.setPadding(60, 40, 60, 40);
        input.setBackground(null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("发布新笔记")
                .setView(input)
                .setPositiveButton("发布", (dialog, which) -> {
                    String content = input.getText().toString();
                    if (!content.isEmpty()) {
                        repository.addPost(content, currentUser, new PostRepository.Callback<Void>() {
                            @Override
                            public void onSuccess(Void data) {
                                Toast.makeText(MainActivity.this, "发布成功", Toast.LENGTH_SHORT).show();
                                if (swipeRefreshHome != null) {
                                    swipeRefreshHome.setRefreshing(true);
                                }
                                refreshHomeRandomly();//发完贴后重新拉取数据
                            }
                            @Override
                            public void onError(String msg) {
                                Toast.makeText(MainActivity.this, "发布失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("取消", null);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog_rounded);
        }
        dialog.show();
    }

    private void navigateToDetail(PostItem item) {//详情页跳转
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra("POST_DATA", item);
        startActivity(intent);
    }

    private int findMax(int[] arr) {
        int max = arr[0];
        for (int v : arr) if (v > max) max = v;
        return max;
    }
}