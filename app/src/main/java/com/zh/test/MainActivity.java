package com.zh.test;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.zh.test.test1.Hex;
import com.zh.test.test1.Identifier;
import com.zh.test.test3.CommonLoadingAnim;
import com.zh.test.test5.DeviceUtil;

import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private CommonLoadingAnim commonLoadingAnim;
    private Button button;

    private ScheduledExecutorService executorService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button=findViewById(R.id.btn_test);
        String pkg=null;
        ArrayList<String> strings = new ArrayList<>();
        if (!TextUtils.isEmpty(pkg)){
            strings.addAll(Arrays.asList(pkg.split(",")));
        }
        Log.d("tag",strings.size()+"");

        executorService=new ScheduledThreadPoolExecutor(1);
        executorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                int i1 = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                Log.d(TAG, "onCreate: "+i1);
            }
        },2,2, TimeUnit.SECONDS);
        commonLoadingAnim=findViewById(R.id.progressbar);
        Random random =new SecureRandom();
//        byte[] value = new byte[3];
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            try {
//                random= SecureRandom.getInstanceStrong();
//            } catch (NoSuchAlgorithmException e) {
//                e.printStackTrace();
//                random=new Random();
//            }
//        }else{
//            random=new Random();
//        }
        for (int i=0;i<500;i++){
//            random.nextBytes(value);
//            String randomValue = Hex.encodeHexString(value);
            String randomValue =random.nextInt(500)+"";
            Log.e("TAG",randomValue);
        }
        Log.d(TAG, "onCreate: isKeyguardSecure"+isKeyguardSecure(this));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        disableKeyguard(MainActivity.this);
                        Log.d(TAG, "onCreate: onclick后"+isKeyguardSecure(MainActivity.this));
                    }
                },5000);
            }
        });
//        Log.e("TAG111", DeviceUtil.getMacAddr());

    }

    public static synchronized void disableKeyguard(Context context) {
        try {
            android.app.KeyguardManager keyguardManager = (android.app.KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            android.app.KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("123456");
            keyguardLock.disableKeyguard();
            keyguardLock.reenableKeyguard();
            Log.i(TAG, "disableKeyguard已经执行");
        } catch (Throwable e) {
            Log.e(TAG,"disableKeyguard", e);
        }
    }

    /**
     * 判断是否存在锁屏密码
     *
     * @return ret
     * @author hexuan
     * @Time2016-8-3
     */
    public static boolean isKeyguardSecure(Context context) {
        boolean ret = true;
        try {
            android.app.KeyguardManager keyguardManager = (android.app.KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                ret = keyguardManager.isKeyguardSecure();
            }
            return ret;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return ret;
    }

}
