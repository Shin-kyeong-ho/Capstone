package com.example.bono_kim;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import android.content.Intent;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    TextView recieveText;
    Button button ;
    Socket socket = null;
    private TextView tv_id, tv_pass;
    static int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_id = findViewById(R.id.tv_id);
        tv_pass = findViewById(R.id.tv_pass);


        Intent intent = getIntent();
        String userID = intent.getStringExtra("userID");
        String userPass = intent.getStringExtra("userPass");

        tv_id.setText(userID);
        tv_pass.setText(userPass);
        Dexter.withContext(this).withPermission(Manifest.permission.CAMERA).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                //runFlashlight();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

            }
        }).check();
        recieveText = (TextView) findViewById(R.id.reciveText);
        button = (Button)findViewById((R.id.button));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //messageText.setText("");
                TimerTask tt = new TimerTask() { // 타이머를 줘서 연속적인 소켓통신을 함으로써 버튼변수 대기
                    @Override
                    public void run() {
                        MyClientTask myClientTask = new MyClientTask("192.168.0.5", // 라즈베리파이의 ip주소로 8091포트에 ...이라는 텍스트를 보냅니다.
                                Integer.parseInt("8091"), "send");
                        myClientTask.execute(); // 처음 버튼 클릭했을시에 소켓통신에 연결
                    }
                };
                Timer timer = new Timer(); // 타이머 시작
                timer.schedule(tt,0,200); // 타아머의 속도는 0.2초로 설정(버튼이 눌리는 시간)

            }
        });
    }
    //프래시 생성
    @SuppressLint("NewApi")
    private void runFlashlight(){
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            for(int i=0;i<3;i++) {
                cameraManager.setTorchMode(cameraId, true);
                Thread.sleep(250);
                cameraManager.setTorchMode(cameraId, false);
                Thread.sleep(250);
            }
        }
        catch (CameraAccessException | InterruptedException e)
        {}

    }

    public class MyClientTask extends AsyncTask<Void, Void, Void> {
        String dstAddress;
        int dstPort;
        String response = "";
        String myMessage = "";

        //constructor
        MyClientTask(String addr, int port, String message) {
            dstAddress = addr;
            dstPort = port;
            myMessage = message;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            Socket socket = null;
            myMessage = myMessage.toString();
            try {
                socket = new Socket(dstAddress, dstPort);
                //송신
                OutputStream out = socket.getOutputStream();
                out.write("send".getBytes());

                //수신
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];
                int bytesRead;
                InputStream inputStream = socket.getInputStream();
                /*
                 * notice:
                 * inputStream.read() will block if no data return
                 */
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    response += byteArrayOutputStream.toString("UTF-8");
                }
                response = "sever:" + response;

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "IOException: " + e.toString();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            recieveText.setText(response);
            super.onPostExecute(result);
            if (recieveText.getText().toString().contains("baby crying")) { //소캣서버에서 push라는 값이 들어올경우에 이벤트 생성
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//오레오 이상 버전에서는 채널을 만들어줘야 알림이 생성가능
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    String Noti_Channel_ID = "Noti";
                    String Noti_Channel_Group_ID = "Noti_Group";
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    NotificationChannel notificationChannel = new NotificationChannel(Noti_Channel_ID, Noti_Channel_Group_ID, importance);
                    if (notificationManager.getNotificationChannel(Noti_Channel_ID) != null) {
                    } else {                                                                 //채널이 없을시에 채널을 생성해줍니다.
                        notificationManager.createNotificationChannel(notificationChannel);
                    }
                    notificationManager.createNotificationChannel(notificationChannel);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), Noti_Channel_ID) //알림 설정 하는부분
                            .setLargeIcon(null).setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setWhen(System.currentTimeMillis()).setShowWhen(true)
                            .setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_MAX)
                            .setContentTitle("아기가 울어요1")
                            .setDefaults(Notification.DEFAULT_VIBRATE)
                            .setDefaults(Notification.DEFAULT_SOUND)
                            .setContentText("아기가 울어요2");
                    notificationManager.notify(0, builder.build()); // 알림 생성하기
                    //runFlashlight();
                }
            }
        }
    }
}
