# cordva-smallvideo
小视频录制插件


基于FFmpeg项目的小视频录制插件,只有android端
(请忽略项目名,少打一个字母)

```
安装:  cordova plugin add https://github.com/zhangjianying/cordva-smallvideo.git
```

## 使用方式:
```javascript
  cordova.plugins.smallvideo.showSmallVideo({time:30},function(res){
             alert(res.dir+' : '+res.path)
              cordova.plugins.smallvideo.smallVieoPathSize(function(size){
                alert(size)
              })
   },function(msg){})
```


## API说明:
showSmallVideo :开启小视频录制, android6.0以上第一次启动可能会需要用户授权.授权后第二次可正常启动.  time 参数表示最大录制多长时间.  返回参数:
  {
    dir :目录 , path:完整mp4路径
  }

![Alt text](https://github.com/zhangjianying/cordva-smallvideo/raw/master/readme/1.jpg)
 ![Alt text](https://github.com/zhangjianying/cordva-smallvideo/raw/master/readme/2.jpg)

 
smallVieoDeleteDir: 删除视频存储目录



smallVieoPathSize: 获取视频存储目录的大小

![Alt text](https://github.com/zhangjianying/cordva-smallvideo/raw/master/readme/3.jpg)

