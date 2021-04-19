package com.example.jautograder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.os.Message;

import android.content.Intent;
import android.provider.Settings;
import android.content.Context;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import org.jetbrains.annotations.Nullable;

import java.util.List;


public class FirstFragment extends Fragment {
    int normalScore = 50;
    public int a, b, c, d, e, action;
    private TextView left, right, noop, acce, dece, gpstv;
    private ImageView bestAction, logo;
    private ProgressBar leftBar, rightBar, noopBar, acceBar, deceBar;
    private LocationManager lm;
    public double longitude, latitude, bearing, speed;
    public double longitude_last = 0, latitude_last = 0, bearing_last = 0, speed_last = 0;
    public String provider;
    public Client socketClient = new Client();
    public Timer timer = new Timer();
    public int scores0, scores1, scores2, scores3, scores4;
    public Bitmap bitmap;
    public Boolean isMove = Boolean.FALSE;

    // 利用Handler来发送消息和处理消息，更改UI上的内容
    Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            // TODO: 输入分数值和最优策略
            if (scores0 == normalScore && scores1 == normalScore && scores2 == normalScore && scores3 == normalScore && scores4 == normalScore) {
                a = normalScore;
                b = normalScore;
                c = normalScore;
                d = normalScore;
                e = normalScore;
            } else {
                a = scores0;
                b = scores1;
                c = scores2;
                d = scores3;
                e = scores4;
            }

            if (a > b) {
                if (a > c) {
                    if (a > d) {
                        if (a > e) action = 0;
                        else action = 4;
                    } else if (d > e) action = 3;
                    else action = 4;
                } else if (c > d) {
                    if (c > e) action = 2;
                    else action = 4;
                } else action = 3;
            } else if (b > c) {
                if (b > d) {
                    if (b > e) action = 1;
                    else action = 4;
                } else if (d > e) action = 3;
                else action = 4;
            } else if (c > d) {
                if (c > e) action = 2;
                else action = 4;
            } else action = 3;

            left.setText(String.valueOf(a));
            right.setText(String.valueOf(b));
            noop.setText(String.valueOf(c));
            acce.setText(String.valueOf(d));
            dece.setText(String.valueOf(e));

            leftBar.setProgress(a);
            rightBar.setProgress(b);
            noopBar.setProgress(c);
            acceBar.setProgress(d);
            deceBar.setProgress(e);

            switch (action) {
                case 0:
                    bestAction.setImageResource(R.drawable.left);
                    break;
                case 1:
                    bestAction.setImageResource(R.drawable.right);
                    break;
                case 2:
                    bestAction.setImageResource(R.drawable.noop);
                    break;
                case 3:
                    bestAction.setImageResource(R.drawable.acce);
                    break;
                case 4:
                    bestAction.setImageResource(R.drawable.dece);
                    break;
                default:
                    break;
            }

