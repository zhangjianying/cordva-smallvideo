package com.zsoftware.smallvideo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.mabeijianxi.smallvideorecord2.JianXiCamera;
import com.mabeijianxi.smallvideorecord2.MediaRecorderActivity;
import com.mabeijianxi.smallvideorecord2.model.MediaRecorderConfig;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmallVideo extends CordovaPlugin {
    private Activity activity;
    private CallbackContext _callbackContext;
    private int videoTime = 0;
    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
    };

    /**
     * check application's permissions
     */
    public boolean hasPermisssion() {
        for (String p : permissions) {
            if (!PermissionHelper.hasPermission(this, p)) {
                return false;
            }
        }
        return true;
    }

    public void requestPermissions(int requestCode) {
        PermissionHelper.requestPermissions(this, requestCode, permissions);
    }

    /**
     * processes the result of permission request
     *
     * @param requestCode  The code to get request action
     * @param permissions  The collection of permissions
     * @param grantResults The result of grant
     */
    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        PluginResult result;
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                result = new PluginResult(PluginResult.Status.ILLEGAL_ACCESS_EXCEPTION);
                this._callbackContext.sendPluginResult(result);
                return;
            }
        }

        switch (requestCode) {
            case 0:
                toActiviey(this.videoTime);
                break;
        }
    }


    /**
     * 重写方法
     *
     * @param action          The action to execute.
     * @param args            The exec() arguments.
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return
     * @throws JSONException
     */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        activity = this.cordova.getActivity();
        this._callbackContext = callbackContext;

        if ("showSmallVideo".equals(action)) {
            //拍摄小视频
            String path = null;
            int time = 30;
            try {
                JSONObject obj = args.getJSONObject(0);
                if (obj.has("time")) {
                    time = obj.getInt("time");
                    if (time <= 0) {
                        time = 30;
                    }
                }
            } catch (Exception e) {
            }
            final int videoMaxTime = time;
            this.videoTime = videoMaxTime;
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!hasPermisssion()) {
                        requestPermissions(0);
                    } else {
                        toActiviey(videoMaxTime);
                    }

                }
            });

            return true;
        } else if ("smallVieoDeleteDir".equals(action)) {
            //清空目录
            smallVieoDeleteDir();
            return true;
        } else if ("smallVieoPathSize".equals(action)) {
            smallVieoPathSize();
        }
        return false;
    }

    //删除视频目录内的文件
    public void smallVieoDeleteDir() {
        try {
            pathDelete(getDataPath());
            _callbackContext.success("删除成功");
        } catch (Exception e) {
            _callbackContext.error("删除文件失败");
        }
    }

    //得到文件大小
    public void smallVieoPathSize() {
        try {
            String size = getPathSize(getDataPath());
            _callbackContext.success(size);
        } catch (Exception e) {
            _callbackContext.error("无法获取");
        }
    }


    private void toActiviey(int time) {
        try {
            String path = getDataPath();
            //设置小视频保存地址
            JianXiCamera.setVideoCachePath(getDataPath());
            JianXiCamera.initialize(true, getDataPath());
            MediaRecorderConfig mediaRecorderConfig = new MediaRecorderConfig.Buidler()
                    .recordTimeMax(time * 1000)
                    .build();

            Intent intent = new Intent(activity, MediaRecorderActivity.class);
            intent.putExtra(MediaRecorderActivity.MEDIA_RECORDER_CONFIG_KEY, mediaRecorderConfig);
            this.cordova.startActivityForResult(this, intent, 1234);
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
        _callbackContext.error("打开小视频错误");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1234 && this._callbackContext != null) {
            if (resultCode == -1) {
                //文件夹地址
                String dir = data.getStringExtra(MediaRecorderActivity.OUTPUT_DIRECTORY);
                //视屏地址
                String path = data.getStringExtra(MediaRecorderActivity.VIDEO_URI);
                // 视屏截图地址
                String hot = data.getStringExtra(MediaRecorderActivity.VIDEO_SCREENSHOT);
                JSONObject ret = new JSONObject();
                try {
                    ret.put("dir", dir);
                    ret.put("path", path);
                    ret.put("screenshotPath", hot);
                    _callbackContext.success(ret);
                    return;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                _callbackContext.error("未获取到视频");
            } else {
                _callbackContext.error("操作取消");
            }
        }
    }


    //得到录视频目录
    private String getDataPath() {
        String path = null;
        String sdcardPath = null;
        sdcardPath = Environment.getExternalStoragePublicDirectory("DIRECTORY_MOVIES").getPath();
        path = sdcardPath + "/smallVideo/";


        return path;
    }

    // 递归得到文件大小
    private static long getFileSize(File f) throws Exception {
        // 取得文件夹大小
        long size = 0;
        File flist[] = f.listFiles();
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getFileSize(flist[i]);
            } else {
                size = size + flist[i].length();
            }
        }
        return size;
    }

    // 得到文件路径下的文件大小
    public static String getPathSize(String path) {
        long length = 0;
        try {
            File ff = new File(path);
            if (ff.isDirectory()) { // 如果路径是文件夹的时候
                length = getFileSize(ff);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getFileSizeStr(length);
    }

    // 删除目录下的所有文件
    public static void pathDelete(String path) {
        try {
            File ff = new File(path);
            deleteFile(ff);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 删除文件安全方式：
    private static void deleteFile(File file) {
        if (file.isFile()) {
            deleteFileSafely(file);
            return;
        }
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                deleteFileSafely(file);
                return;
            }
            for (int i = 0; i < childFiles.length; i++) {
                deleteFile(childFiles[i]);
            }
            deleteFileSafely(file);
        }
    }

    /**
     * 安全删除文件.
     *
     * @param file
     * @return
     */
    public static boolean deleteFileSafely(File file) {
        if (file != null) {
            String tmpPath = file.getParent() + File.separator
                    + System.currentTimeMillis();
            File tmp = new File(tmpPath);
            file.renameTo(tmp);
            return tmp.delete();
        }
        return false;
    }

    // 字节转说明
    public static String getFileSizeStr(long fileSize) {
        String sFileSize = "0KB";
        if (fileSize > 0) {
            double dFileSize = (double) fileSize;

            double kiloByte = dFileSize / 1024;
            // if (kiloByte < 1) {
            // return sFileSize + "S";
            // }
            double megaByte = kiloByte / 1024;
            if (megaByte < 1) {
                sFileSize = String.format("%.2f", kiloByte);
                return sFileSize + "K";
            }

            double gigaByte = megaByte / 1024;
            if (gigaByte < 1) {
                sFileSize = String.format("%.2f", megaByte);
                return sFileSize + "M";
            }

            double teraByte = gigaByte / 1024;
            if (teraByte < 1) {
                sFileSize = String.format("%.2f", gigaByte);
                return sFileSize + "G";
            }

            sFileSize = String.format("%.2f", teraByte);
            return sFileSize + "TB";
        }
        return sFileSize;
    }

}
