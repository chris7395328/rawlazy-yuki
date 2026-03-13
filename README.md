# RawLazy Mihon Extension

这是一个为 Mihon (原 Tachiyomi) 开发的 RawLazy 漫画源插件。

## 项目结构

```
rawlazy/
├── build.gradle.kts              # Gradle 构建配置
├── settings.gradle.kts           # Gradle 设置
├── gradle.properties             # Gradle 属性
├── proguard-rules.pro            # ProGuard 规则
├── src/main/
│   ├── AndroidManifest.xml       # Android 清单文件
│   └── kotlin/
│       └── eu/kanade/tachiyomi/extension/all/rawlazy/
│           └── RawLazy.kt        # 主插件类
└── README.md                      # 说明文档
```

## 功能说明

🎉 **所有功能已实现！**

✅ **热门漫画** - 从侧边栏的排行榜获取（`div.top_sidebar div.entry`）
✅ **最新更新** - 从主内容区获取（`div.row-of-mangas div.entry-tag`）
✅ **搜索功能** - 使用 `?s_manga=` 参数搜索
✅ **漫画详情** - 解析标题、封面、简介、标签（`h1.font-bold`、`img.thumb`、`div.content-text`、`div.genres-wrap`）
✅ **章节列表** - 解析章节并自动反序（`div.chapters-list a`）
✅ **图片加载** - 通过 AJAX 请求动态加载图片，处理多次请求分页

## 已完成的更新

1. ✅ 搜索参数修正 - 使用 `?s_manga=` 而非 `?s=`
2. ✅ 漫画详情解析 - 支持正确的标题、封面、简介、标签提取
3. ✅ 章节列表解析 - 从 `div.chapters-list` 正确获取章节
4. ✅ 章节反序 - 自动将最新章节在前的列表反转为从旧到新
5. ✅ AJAX 图片加载 - 实现完整的 AJAX 请求流程，提取 nonce、p、chapter_id 等参数，多次请求获取所有图片
6. ✅ 防盗链处理 - 添加正确的 Referer 和 User-Agent 头部
7. ✅ 备用方案 - 如果 AJAX 失败，回退到直接解析页面图片

## 如何编译

### 前置要求
1. 安装 Android Studio
2. 安装 JDK 17

### 编译步骤
1. 克隆或下载此项目
2. 用 Android Studio 打开项目
3. 等待 Gradle 同步完成
4. 运行命令：
   ```bash
   ./gradlew assembleDebug
   ```
5. 生成的 APK 文件位于 `app/build/outputs/apk/debug/` 目录

## 技术细节

### AJAX 图片加载流程

1. **提取参数** - 从页面 JavaScript 中提取：
   - `ajax_url` - AJAX 请求地址
   - `nonce` - 安全令牌
   - `p` - 文章 ID
   - `chapter_id` - 章节 ID

2. **发送请求** - POST 请求到 `wp-admin/admin-ajax.php`，参数包括：
   - `action: z_do_ajax`
   - `_action: decode_images`
   - `p`, `chapter_id`, `img_index`, `content`, `nonce`

3. **处理响应** - JSON 响应包含：
   - `mes` - 图片 HTML 内容
   - `going` - 是否继续请求 (1=继续, 0=完成)
   - `img_index` - 当前图片索引

4. **循环请求** - 持续请求直到 `going=0` 或达到最大尝试次数

## 注意事项

✅ **当前状态**：插件功能完整！可以正常使用了！

祝你使用愉快！📚
