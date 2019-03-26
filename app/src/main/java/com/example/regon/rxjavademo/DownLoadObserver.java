package com.example.regon.rxjavademo;


import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * @author shuai
 * @date 2019/1/9
 * @description: 下载监听器
 */
public class DownLoadObserver implements Observer<DownloadTask> {

    protected Disposable d;//可用于取消注册的监听者
    protected DownloadTask downloadInfo;

    @Override
    public void onSubscribe(Disposable d) {
        this.d = d;
    }

    @Override
    public void onNext(DownloadTask value) {
        this.downloadInfo = value;
    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
    }

    @Override
    public void onComplete() {

    }
}
