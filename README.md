# 使用 Gradle 实现一套代码开发多个应用

在文章 [使用 Gradle 对应用进行个性化定制](http://www.imliujun.com/gradle1.html) 中，我们能够针对一个应用的正式服、测试服、超管服等其他版本，进行个性化定制。
这一篇文章我们来点大动作，让你用一套代码构建多个应用。

## 场景介绍

需求：“将某个应用换一套皮肤、第三方账号、后台服务器，改个名字上线，并且以后的新功能同步进行更新”。

当你遇到这样的需求会怎么做呢？

是将项目复制一份，然后修改其中的内容，有新功能的时候再手动复制过来稍微修改一下 UI？

或者可以切换一个分支，在这个分支上修改相关的信息，每次开发完新功能，将代码合并过来，再稍微修改新功能的 UI？

现在我来介绍使用 `Gradle` 的 `flavorDimensions`，实现一份代码构建多个应用。

## 具体实现

老规矩，先上完整的 `Gradle` 配置：

```gradle
android {
    compileSdkVersion 25
    buildToolsVersion "25.0.3"
    defaultConfig {
        minSdkVersion 16
        targetSdkVersion 25
        versionCode gitVersionCode()
    }

    // 配置两个应用的签名文件
    signingConfigs {
        app1 {
            storeFile file("app1.jks")
            storePassword "111111"
            keyAlias "app1"
            keyPassword "111111"
        }

        app2 {
            storeFile file("app2.jks")
            storePassword "111111"
            keyAlias "app2"
            keyPassword "111111"
        }
    }

    buildTypes {
        release {
            // 不显示Log
            buildConfigField "boolean", "LOG_DEBUG", "false"
        }

        debug {
            // 显示Log
            buildConfigField "boolean", "LOG_DEBUG", "true"
            versionNameSuffix "-debug"
            signingConfig null
            manifestPlaceholders.UMENG_CHANNEL_VALUE = "test"
        }
    }

    //创建两个维度的 flavor
    flavorDimensions "APP", "SERVER"

    productFlavors {

        app1 {
            dimension "APP"
            applicationId 'com.imliujun.app1'

            versionName rootProject.ext.APP1_versionName

            //应用名
            resValue "string", "app_name", "APP1"

            buildConfigField("String", "versionNumber", "\"${rootProject.ext.APP1_versionName}\"")

            //第三方SDK的一些配置
            buildConfigField "int", "IM_APPID", "app1的腾讯IM APPID"
            buildConfigField "String", "IM_ACCOUNTTYPE", "\"app1的腾讯IM accountype\""
            manifestPlaceholders = [UMENG_APP_KEY      : "app1的友盟 APP KEY",
                                    UMENG_CHANNEL_VALUE: "app1默认的渠道名",
                                    XG_ACCESS_ID       : "app1信鸽推送ACCESS_ID",
                                    XG_ACCESS_KEY      : "app1信鸽推送ACCESS_KEY",
                                    QQ_APP_ID          : "app1的QQ_APP_ID",
                                    AMAP_KEY           : "app1的高德地图key",
                                    APPLICATIONID      : applicationId]
            //签名文件
            signingConfig signingConfigs.app1
        }

        app2 {
            dimension "APP"
            applicationId 'com.imliujun.app2'

            versionName rootProject.ext.APP2_versionName

            //应用名
            resValue "string", "app_name", "APP2"

            buildConfigField "String", "versionNumber", "\"${rootProject.ext.APP2_versionName}\""

            //第三方SDK的一些配置
            buildConfigField "int", "IM_APPID", "app2的腾讯IM APPID"
            buildConfigField "String", "IM_ACCOUNTTYPE", "\"app2的腾讯IM accountype\""
            manifestPlaceholders = [UMENG_APP_KEY      : "app2的友盟 APP KEY",
                                    UMENG_CHANNEL_VALUE: "app2默认的渠道名",
                                    XG_ACCESS_ID       : "app2信鸽推送ACCESS_ID",
                                    XG_ACCESS_KEY      : "app2信鸽推送ACCESS_KEY",
                                    QQ_APP_ID          : "app2的QQ_APP_ID",
                                    AMAP_KEY           : "app2的高德地图key",
                                    APPLICATIONID      : applicationId]
            //签名文件
            signingConfig signingConfigs.app2
        }

        offline {
            dimension "SERVER"

            versionName getTestVersionName()
        }

        online {
            dimension "SERVER"
        }

        admin {
            dimension "SERVER"

            versionName rootProject.ext.versionName + "-管理员"
            manifestPlaceholders.UMENG_CHANNEL_VALUE = "admin"
        }
    }
}

android.applicationVariants.all { variant ->
    switch (variant.flavorName) {
        case "app1Admin":
            variant.buildConfigField "String", "DOMAIN_NAME",
                    "\"https://admin.app1domain.com/\""
            if ("debug" == variant.buildType.getName()) {
                variant.mergedFlavor.setVersionName(getTestVersionName() + "-管理员")
            } else {
                variant.mergedFlavor.setVersionName(rootProject.ext.APP1_VERSION_NAME + "-管理员")
            }
            break
        case "app1Offline":
            variant.buildConfigField "String", "DOMAIN_NAME",
                    "\"https://offline.app1domain.com/\""
            variant.mergedFlavor.setVersionName(getTestVersionName())
            break
        case "app1Online":
            variant.buildConfigField "String", "DOMAIN_NAME",
                    "\"https://online.app1domain.com/\""
            if ("debug" == variant.buildType.getName()) {
                variant.mergedFlavor.setVersionName(getTestVersionName())
            }
            break
        case "app2Admin":
            variant.buildConfigField "String", "DOMAIN_NAME",
                    "\"https://admin.app2domain.com/\""
            if ("debug" == variant.buildType.getName()) {
                variant.mergedFlavor.setVersionName(getApp2TestVersionName() + "-管理员")
            } else {
                variant.mergedFlavor.setVersionName(rootProject.ext.APP2_VERSION_NAME + "-管理员")
            }
            break
        case "app2Offline":
            variant.buildConfigField "String", "DOMAIN_NAME",
                    "\"https://offline.app2domain.com/\""
            variant.mergedFlavor.setVersionName(getApp2TestVersionName())
            break
        case "app2Online":
            variant.buildConfigField "String", "DOMAIN_NAME",
                    "\"https://online.app2domain.com/\""
            if ("debug" == variant.buildType.getName()) {
                variant.mergedFlavor.setVersionName(getApp2TestVersionName())
            }
            break
    }
}
```

```gradle
ext {
    APP1_VERSION_NAME = "2.0.2"
    APP1_TEST_NUM = "0001"
    APP2_VERSION_NAME = "1.0.5"
    APP2_TEST_NUM = "0005"
}

def getTestVersionName() {
    return String.format("%s.%s", rootProject.ext.APP1_VERSION_NAME,
            rootProject.ext.APP1_TEST_NUM)
}

def getApp2TestVersionName() {
    return String.format("%s.%s", rootProject.ext.APP2_VERSION_NAME,
            rootProject.ext.APP2_TEST_NUM)
}

static int gitVersionCode() {
    def count = "git rev-list HEAD --count".execute().text.trim()
    return count.isInteger() ? count.toInteger() : 0
}
```

在上一篇文章的配置上进行了一些修改，同时保留上一篇文章里所有的功能。

### 配置多应用

首先来看最重要的一个概念：

```gradle
flavorDimensions "APP", "SERVER"
```

这一行代码配置了两个维度的 `flavor`，`APP` 代表多应用，`SERVER` 代表服务器版本。

根据上面的配置信息可以看到，`app1`、`app2` 设置了 `dimension "APP"` 所以属于 `APP` 这个维度，`offline`、`online`、`admin` 设置了 `dimension "SERVER"` 属于 `SERVER` 这个维度。

根据 Product Flavors 的两个维度 APP [app1, app2] 和 SERVER [offline, online, admin] 以及 Build Type [debug, release]，最后会生成以下 Build Variant：

* `app1AdminDebug`
* `app1AdminRelease`
* `app1OfflineDebug`
* `app1OfflineRelease`
* `app1OnlineDebug`
* `app1OnlineRelease`
* `app2AdminDebug`
* `app2AdminRelease`
* `app2OfflineDebug`
* `app2OfflineRelease`
* `app2OnlineDebug`
* `app2OnlineRelease`

是不是每个应用都有 3 个服务器版本，每个版本都有 `debug` 和 `release` 包。

### 配置不同的包名

我们要实现多应用，必须能安装在同一台手机上。所以不同应用之间的包名得不一样。

在 `APP` 维度的 `flavor` 中设置不同的 `applicationId`，就可以实现修改应用包名。

```gradle
app1{
    applicationId 'com.imliujun.app1'
}

app2{
    applicationId 'com.imliujun.app2'
}
```

这样配置后，`app1` 和 `app2` 就能够安装在同一台手机上，也能同时上传应用商店。

有一点大家切记，`AndroidManifest.xml` 中的 `package` 不需要去修改，R 文件的路径是根据这个 `package` 来生成的。如果对 `package` 进行修改，R 文件的路径也会改变，所有引用到 R 文件的类都需要进行修改。 

### 动态配置 URL 和版本号

既然每个 Build Variant 都是由不同维度的 Product Flavors 和 Build Type 组合而来，我们肯定不能像上一篇文章一样将服务器的 URL 配置在 `offline`、`online`、`admin` 中了，因为 `app1Offline` 和 `app2Offline` 同样是测试服，但不是同一个应用 URL 也不一样。

这个时候就需要通过 task 操作来根据不同的组合设置不同的数据了。

```gradle
android.applicationVariants.all { variant ->
    //判断当前的 flavorName 是什么版本
    switch (variant.flavorName) {
        case "app1Admin":
            //这是 app1 的超管版本，设置超管服务器 URL
            variant.buildConfigField "String", "DOMAIN_NAME",
                    "\"https://admin.app1domain.com/\""
            //判断当前是 `debug` 包还是 `release` 包，设置版本号
            if ("debug" == variant.buildType.getName()) {
                variant.mergedFlavor.setVersionName(getTestVersionName() + "-管理员")
            } else {
                variant.mergedFlavor.setVersionName(rootProject.ext.APP1_VERSION_NAME + "-管理员")
            }
            break
        case "app1Offline":
            variant.buildConfigField "String", "DOMAIN_NAME",
                    "\"https://offline.app1domain.com/\""
            variant.mergedFlavor.setVersionName(getTestVersionName())
            break
        case "app1Online":
            variant.buildConfigField "String", "DOMAIN_NAME",
                    "\"https://online.app1domain.com/\""
            if ("debug" == variant.buildType.getName()) {
                variant.mergedFlavor.setVersionName(getTestVersionName())
            }
            break
        case "app2Admin":
            variant.buildConfigField "String", "DOMAIN_NAME",
                    "\"https://admin.app2domain.com/\""
            if ("debug" == variant.buildType.getName()) {
                variant.mergedFlavor.setVersionName(getApp2TestVersionName() + "-管理员")
            } else {
                variant.mergedFlavor.setVersionName(rootProject.ext.APP2_VERSION_NAME + "-管理员")
            }
            break
        case "app2Offline":
            variant.buildConfigField "String", "DOMAIN_NAME",
                    "\"https://offline.app2domain.com/\""
            variant.mergedFlavor.setVersionName(getApp2TestVersionName())
            break
        case "app2Online":
            variant.buildConfigField "String", "DOMAIN_NAME",
                    "\"https://online.app2domain.com/\""
            if ("debug" == variant.buildType.getName()) {
                variant.mergedFlavor.setVersionName(getApp2TestVersionName())
            }
            break
    }
}
```

两个 APP 的服务器 URL 和版本号不一致，所以通过 task 来动态设置。

### 配置应用名

不同的应用配置自己的应用名：

```gradle
resValue "string", "app_name", "APP1"
```

这行代码的意思和在 `strings.xml` 中定义一个 String 值是一样的。不过这里通过 Gradle 配置了 `app_name` 就不能在 `strings.xml` 中再定义了，会报错提示有冲突。

### 配置应用签名

如果多个应用使用同一个签名文件，按照上一篇文章写的在 `buildTypes` 的 `release` 和 `debug` 中配置就可以。但是每个应用的签名文件不一样呢？

```gradle
signingConfigs {

    app1 {
        storeFile file("app1.jks")
        storePassword "111111"
        keyAlias "app1"
        keyPassword "111111"
    }

    app2 {
        storeFile file("app2.jks")
        storePassword "111111"
        keyAlias "app2"
        keyPassword "111111"
    }
}
```

配置多个签名文件，在 `APP` 这个维度的 `flavor` 中配置签名信息：

```gradle
app1{
    signingConfig signingConfigs.app1
}

app2{
    signingConfig signingConfigs.app2
}
```

这样就可以针对不同的应用设置不同的签名文件了。**但是，还有一个要注意的地方，这个坑我以前没填上，而是绕远路绕过去了，现在我来填上它！**

```gradle
debug {
    signingConfig null
}
```

一定要在 `debug` 中将签名文件的配置置空，不然 Build Type 的权限比 Product Flavors 要高，而 `debug` Build Type(构建类型) 会自动使用 `debug` SigningConfig (签名配置)，这样一来就将 `flavor` 中配置的签名信息给覆盖掉了。导致的问题就是编译 `release` 包没有问题，编译 `debug` 包就不能使用某些需要校验签名的第三方SDK了。

### 配置不同应用的代码和资源

终于来到重头戏了，现在只需要更换 UI、文案或者某些界面布局和逻辑代码就大功告成啦。

首先，建立每个应用对应的 `sourceSets` 目录，比如：

* app1 的 `sourceSets` 位置是 `src/app1/`
* app2 的 `sourceSets` 位置是 `src/app2/`

`app1` 是已经开发完成的应用，只需要换 UI、文案就成了 `app2`，在 `src/app2/` 目录下再新建 `res` 目录，将需要替换的切图命名和 `app1` 中的命名保持一致放入 `res` 对应的目录下就完美换肤了。

文案同理，将需要替换的字符串在 `src/app2/res/values/strings.xml` 中再写一份，保持 `name` 相同，其中的内容随便替换。

布局文件、style、color 替换的规则同上。

微信登录、分享、支付的回调是返回到 `{应用包名.wxapi.WXEntryActivity}`、`{应用包名.wxapi.WXPayEntryActivity}` 这两个 Activity。

我们在 `app1` 和 `app2` 中都放入这两个回调 Activity：

![sourceSets 文件目录](https://user-gold-cdn.xitu.io/2017/7/11/f67d9fbbb117beff6aa66b2dbedc94bd)

然后在 `AndroidManifest.xml` 文件中动态配置 Activity 的包名：

```xml
<!-- 微信分享回调 -->
<activity android:name="${APPLICATIONID}.wxapi.WXEntryActivity"/>
<!-- 微信支付的回调 -->
<activity android:name="${APPLICATIONID}.wxapi.WXPayEntryActivity"/>
```

`APPLICATIONID` 占位符在 Gradle 中设置：

```gradle
manifestPlaceholders = [APPLICATIONID : applicationId]
```

如果使用了 `ShareSDK` 做第三方分享和登录，需要配置 `ShareSDK.xml` 放到 `assets` 文件夹下，将 `main/assets/ShareSDK.xml` 复制一份到 `app2/assets/ShareSDK.xml`，将里面的第三方 APP ID 和 APP KEY 替换一下就可以了。

项目如果使用了 `ContentProvider` 要注意替换 `authorities`，如果 `authorities` 里面的值是一样的，手机上只能装一个应用哦，可以和上面动态配置 Activity 包名一样操作，用信鸽 SDK 演示一下：

```xml
 <!-- 【必须】 【注意】authorities修改为 包名.AUTH_XGPUSH, 如demo的包名为：com.qq.xgdemo -->
<provider
    android:name="com.tencent.android.tpush.XGPushProvider"
    android:authorities="${APPLICATIONID}.AUTH_XGPUSH"
    android:exported="true"/>
```

## 总结

上面的内容基本涉及到所有的方面，其他的细节也好，特殊的需求定制也好，使用上面的方式去处理都能够解决。希望大家不要光学会复制粘贴，要掌握其原理，遇到类似的需求就能举一反三。

> demo地址：https://github.com/imliujun/GradleTest

总结一下技术点：

* `manifestPlaceholders` -> `AndroidManifest.xml` 占位符
* `buildConfigField` -> `BuildConfig` 动态配置常量值
* `resValue` -> `String.xml` 动态配置字符串
* `signingConfigs` -> 配置签名文件
* `productFlavors` -> 产品定制多版本
* `flavorDimensions` -> 为产品定制设置多个维度
* `android.applicationVariants` -> 操作 task

## 相关阅读

* [使用 Gradle 对应用进行个性化定制](http://www.imliujun.com/gradle1.html)
* [Android Studio 3.0 上 Gradle 改动](http://www.imliujun.com/gradle2.html)


<center>欢迎关注微信公众号：**大脑好饿**，更多干货等你来尝</center>

![公众号：大脑好饿 ](https://user-gold-cdn.xitu.io/2017/7/7/e914fa546cd963fbe00a21043e0d5bea)