            logo.setImageResource(R.drawable.logo_white);
            super.handleMessage(msg);
        }
    };

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        scores0 = normalScore;
        scores1 = normalScore;
        scores2 = normalScore;
        scores3 = normalScore;
        scores4 = normalScore;

        left = getActivity().findViewById(R.id.left);
        right = getActivity().findViewById(R.id.right);
        noop = getActivity().findViewById(R.id.noop);
        acce = getActivity().findViewById(R.id.acce);
        dece = getActivity().findViewById(R.id.dece);
        bestAction = getActivity().findViewById(R.id.bestAction);
        logo = getActivity().findViewById(R.id.imageView2);

        leftBar = getActivity().findViewById(R.id.leftBar);
        rightBar = getActivity().findViewById(R.id.rightBar);
        noopBar = getActivity().findViewById(R.id.noopBar);
        acceBar = getActivity().findViewById(R.id.acceBar);
        deceBar = getActivity().findViewById(R.id.deceBar);

        gpstv = getActivity().findViewById(R.id.gpstv);
        lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            openGPS2();
        }

        List <String> providerList = lm.getProviders(true);
        if (providerList.contains(LocationManager.NETWORK_PROVIDER)) { // 网络提供器
            provider = LocationManager.NETWORK_PROVIDER;
        } else if (providerList.contains(LocationManager.GPS_PROVIDER)) { // GPS提供器
            provider = LocationManager.GPS_PROVIDER;
        } else {
            provider = "null";
        }

        // 从GPS获取最近的定位信息
        Location lc = lm.getLastKnownLocation(provider);
        try {
            updateShow(lc);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        // 设置间隔获得一次GPS定位信息
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 8, new LocationListener() {
            @Override
            // 当GPS定位信息发生改变时，更新定位
            public void onLocationChanged(Location location) {
                try {
                    updateShow(location);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }

            @Override
            // 当GPS LocationProvider可用时，更新定位
            public void onProviderEnabled(String provider) {
                try {
                    updateShow(lm.getLastKnownLocation(provider));
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }

            @Override
            public void onProviderDisabled(String provider) {
                try {
                    updateShow(null);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });

        // 定时器
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // 使用handler发送消息
                Message message = new Message();
                handler.sendMessage(message);
            }
        },0,100); // 每100ms执行一次

        // First Fragment中的按钮
        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

        // 启动线程
        new Thread()
        {
            @Override
            public void run() {
                super.run();
                try {
                    int i = 0;
                    while (true) {
                        // 创建Socket
                        socketClient.socketClient = new Socket("8.140.109.205" , 8383);
                        System.out.println("网络连通成功");

                        // 时间网络等待
                        socketClient.socketClient.setSoTimeout(500000);
                        socketClient.fromServer = new DataInputStream(socketClient.socketClient.getInputStream());
                        socketClient.toServer = new DataOutputStream(socketClient.socketClient.getOutputStream());

                        socketClient.toServer.writeUTF(" " + socketClient.strClient);
                        System.out.println("Send to Server Successfully! " + socketClient.strClient);
                        sleep(2000); // TODO: time adjust
                        socketClient.strServer = socketClient.fromServer.readLine();
                        System.out.println("Recv from Server Successfully!");

                        System.out.println("Server: " + socketClient.strServer);
                        String[] scoresText = socketClient.strServer.split(" ");

                        scores0 = Integer.parseInt(scoresText[0]);
                        scores1 = Integer.parseInt(scoresText[1]);
                        scores2 = Integer.parseInt(scoresText[2]);
                        scores3 = Integer.parseInt(scoresText[3]);
                        scores4 = Integer.parseInt(scoresText[4]);

                        // TODO: 获取服务器的URL
                        bitmap = getBitmap("http://8.140.109.205:82/car.jpg");

                        //关闭连接
                        socketClient.socketClient.close();
                        System.out.println(i++);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("网络连通失败");
                }
            }
        }.start();
    }

    // 打开位置信息设置页面让用户自己设置
    private void openGPS2(){
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(intent,0);
    }

    // 定义一个更新显示的方法
    private void updateShow(Location location) throws Exception {
        if (location != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("当前的位置信息\n");
            sb.append("经度：" + String.format("%.6f", location.getLongitude()) + "\n");
            sb.append("纬度：" + String.format("%.6f", location.getLatitude()) + "\n");
            sb.append("速度：" + String.format("%.2f", location.getSpeed()) + "\n");
            sb.append("方向：" + String.format("%.2f", location.getBearing()) + "\n");
            sb.append("精度：" + String.format("%.2f", location.getAccuracy()) + "\n");
            gpstv.setText(sb.toString());

            longitude = location.getLongitude();
            latitude = location.getLatitude();
            bearing = location.getBearing();
            speed = location.getSpeed();
            System.out.println("Position Refreshed!");

            if (longitude != longitude_last || latitude != latitude_last || bearing != bearing_last || speed != speed_last) {
                socketClient.input(longitude, latitude, bearing, speed);
                longitude_last = longitude;
                latitude_last = latitude;
                bearing_last = bearing;
                speed_last = speed;
                isMove = Boolean.TRUE;
            }

        } else gpstv.setText("");
    }

    @Nullable
    public static Bitmap getBitmap (String path) throws IOException {
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() == 200) {
            InputStream inputStream = conn.getInputStream();
            return BitmapFactory.decodeStream(inputStream);
        }
        return null;
    }
}
