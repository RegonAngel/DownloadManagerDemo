package com.example.regon.rxjavademo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ProgressBar progressBar;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = (ProgressBar)findViewById(R.id.bar_progress);
        findViewById(R.id.button_download).setOnClickListener(v ->{
            String url = "http://book.ibook8.com/down/epub/27988.epub";
            String directory = getFilesDir().getAbsolutePath();
            DownloadManager.getInstance().download(url,directory,null,observer);
        });
        toast = Toast.makeText(MainActivity.this,null,Toast.LENGTH_SHORT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownloadManager.getInstance().cancelAll();
    }

    private DownLoadObserver observer = new DownLoadObserver(){
        @Override
        public void onNext(DownloadTask value) {
            super.onNext(value);
            progressBar.setProgress(value.getProgress());
        }

        @Override
        public void onError(Throwable e) {
            super.onError(e);
            /*用的小米手机测试*/
            toast.setText("error occured:"+e.getMessage());
            toast.show();
        }

        @Override
        public void onComplete() {
            super.onComplete();
            toast.setText("Download file successfully!");
            toast.show();
        }
    };
}
