# 仿抖音经验app

## 项目成品展示
###  ⚡已实现项目功能
1、**要求必备功能**
- **瀑布流**: 采用双列瀑布流展示内容，图片根据原始比例自适应高度。
- **完整的经验卡片**: 图片、标题、用户头像、用户名、点赞数等核心元素全部具备
- **点赞交互功能**: 支持点击点赞图标切换点赞状态，并更新点赞计数。
- **多种数据更新方式**: 实现上拉加载更多数据和下拉刷新最新内容的功能
- **数据配置管理**: 支持 Mock 数据模拟，并且配置了网络图片作为模拟，数据也可以动态更新。

2、**其他功能与加分项**
- **身份认证:**: 简化登录流程，系统自动识别账号状态，实现自动注册、快速登录，退出登录等。
- **内容详情浏览**:支持点击列表卡片进入详情页，无缝展示高清大图、作者头像以及完整的文字描述信息。
- **个人中心管理**: 拥有独立的个人主页，自动聚合展示当前用户发布过的所有历史动态，并提供安全退出登录选项。。
- **内容发布机制**: 提供便捷的发帖入口，支持用户输入文本并模拟发布流程，发布后会展现最新内容。
- **图片预加载**: 系统会预判用户的浏览进度，在滑动间隙提前加载屏幕下方即将出现的图片，消除滑动时的白色占位图。
- **布局即时切换**: 顶部提供快捷切换按钮，支持用户在“双列瀑布流”与“单列大图流”两种浏览模式间 0 秒无缝切换。
- **零延迟点赞交互**: 采用“视觉先行”的交互策略，点击爱心瞬间即亮起并播放回弹动画，无需等待网络响应并实现了点赞动画效果。
- **图片缓存**: 读取图片时读入缓存读入手机硬盘并建立经验页与详情页的资源共享通道，无论是主页瀑布流还是进入详情页都不需要重新加载图片实现详情页图片的瞬间加载
- **防抖稳定**: 优化了刷新和回滚时的排版逻辑，确保在加载新旧数据时画面稳定，不会出现卡片左右乱跳或错位现象。
- **丝滑页面切换**：基于 ViewPager2 + BottomNavigationView 实现主页与个人中心的双向联动，支持手势左右滑动切换与点击跳转。

3、可扩展功能（时间关系因为有bug并未实装）
- 搜索功能:通过keyword搜索帖子。（参考文档最下方可拓展内容）

### 项目apk
https://github.com/smokingalice/DouYinExperience/releases/download/v1.0.0/DouYinExperience.apk
### 功能展示视频
https://www.bilibili.com/video/BV1DhSPBHEfU/?spm_id_from=333.1387.upload.video_card.click&vd_source=32a1e95314126e39c5a2d66d16deb409
## 项目代码解析

