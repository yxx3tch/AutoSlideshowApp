package jp.techacademy.yxx3tch.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    Timer mTimer;
    Handler mHandler = new Handler();

    ArrayList<Uri> uriArray = new ArrayList<Uri>();
    int selector = 0;

    Button backButton;
    Button playButton;
    Button forwardButton;

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        backButton = (Button)findViewById(R.id.back_button);
        playButton = (Button)findViewById(R.id.play_button);
        forwardButton = (Button)findViewById(R.id.forward_button);

        imageView = (ImageView)findViewById(R.id.imageView);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selector = selector > 0 ? selector - 1 : uriArray.size() - 1;
                imageView.setImageURI(uriArray.get(selector));
            }
        });

        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selector = selector < uriArray.size() - 1 ? selector + 1 : 0;
                imageView.setImageURI(uriArray.get(selector));
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTimer == null) {
                    playButton.setText("停止");
                    backButton.setEnabled(false);
                    forwardButton.setEnabled(false);
                    // タイマーの作成
                    mTimer = new Timer();
                    // タイマーの始動
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            selector = selector < uriArray.size() - 1 ? selector + 1 : 0;
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    imageView.setImageURI(uriArray.get(selector));
                                }
                            });
                        }
                    }, 2000, 2000);
                } else {
                    playButton.setText("再生");
                    backButton.setEnabled(true);
                    forwardButton.setEnabled(true);
                    mTimer.cancel();
                    mTimer = null;
                }
            }
        });

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mTimer != null) {
            playButton.setText("再生");
            backButton.setEnabled(true);
            forwardButton.setEnabled(true);
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                } else {
                    // 許可がない場合ボタンを操作不能にする
                    playButton.setEnabled(false);
                    backButton.setEnabled(false);
                    forwardButton.setEnabled(false);
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo() {
        // すでにURIがセットされていた場合削除
        uriArray.clear();

        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        if (cursor.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = cursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                uriArray.add(imageUri);
                Log.d("ANDROID", "URI : " + imageUri.toString());
            } while (cursor.moveToNext());
        }
        cursor.close();
        if (uriArray.size() == 0) {
            // 画像がない場合ボタンを操作不能にする
            playButton.setEnabled(false);
            backButton.setEnabled(false);
            forwardButton.setEnabled(false);
            imageView.setImageDrawable(null);
        } else {
            playButton.setEnabled(true);
            backButton.setEnabled(true);
            forwardButton.setEnabled(true);
            imageView.setImageURI(uriArray.get(selector));
        }
    }

}
