package com.okhttp.demo;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * OkHttp 使用demo
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String HOST_URL = "http://api.juheapi.com/"; // 聚合接口跟地址

    private static final String HISTORY_TODAY_KEY = "d839bb63ffd3bc6dd0199b3fbd64a041"; // 聚合接口 历史上的今天 访问 key

    private static final String HISTORY_TODAY_V = "1.0"; //接口版本号

    MainHandler mainHandler = new MainHandler(new WeakReference<>(this));

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);

        new Thread() {
            @Override
            public void run() {
                super.run();
                fetchHistoryTodayEvent();
            }
        }.start();

    }

    private void fetchHistoryTodayEvent() {
        OkHttpClient httpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url(HOST_URL + "japi/toh?key=" + HISTORY_TODAY_KEY + "&v=" + HISTORY_TODAY_V + "&month=" + 11 + "&day=" + 1)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                String str = response.body().string();
                Message msg = Message.obtain();
                msg.what = MainHandler.GET_RESULT;
                msg.obj = str;
                mainHandler.sendMessage(msg);

            }
        });
    }


    static class MainHandler extends Handler {

        private static final int GET_RESULT = 1;

        private WeakReference<MainActivity> weakReference;

        MainHandler(WeakReference<MainActivity> wk) {
            weakReference = wk;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = weakReference.get();
            if (activity == null) {
                return;
            }
            switch (msg.what) {
                case GET_RESULT:
                    Log.d(TAG, "handleMessage: " + msg.obj);
                    try {
                        JSONObject jsonObject = new JSONObject(msg.obj.toString());
                        if (jsonObject.getInt("error_code") == 0) {
                            Toast.makeText(activity, jsonObject.getString("reason"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

}