这部分将详细介绍整个代码的结构和设计思路，以及在写代码的过程中进行的修改部分。
### 📂 项目代码结构
针对整个项目结构我最初进行了分包设计，即分成ui.activity、adapters、data等等。但是由于项目结构单一且涉及的页面相对不多，同时在处理前后端数据交互时采用了模拟后端以及接口的形式并没有真正访问云服务器所以最终选择了都放置在同一个目录下方便修改。
```java
app/src/main
├── java/com/example/myapplication
│   ├── MainActivity.java        // 主Activity,管理底部控建、经验首页与个人页
│   ├── SplashActivity.java      // 模仿抖音启动页面
│   ├── LoginActivity.java       // 实现登录注册功能,SQLite 完成校验,为了简化项目可以自动注册
│   ├── DetailActivity.java      // 每个帖子详情页,展示内容随机生成
│   ├── WaterfallAdapter.java    // 瀑布流列表适配器,实现瀑布流图片加载与点击事件
│   ├── PostRepository.java      // 后端模拟仓库,模拟请求延迟,回调接口以及异步,根据群里提问所设计
│   ├── DatabaseHelper.java      // 数据库helper,实现SQLite 建表、Mock 数据生成、随机查询
│   └── PostItem.java            // 帖子实体对象
│
└── res
    ├── layout                   // 布局文件
    │   ├── activity_demoactivate.xml    // 主框架TopBar + ViewPager2 +BottomNavigationView
    │   ├── page_home.xml        // 首页页面
    │   ├── page_profile.xml     // 个人页页面
    │   ├── waterfall_card.xml // 瀑布流单个帖子布局 CardView + ImageView
    │   ├── activity_detail.xml  // 详情页
    │   ├── activity_login.xml   // 登录页
    │   └── activity_splash.xml  // 启动页
    ├── menu
    │   └── bottom_nav_menu.xml  // 底部导航栏菜单
    ├── values
    │   ├── themes.xml           // 主题配置使用 NoActionBar 去除默认标题栏
    │   ├── strings.xml
    │   └── colors.xml
    ├── mipmap
	└── drawable
	...
```
### ✨ 项目设计（重点创新我使用了红色标记）
#### 1、配置文件 AndroidManifest.xml与SplashActivity
由于我设置了仿造抖音的启动页面，所以SplashActivity是整个项目的launcher启动界面，所以配置文件做出申请。
```java
 <activity
     android:name=".SplashActivity"
     ....
     <intent-filter>
         <action android:name="android.intent.action.MAIN" />
         <category android:name="android.intent.category.LAUNCHER" />
     </intent-filter>
 </activity>
```
SplashActivity的页面文件activity_splash.xml用RelativeLayout布局，布局用TextView设置文字。
SplashActivity活动使用Handler().postDelayed实现延迟启动，具体代码
```java
        // 延迟后执行跳转逻辑,Handler 是 Android 中用于线程间通信和消息发送的工具
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, 2000);//延迟时长

```
如注释所说，Handler 是 Android 中用于线程间通信和消息发送的工具，他的方法postDelayed可以实现延迟通信，而在传入参数时使用了() -> {}这样的Lambda 表达式，这类表达式只在我学习python时出现过，而此次培训我在看课堂上实例项目时好奇为什么实际开发中用这种方式，参考ai给我的回答是java也有这种快速打包的方法，在安卓开发中常用，用来简化反复的类申请，于是我的后面的代码几乎都使用了Lambda 表达式。然后Intent就是最基础的显示跳转，跳转至登录界面。
#### 2、DatabaseHelper数据库设置以及LoginActivity登录部分
由于本项目无法连接云端服务器，我通过 SQLite 本地数据库实现数据存储以及记录账号信息、点赞信息等等。我创建⼀个继承⾃ SQLiteOpenHelper 的类DatabaseHelper，onCreate 方法中，定义了三张核心数据表：
```java
 public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (username TEXT PRIMARY KEY, password TEXT)");
        db.execSQL("CREATE TABLE posts (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, image_url TEXT, author TEXT, description TEXT)");
        db.execSQL("CREATE TABLE likes (post_id INTEGER, username TEXT, PRIMARY KEY(post_id, username))");//初始化建表

```
表1是用户表，username 作为主键，确保账号唯一性。表2是帖子表，存储所有经验贴。表3是点赞关系表,记录"谁"点赞了"哪条帖子",用联合主键 (post_id + username) 物理层面防止重复点赞。
我没有手动录入数据,而是利用网络图片和豆包生成的文本描述随机生成帖子,具体做法是首先创建三个库：标题库String[] TITLES,图片库String[] IMAGES,内容库String[] DETAILS,他们的内容形式如下：
```java
  TITLES ："周末去哪里？这家咖啡馆绝了！"
  IMAGES ："https://images.pexels.com/photos/45201/kitty-cat-kitten-pet-45201.jpeg?auto=compress&cs=tinysrgb&w=500"
  DETAILS ："下班路上偶然拐进一条小巷，居然藏着这么治愈的小店！环境超舒服，已经列入常去清单～# 探店 #生活碎片"
```
其中图片来自pexel网站，方便国内网络访问。在随机生成帖子过程中我使用了db.beginTransaction() 和 db.setTransactionSuccessful()等事务操作方法，使得插入操作在内存中打包，最后一次性写入磁盘，不会出现循环中插入多条数据每次都会出现I/O.
```java
  private void generateBatchData(SQLiteDatabase db, int count) {//随机生成帖子逻辑
    db.beginTransaction();
    try {
        for (int i = 0; i < count; i++) {
            ContentValues values = new ContentValues();
            values.put("title", TITLES[random.nextInt(TITLES.length)]);
            ...
            db.insert("posts", null, values);
			...
        }
        db.setTransactionSuccessful();
    } finally {
        db.endTransaction();
}
```
同时为了模拟真实的经验模块，我设置了永远刷不到底的功能性设置，即当刷到所有帖子时，会再次生成一定数量的贴子，所以在 DatabaseHelper里我还设置了一个generateMoreRandomData（）方法：
```java
 public void generateMoreRandomData(int count) {//随机生成更多帖子逻辑
        SQLiteDatabase db = this.getWritableDatabase();
        generateBatchData(db, count);
 }
```
具体实现永远刷不到底的功能逻辑体现在主activity中。当然我也简单内置了发帖功能，由于时间的缘故，发帖功能并不复杂，只能输入帖子标题，用于发帖数据库设置了addPost()方法。queryPosts()方法实现查询功能，把查到的原始数据，转换成 Java 对象列表，后面的方法以及作用如下，具体代码参考代码细节。
```java
getPostsByPage()//往上滑顺序加载帖子
getRandomPosts()//往下拉刷新随机帖子（模拟推荐喜欢，由于事件原因没办法设计推荐算法只能模拟）
getUserPosts()//获取用户个人主页
toggleLike()//点赞功能，即填写likes表。
loginOrRegister()//判断是登录还是注册
```
针对登陆部分LoginActivity，为了简化，我设计了自动判断账号状态。如果账号已存在，则校验密码；如果账号不存在，则自动创建账号。其中处理过程为:
```java
btnLogin.setOnClickListener(v -> {
            ...
            if (dbHelper.loginOrRegister(u, p)) {
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("USER", u);
                startActivity(intent);
                finish();
            }
			...loginOrRegister
```
xml界面也相对简洁，使用LinearLayout并用两个EditText组件记录输入和一个Button按钮控制即可。
#### 3、帖子对象PostItem、实现瀑布流的适配器WaterfallAdapter以及每个帖子内容DetailActivity
为了实现瀑布流呈现效果，我设置了一个基础的实体帖子对象PostItem，它能够将数据库表结构与Java对象相对应。其中设置了如id、likeCount、avatarUrl等等字段，avatarUrl字段用来生成模拟头像。生成头像时我使用了DiceBear这一个开源的头像生成库，只需要构造特定的url即可：
```java
 this.avatarUrl = "https://api.dicebear.com/7.x/avataaars/png?seed=" + userName;
```
同时我的PostItem实现了Serializable接口，相当于给这个对象序列化，我理解的是打了个包，主要是后面展示详情页的时候需要一次性传输整个对象包括图片一起传给下一个activity：
```java
 intent.putExtra("POST_DATA", item);//MainActivity发送
 PostItem item = (PostItem) getIntent().getSerializableExtra("POST_DATA");//DetailActivity接收
```
然后是实现瀑布流的关键适配器WaterfallAdapter，其中为了实现下拉刷新和上滑加载，添加两个方法：
```java
    public void resetData(List<PostItem> newData) {
        this.dataList.clear();
        this.dataList.addAll(newData);//删除刷新
        notifyDataSetChanged();
    }//下拉刷新
    public void appendData(List<PostItem> newData) {
        int startPos = this.dataList.size();
        this.dataList.addAll(newData);
        notifyItemRangeInserted(startPos, newData.size());//在队列尾部插入
    }//上拉加载
```
Adapter 不应该包含业务逻辑,因此我定义了一个接口，把点击事件抛给 MainActivity 处理
```java
   public interface OnItemClickListener {
    void onLikeClick(int position, PostItem item);    // 点击了爱心区域,需要处理点赞数据
    void onPostClick(PostItem item);    // 点击了卡片整体,需要跳转详情页
}
```
同时为了优化点赞的视觉感受，我设计了点暂时会由红星q弹效果，算是一类视觉优化：
```java
holder.layoutLike.setOnClickListener(v -> {
    listener.onLikeClick(position, item);  //通知外部 Activity 更新数据状态
    if (item.isLiked) {
        holder.ivLikeIcon.setImageResource(R.drawable.ic_heart_filled);    // 切换为实心红心
        holder.ivLikeIcon.setScaleX(0.8f);// 起始状态略微缩小
        holder.ivLikeIcon.animate()// 先重置大小 -> 快速放大到 1.2倍 -> 回弹到 1.0倍
                .scaleX(1.2f).scaleY(1.2f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator(4f))
                .start();
        ...
```
ViewHolder()方法用于缓存读取每一个帖子所包含的控件比如名字图片标题等等。我了解到瀑布流布局StaggeredGridLayout开发时通常都会用这个方法防止每次列表滑动时都会重新去找控件，导致卡顿。这样通过onCreateViewHolder可以连接到xml布局文件，并且通过onBindViewHolder根据位置布局每一个帖子。同时为了实现图片从主页到详情页加载更快，我使用了图片缓存策略，后面头像加载同理。至于每个帖子布局文件参考waterfall_card.xml。具体还是使用LinearLayout等，这里不多赘述。
```java
Glide.with(context)
        .load(item.imageUrl)
        .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存图片，切到详情页加载更快，同时主页也有缓存
        .placeholder(android.R.drawable.ic_menu_gallery)
        .into(holder.ivCover);
```
至于DetailActivity，就是一个基础的帖子细节展示界面，值得注意的是它接受了整个帖子对象（上文有提到）以及他也使用了缓存策略，使得进入该界面是不需要再加载图片，其布局文件就是放大版的帖子布局文件，这里不多赘述。
#### 4、模拟后端接口仓库PostRepository
在最开始我的项目本身是直接用MainActivity连接底层数据库，没有线程分离也没有异步设置，经过同学在群里的提问，我去网上查阅资料并通过ai了解到，现如今的真实客户端开发中都会在云服务器上有后端程序以及真实的网络接口，我只能在本地进行简单的模拟，这样更符合开发逻辑。于是我结合设计了PostRepository作为本项目架构中最核心的中间层，他既包含了的数据获取方法（访问数据库的工作）,也包含了具体实施过程,例如是直接查库，还是先造数据再查,同时他也包含了模拟网络延迟的功能。我的整个结构图如下：
```java
│   [ 1. 前端界面 (MainActivity) ]        │
│           │ (调用方法)                  │
│   [ 2. 伪装接口 (PostRepository) ]      │
│           │ (开启Thread线程模拟耗时)     │
│   [ 3. 业务逻辑 (DatabaseHelper) ]      │
│           │ (读写本地文件)              │
│   [ 4. 本地文件 (SQLite .db 文件) ]     │
```
在真实网络请求中，因为请求慢，不能立马return数据。所以MainActivity在调用方法时，必须传这个接口进去。等 Repository查完数据后，会调用onSuccess把数据推给MainActivity。所以首先是设置回调接口，
```java
public interface Callback<T> {
    void onSuccess(T data);
    void onError(String msg);
}
```
然后后面提供了获取首页列表getHomePosts,随即推荐getRandomPosts,个人数据主页getUserPosts,点赞toggleLike,发帖addPost等方法，都是使用了new Thread来模拟子线程以及网络延迟（这里我查询了ai询问如何模拟，ai建议我用new Thread）。我以getHomePosts举例，首先new Thread开启子线程，然后用Thread模拟延迟，之后针对上面所说的可以无限往下滚动即看完帖子后再自动生成帖子，最后用try-catch来防止发生错误。其他的方法详情参考具体代码。
```java
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
```
通过这样的设计，MainActivity不需要知道DatabaseHelper的存在，也不需要知道SQL怎么写。它只需要找 Repository要数据即可，这符合实际开发。
#### 5、主控制活动MainActivity
主活动MainActivity其实主要负责整个UI的逻辑，首先是整体分为两个板块，经验板块和个人主页板块，第一个问题就是这两者的切换。首先我设计了底部按钮，用BottomNavigationView定义按钮布局，然后关联到文件menu/bottom_nav_menu.xml设置按钮图片：
```java
 <com.google.android.material.bottomnavigation.BottomNavigationView
        android:id="@+id/bottom_navigation"
        ...
        app:menu="@menu/bottom_nav_menu"
        ...
        app:layout_constraintBottom_toBottomOf="parent"/>
```
BottomNavigationView会自动维护每个按钮的状态，例如点击“我”时，系统会把“我”这个按钮标记为 checked=true，把“经验”标记为 checked=false，通过我提前设置的selector文件，可以做到点击时切换的效果。然后我使用了安卓自带的ViewPage2，ViewPager2 的底层其实就是一个 RecyclerView，所以我写了一个MainPagerAdapter来提供每一板块的页面：
```java
class MainPagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  @Override
  public int getItemCount() {return 2}//总页数
  @Override
  public int getItemViewType(int position) {return position;}//当前是哪一页
  class HomeViewHolder extends RecyclerView.ViewHolder {...}//加载经验页
  class ProfileViewHolder extends RecyclerView.ViewHolder {...}//加载个人主页
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == 0) {...} //如果是第 0 页，加载 page_home.xml经验页布局
            else {}//如果是第 1 页，加载 page_profile.xml个人页布局
        }
  ...
}
```
然后在onBindViewHolder分别通过initHomeLogic()和 initProfileLogic()从page_home.xml和page_profile.xml两个文件里找两个页面的控件并绑定，针对initHomeLogic()，他也进行了初始化瀑布流布局：
```java
homeLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);//创建瀑布流管理
homeLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);//防止布局乱跳
rvHome.setLayoutManager(homeLayoutManager);
rvHome.setItemAnimator(null);
```
在首页加载中我使用了一个PreloadScrollListener监听器，其实就是实现预加载图片以及永远滑不到底的设计。
首先我们看里面的第一个方法onScrollStateChanged(),我使用homeLayoutManager.invalidateSpanAssignments()是为了强制布局管理器重新计算一下格子的分配，而if (newState == RecyclerView.SCROLL_STATE_IDLE) 时，也就是不划动页面时，使用preloadImages()方法，该方法首先要先定位，找出每一列最下面那个可见的 Item 是第几个：
```java
int[] lastPositions = new int[homeLayoutManager.getSpanCount()];
homeLayoutManager.findLastVisibleItemPositions(lastPositions);//定位最下方的在哪个位置
int lastVisiblePos = findMax(lastPositions);
```
然后设置循环执行n次，也就是预加载n个即将到来的帖子，这里使用.preload()，只加载不显示，我这里n设置的6：
```java
for (int i = 1; i <= 6; i++) {
  int targetPos = lastVisiblePos + i;// 算出目标位置，比如当前看到第 11 个，那我要加载第 12,13,...个
  if (targetPos < dataList.size()) {// 不能超出总数
  Glide.with(MainActivity.this)
         .load(dataList.get(targetPos).imageUrl)
         .diskCacheStrategy(DiskCacheStrategy.ALL)
         .preload();//只加载不显示，存到缓存里即可
    }
  }
```
这样就完成了预加载功能。而在实现永远滑不到底的逻辑时，同样是获取最底部信息，并首先生成新数据然后加载新数据：
```java
int[] lastPositions = new int[homeLayoutManager.getSpanCount()];
     homeLayoutManager.findLastVisibleItemPositions(lastPositions);
     int lastVisibleItemPos = findMax(lastPositions);
     int total = homeLayoutManager.getItemCount();
     if ((total - lastVisibleItemPos) <= 4 && !isHomeLoading && total > 0) {//剩余未读数量
     loadMoreHomeData();//加载新数据
```
而加载个人页面时本质同理只是单纯生成页面，只是在个人页面我设置了 btnLogout.setOnClickListener退出登录的功能按键方便测试账号。
后续的是通过连接reposter接口层访问数据库信息读取数据，firstLoadHome()首次加载，refreshHomeRandomly() 下拉刷新，loadMoreHomeData()无限滚动时生成更多，loadProfileData()加载个人主页以及 handleLike(...) 更新点赞数据。这里以firstLoadHome()为例，它使用repository里的方法getHomePosts()请求访问数据库并通过callback返回信息，且设置点赞和进入细节页面的点击功能，如下代码所示。其余方法类似具体参考代码细节：
```java
private void firstLoadHome() {//首次加载
        if (swipeRefreshHome != null) swipeRefreshHome.setRefreshing(true);
        repository.getHomePosts(currentUser, 0, PAGE_SIZE, new PostRepository.Callback<List<PostItem>>() {
            @Override
            public void onSuccess(List<PostItem> data) {
                adapterHome = new WaterfallAdapter(MainActivity.this, data, new WaterfallAdapter.OnItemClickListener() {
                    @Override
                    public void onLikeClick(int position, PostItem item) {
                        handleLike(item, position, adapterHome);//点赞记录
                    }
                    @Override
                    public void onPostClick(PostItem item) {
                        navigateToDetail(item);///点击到详情页
                    }
                });
                 ...
```
而下拉刷新通过SwipeRefreshLayout，这个容器会检测手势并在下拉时自动把藏在顶部的圆圈进度条拉出来，而实现代码只需要使用重新读取数据库并重新填充即可。而实现功能单列变双列则是很简单的绑定按钮初始化为双列，然后按下后切换单列后设置判断并清空页面再重新加载为单列，xml文件里布局一个切换按钮即可。值得注意的是为什么我切换后帖子还是能够自适应屏幕呢？是因为我使用了match_parent，这能使它无论什么情况都贴合父类，所以能够自适应屏幕：
```java
private void switchLayoutMode() {
    isSingleColumn = !isSingleColumn;//按键按下后自动切换模式
    homeLayoutManager.setSpanCount(isSingleColumn ? 1 : 2);//模式判断
    if (adapterHome != null) adapterHome.notifyItemRangeChanged(0, adapterHome.getItemCount());//强制重绘
    Toast.makeText(this, isSingleColumn ? "单列模式" : "双列模式", Toast.LENGTH_SHORT).show();//输出提示框
    }
```
然后还有一个关键功能则是自己可以发布新帖，虽然由于时间紧张没来得及设置图片上传，只能简单的输入文本上传自动配图，但是也算是完整功能。首先我布局了发帖案件，然后发帖使用了最基础的弹框输入形式final EditText input = new EditText(this)，并使用AlertDialog.Builder来组装弹窗，如，而发帖同样通过虚构的repository后端接口设计的方法addPost发帖，如下代码所示。发帖够加载到个人主页页面里，个人主页的展示基本雷同这里不做展示。
```java
   .setPositiveButton("发布", (dialog, which) -> {
            String content = input.getText().toString();
            if (!content.isEmpty()) {
            repository.addPost(content, currentUser, new PostRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
            Toast.makeText(MainActivity.this, "发布成功", Toast.LENGTH_SHORT).show();
                 if (swipeRefreshHome != null) {
                     swipeRefreshHome.setRefreshing(true);//同样开启转圈提示
                     }
					 refreshHomeRandomly();//发完贴后重新拉数据
              }
          ...
```
同时我为了让弹窗更好看还设置了圆角弹窗，即corners android:radius="20dp"。页面优化如顶部状态栏颜色对齐使ui美观等代码详情参考具体代码。
最后则是详情页的跳转，只需要用最简单的页面跳转即可：
```java
Intent intent = new Intent(MainActivity.this, DetailActivity.class);
```
## 可拓展的设想
我还想添加搜索功能，数据库与数据仓库层已预置基础查询接口，但是在我尝试实现搜索功能时出现与无限下滑这一功能出现冲突导致搜索时还能无限造帖子出现bug，并且由于时间紧张实在无法完善此功能，于是只能放弃展示，但我认为搜索功能任然是一个可拓展的重要功能。
我还了解到，我在读取数据库数据时list只会增加越来越多，而真实项目开发中如果真的有人往下翻页100万条帖子，那么就会出现系统崩溃。所以大厂一般是用“分页库 (Paging Library)” 的技术，它会自动把第1页到第50页的数据从内存里扔掉，这是我学习到的新知识。



