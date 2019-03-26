package com.example.regon.rxjavademo;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * @author shuai
 * @date 2019/1/9
 * @description:封装okhttp3的文件下载器
 */
public class DownloadManager {
    private static final String TAG = "DownloadManager";
    private static final AtomicReference<DownloadManager> INSTANCE = new AtomicReference<>();

    private HashMap<String, Call> downcalls;
    private OkHttpClient mClient;

    public static DownloadManager getInstance(){
        for(;;){
            DownloadManager current = INSTANCE.get();
            if(current != null)
                return current;
            current = new DownloadManager();
            if(INSTANCE.compareAndSet(null,current))
                return current;
        }
    }

    private DownloadManager(){
        downcalls = new HashMap<>();
        mClient = new OkHttpClient.Builder().build();
    }

    /**
     * 取消下载任务
     * @param url url
     */
    public void cancel(String url){
        Call call = downcalls.get(url);
        if(call != null){
            call.cancel();
        }
        downcalls.remove(url);
    }

    /**
     * 取消所有的下载任务
     */
    public void cancelAll(){
        for(String url:downcalls.keySet()){
            cancel(url);
        }
    }

    /**
     * 下载文件
     * @param task 下载任务
     * @param observer 监听器
     */
    public void download(DownloadTask task,DownLoadObserver observer){
        Observable.create(new ObservableOnSubscribe<DownloadTask>() {
            @Override
            public void subscribe(ObservableEmitter<DownloadTask> e) throws Exception {
                DownloadTask downloadTask = task;
                if (downloadTask == null || downloadTask.getUrl()==null){
                    e.onError(new Exception("错误的Url"));
                    return;
                }

                Request request = new Request.Builder()
                        .url(downloadTask.getUrl())
                        .build();
                Call call = mClient.newCall(request);
                downcalls.put(downloadTask.getUrl(), call);

                File file = new File(downloadTask.getDirectory() + File.separator + downloadTask.getFilename());
                FileUtils.createFileByDeleteOldFile(file);
                InputStream is = null;
                FileOutputStream fos = null;
                try {
                    Response response = call.execute();
                    long total = response.body().contentLength();
                    long sum = 0;
                    is = response.body().byteStream();
                    fos = new FileOutputStream(file, false);
                    byte[] buffer = new byte[2048*10];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                        sum += len;
                        Log.i(TAG,"已下载:"+sum+"/"+total);
                        downloadTask.setProgress((int)((double)sum/total*100));
                        /*更新下载进度*/
                        e.onNext(downloadTask);
                    }
                    fos.flush();
                    downcalls.remove(downloadTask.getUrl());
                    e.onComplete();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    e.onError(e1);
                } finally {
                    CloseUtils.closeIO(is, fos);
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

    }

    /**
     * 下载文件
     * @param url url
     * @param directory 目录
     * @param name 文件名
     */
    public void download(String url, @NonNull String directory, @Nullable String name, DownLoadObserver observer){
        download(createDownloadTask(url,directory,name),observer);
    }

    /**
     * 根据url获取构建DownloadInfo
     * @param url url
     * @param directory 目录
     * @return DownloadTask
     */
    private DownloadTask createDownloadTask(String url, String directory) {
        DownloadTask downloadInfo = new DownloadTask(url);
        int lastIndex = url.contains("?")?url.indexOf("?"):url.length();
        String filename = url.substring(url.lastIndexOf("/"), lastIndex);
        downloadInfo.setFilename(filename);
        downloadInfo.setDirectory(directory);
        return downloadInfo;
    }

    /**
     * 构建DownloadInfo
     * @param url url
     * @param directory 目录
     * @param name 文件名
     * @return DownloadTask
     */
    private DownloadTask createDownloadTask(String url,String directory,String name){
        DownloadTask task = createDownloadTask(url,directory);
        if(!TextUtils.isEmpty(name))
            task.setFilename(name);
        return task;
    }

}
