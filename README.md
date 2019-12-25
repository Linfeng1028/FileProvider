# FileProvider
Android 7.0+ 拍照&amp;选择图片&amp;图片裁剪 Demo

### 一、前言

从 Android 7.0 开始，安卓系统不允许在应用间使用 `file://` 形式的 Uri，来传递一个 FIle，否则就会抛出 `FileUriExposedException` 异常，因为安卓出于以下原因考虑：

- 接收方 app 可能没有申请 `Manifest.permission.READ_EXTERNAL_STORAGE` 权限，从而导致崩溃

那么我们如果在应用间传递一个文件呢？

官方给出了解决方案，就是使用 `content://` 形式的 Uri 来使接收方 app 获得临时权限。

> 虽然只有在 Android 7.0 以上的系统才会抛出异常，但是还是强烈建议不要使用 file:// 的形式来传递文件

---

### 二、使用 FileProvider

FileProvider 是 ContentProvider 的子类，它提供更高级别的文件访问安全性，是 Android 安全基础结构的关键部分。

在 app 开发过程中需要用到 FileProvider 的场景有使用相机拍照、图片裁剪。

通过以下步骤来使用 FileProvider：

1. <a href="#定义 FileProvider">定义 FileProvid</a>
2. <a href="#指定可共享的文件">指定可共享的文件</a>
3. <a href="#生成 content:// 形式的 Uri">生成 content:// 形式的 Uri</a>
4. <a href="#授予 Uri 临时权限">授予 Uri 临时权限</a>
5. <a href="#向另一个应用提供 content:// 形式的 Uri">向另一个应用提供 content:// 形式的 Uri</a>



#### 定义 FileProvider

- 在 Manifest.xml 中增加 <provider> 标签
-  `android:name` 固定都写 `androidx.core.content.FileProvider`
- `android:authorities` 设为 `包名.fileprovider`，例如 `com.mydomain.fileprovider`
- `android:exported` 固定都写 `false`
- `android:grantUriPermissions` 固定都写 `true`，来为文件授予临时访问权限

完整示例如下：

```xml
<manifest>
    ...
    <application>
        ...
        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="com.mydomain.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            ...
        </provider>
        ...
    </application>
</manifest>
```

如果你需要自定义 FileProvider，那么就将 ` android:name` 标签设为自己 FileProvider 的完整目录，比如 support 包就为 `android.support.v4.content.FileProvider`



#### 指定可共享的文件

FileProvider 只能为预先指定过目录的文件生成 content:// 形式的 Uri。要指定目录，需要在 xml 格式的文件中使用 <paths> 标签来指定其存储路径，例如：

```xml
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <files-path name="my_images" path="images/"/>
</paths>
```

解释一下：

- file-path，等同于 Context.getFilesDir()，其目录为` /data/user/0/包名/files`

- Name，为了加强安全性，此值将隐藏您要共享的子目录的名称
- path，会在 ` /data/user/0/包名/files` 目录下会生成一个 images 的文件夹

也就是说现在你的 /data/user/0/包名/files/images/ 下面的文件具备了提供给外部应用共享的能力了



当然，除了 files-path 标签之外还有许多标签，道理都是一样的，只是路径改变了而已

```xml
Context.getFilesDir()   /data/user/0/com.mydomain/files
<files-path name="name" path="path/"/>

Context.getFilesDir()   /data/user/0/com.mydomain/cache
<cache-path name="name" path="path" />

Environment.getExternalStorageDirectory()   /storage/emulated/0
<external-path name="name" path="path" />

Context.getExternalFilesDir(null)   /storage/emulated/0/Android/data/com.mydomain/files
<external-files-path name="name" path="path" />

Context.getExternalCacheDir()   /storage/emulated/0/Android/data/com.mydomain/cache
<external-cache-path name="name" path="path" />

Context.getExternalMediaDirs()   /storage/emulated/0/Android/media/com.mydomain
<external-media-path name="name" path="path" />
```



最后，将这个 xml 文件应用在你的应用中，假设 xml 名为 `res/xml/file_paths`，那么示例如下：

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="com.mydomain.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```



#### 生成 content:// 形式的 Uri

生成一个 content:// 形式的 Uri 可以分为三步：

- 在具有共享能力的文件夹下创建一个文件夹
- 在上面的文件夹中创建图片文件
- 在 getUriForFile 方法中传入authority 和 上面创建的图片文件，返回获得 content:// 形式的 Uri

代码如下：

```java
File imagePath = new File(Context.getFilesDir(), "images");
File newFile = new File(imagePath, "default_image.jpg");
if (!imagePath.exists()) {
	imagePath.mkdirs();
}
Uri contentUri = getUriForFile(getContext(), "com.mydomain.fileprovider", newFile);
```

最终返回的 contentUri 为 `content://com.mydomain.fileprovider/my_images/default_image.jpg`



#### 授予 Uri 临时权限

通过以下任意一步，来为 getUriForFile 返回的 uri 授予访问权限

- 使用  `Context.grantUriPermission(package, Uri, mode_flags)` 。package 参数用来指定获取临时访问权限的包名，mode_flags  设置为 `Intent.FLAG_GRANT_READ_URI_PERMISSION`或 `Intent.FLAG_GRANT_WRITE_URI_PERMISSION` 或两者都设置
- 调用 Intent 的 setData() 方法，把 uri 传入
- 调用 Intent 的 setFlags() 方法，设置 `Intent.FLAG_GRANT_READ_URI_PERMISSION`或 `Intent.FLAG_GRANT_WRITE_URI_PERMISSION` 或两者都设置

> 注意：当接收 Activity 的堆栈处于活动状态时，在 Intent 中授予的权限仍然有效。堆栈完成后，权限将自动删除。在客户端应用程序中授予一个 Activity 的权限会自动扩展到该应用程序的其他组件。



#### 向另一个应用提供 content:// 形式的 Uri

传递一个 content:// 形式的 Uri 给其他应用有许多方式，最常用的一种是通过 `startActivityForResult()`，将 Intent 传递给另一个应用。





**参考文章：**https://developer.android.com/reference/androidx/core/content/FileProvider?hl=en
