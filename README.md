# RawLazy Mihon Extension

这是一个为 Mihon (原 Tachiyomi) 开发的 RawLazy 漫画源插件。

## 项目结构

```
rawlazy/
├── build.gradle.kts              # 根项目构建配置
├── settings.gradle.kts           # Gradle 设置
├── .github/workflows/
│   └── build.yml               # GitHub Actions 构建配置
└── src/ja/rawlazy/            # 插件源码目录
    ├── build.gradle             # 插件构建配置
    ├── src/
    │   ├── AndroidManifest.xml   # Android 清单文件
    │   └── eu/kanade/tachiyomi/extension/ja/rawlazy/
    │       └── RawLazy.kt      # 主插件类
    └── res/                    # 资源文件（可选）
```

## 功能说明

✅ **热门漫画** - 从侧边栏的排行榜获取（`div.top_sidebar div.entry`）
✅ **最新更新** - 从主内容区获取（`div.row-of-mangas div.entry-tag`）
✅ **搜索功能** - 使用 `?s_manga=` 参数搜索
✅ **漫画详情** - 解析标题、封面、简介、标签（`h1.font-bold`、`img.thumb`、`div.content-text`、`div.genres-wrap`）
✅ **章节列表** - 解析章节并自动反序（`div.chapters-list a`）
✅ **图片加载** - 通过 AJAX 请求动态加载图片，处理多次请求分页

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

## 如何编译

### 在线构建（推荐）

1. 推送代码到 GitHub 会自动触发 GitHub Actions 构建
2. 构建完成后，在 Actions 页面下载 APK 文件
3. 将 APK 安装到 Mihon 应用中

### 本地构建

#### 前置要求
1. 安装 JDK 17
2. 安装 Gradle（可选，项目包含 Gradle Wrapper）

#### 编译步骤
1. 克隆或下载此项目
2. 运行命令：
   ```bash
   gradle :rawlazy:assembleDebug
   ```
3. 生成的 APK 文件位于 `src/ja/rawlazy/build/outputs/apk/debug/` 目录

## 安装到 Mihon

1. 在 Mihon 应用中，进入 `设置` → `安全`
2. 启用 "允许安装来自未知来源的应用"
3. 使用文件管理器找到生成的 APK 文件
4. 点击 APK 文件开始安装
5. 打开 Mihon 应用，进入 `浏览` 标签
6. 点击右上角的 `扩展` 按钮
7. 找到 "RawLazy" 并启用它

## 基于官方插件库

本项目基于 [keiyoushi/extensions-source](https://github.com/keiyoushi/extensions-source) 的项目结构和构建方法，确保与 Mihon/Tachiyomi 生态系统的兼容性。

## 注意事项

- 本插件仅供个人学习和使用
- 请遵守原网站的使用条款
- 如有问题，请提交 Issue 或 Pull Request

## 许可证

Apache License 2.0
