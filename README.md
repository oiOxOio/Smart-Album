# smartAlbum

2021年软件杯赛题 A5 云端智能相册应用  
[视频演示](https://www.bilibili.com/video/BV1iv411J7Dm?share_source=copy_web) [在线演示](http://smartalbum.5hp.cc)  

## 项目介绍

### 总体背景

随着手机像素越来越高，对于手机可以换但是照片不能丢的你来说，手机容量是不是越来越不够用了？云相册可以很好的解决这个棘手的问题，但是相册只有存储功能是远远不够的。基于云上的大量计算资源，我们可以让相册越来越智能。

### 总体目标

以“**智能云相册**”为主要应用方向，向企业、高校和创业团队征集具有创新性并符合行业发展趋势商业应用、创意设计方面的优秀项目。

### 功能要求

#### 基本功能

> 1. 软件可以实现照片的**批量上传、下载**和**展示**
> 2. 软件可以实现照片类别的**自动识别与归类**
> 3. 软件可以实现基于人脸的**自动检索与聚合**
> 4. 软件可以实现**精彩时刻**的自动化剪辑
> 5. 软件具备基础**Web系统界面**
> 6. 软件可运行在**Windows**或**Linux**平台

#### 附加

> 1. 软件需要使用到**移动云**云上AI能力
> 2. 演示时照片**不少于**200张，照片类型不得少于**5类**
> 3. 创新功能需具备自主知识产权/不产生产权纠纷
> 4. 作品需要标明使用的开源数据/软件，并标明出处

## 所使用技术栈

**1. Spring Boot**  
**2. Mybatis**  
**3. Thymeleaf**  
**4. Bootstrap**  
**5. jQuery**  
**6. Mysql**
## 项目启动
### 前期准备
+ 数据库导入[sql文件](https://github.com/oIOxOIo/Smart-Album/blob/master/smartalbum.sql)
+ 前往移动云平台开通对象存储服务，并创建两个对象存储桶（名字自定）
+ 前往移动云平台开通通用图像识别、人体检测与属性识别、车辆检测与属性识别
+ 完善 application.yml 配置信息，其它配置可按需更改 
```yaml
  spring:  
    #邮箱连接配置(注册账号时用到)  
    mail:  
    username: #例 1583298997@qq.com  
    password: #不是邮箱的密码，如果是qq邮箱的话得在qq邮件设置中申请应用的授权码  
    host: smtp.qq.com

  # 移动云相关服务配置信息
  ecloud:
    #  放原图的对象存储桶
    bucketName: 
    #  用来存放用户的其它资源（相册背景图片/精彩时刻视频/图片缩略图等）
    bucketName2: 
    
    #  对象存储服务的ak、sk和地区
    ossAccessKey: 
    ossSecretKey: 
    ossEndPoint: 
    
    #  云API的ak和sk
    ecloudAccessKey: 
    ecloudSecretKey: 

```
### 运行启动类即可启动项目
```java

@SpringBootApplication
@MapperScan("com.example.smartalbum.dao")
@EnableTransactionManagement
@EnableScheduling
public class SmartAlbumApplication {
  public static void main(String[] args) {
      SpringApplication.run(SmartAlbumApplication.class, args);
  }
}
```
### 注意事项
+ 如需使用精彩时刻视频功能，请将 [ffmpeg.exe(此为win版本，可在官网下载其它平台版本)](https://oss.5hp.cc/resources/util/ffmpeg.exe) 放置在工作目录下（配置文件中可更改工作目录路径）

## [版本更新历史(v4.0)](version.md)

## [API文档（v3.6）](api.md)

## 参考文献

[S3 OSS对象存储](https://ecloud.10086.cn/op-help-center/develop/202007021593677832392011670.pdf)  
[移动云图像识别](https://ecloud.10086.cn/op-help-center/doc/article/resource-manager/f4fd27bcd2f4b57f3b7fb833057ba8c473f182317a749f41560ebb394ad49441)  
[精彩时刻相册轮播](https://www.bilibili.com/video/BV1eh411C7WH)  
[精彩时刻底部蜡烛](https://www.bilibili.com/video/BV1fK4y127Vr)  
[登录界面背景](https://codepen.io/plavookac/pen/QMwObb)  
[视频处理工具](http://ffmpeg.org/)  

