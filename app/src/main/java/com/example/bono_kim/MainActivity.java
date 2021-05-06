package com.example.bono_kim;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> arr = new ArrayList<String>();
    TextView recieveText;
    Button button ;
    Button update_btn;
    Socket socket = null;

    private TextView tv_id, tv_pass;
    static int counter = 0;

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fm;
    private FragmentTransaction ft;
    private Frag1 frag1;
    private Frag2 frag2;
    private Frag3 frag3;
    public Frag4 frag4;
    private Frag5 frag5;

    ListView listView;

    DBHelper dbHelper;
    SQLiteDatabase db = null;
    Cursor cursor;
    ArrayAdapter adapter;


    @Override
    public void onBackPressed() {
        AlertDialog.Builder alBuilder = new AlertDialog.Builder(this);
        alBuilder.setMessage("로그아웃하고 종료하시겠습니까?");

        alBuilder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ActivityCompat.finishAffinity(MainActivity.this);
                System.exit(0);
            }
        });

        alBuilder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                return;
            }
        });
        alBuilder.setTitle("프로그램 종료");
        alBuilder.show();


    }//뒤로가기 했을때 로그인 화면이 아니라 완전히 나가지도록

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        listView = findViewById(R.id.listView);
        dbHelper = new DBHelper(this, 4);
        db = dbHelper.getWritableDatabase();    // 읽기/쓰기 모드로 데이터베이스를 오픈

        tv_id = findViewById(R.id.tv_id);
        tv_pass = findViewById(R.id.tv_pass);
        bottomNavigationView = findViewById(R.id.bottomNavi);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.action_ourbaby:
                        setFrag(0);
                        break;
                    case R.id.action_babyinfo:
                        setFrag(1);
                        break;
                    case R.id.action_babylog:
                        setFrag(2);
                        break;
                    case R.id.action_babycrylog:
                        setFrag(3);
                        break;
                    case R.id.action_how:
                        setFrag(4);
                        break;
                }
                return true;
            }
        });
        frag1 = new Frag1();
        frag2 = new Frag2();
        frag3 = new Frag3();
        frag4 = new Frag4();
        frag5 = new Frag5();
        setFrag(0);//첫 프래그먼트 화면 무엇으로 할지


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
        connet();
        recieveText = (TextView) findViewById(R.id.reciveText);
    }

    public void connet(){
        //messageText.setText("");
        TimerTask tt = new TimerTask() { // 타이머를 줘서 연속적인 소켓통신을 함으로써 버튼변수 대기
            @Override
            public void run() {
                MyClientTask myClientTask = new MyClientTask("192.168.0.2", // 라즈베리파이의 ip주소로 8091포트에 ...이라는 텍스트를 보냅니다.
                        Integer.parseInt("8091"), "send");
                myClientTask.execute(); // 처음 버튼 클릭했을시에 소켓통신에 연결
            }
        };
        Timer timer = new Timer(); // 타이머 시작
        timer.schedule(tt,0,200); // 타아머의 속도는 0.2초로 설정(버튼이 눌리는 시간)
    }
    public void listUpdate() {
        listView = findViewById(R.id.listView);
        dbHelper = new DBHelper(this, 4);
        db = dbHelper.getWritableDatabase();    // 읽기/쓰기 모드로 데이터베이스를 오픈
        cursor = db.rawQuery("SELECT * FROM tableName", null);
        startManagingCursor(cursor);    //엑티비티의 생명주기와 커서의 생명주기를 같게 한다.
        adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1);
        while (cursor.moveToNext()) {
            adapter.add(cursor.getString(0));
        }
        /*cursor.moveToFirst();
        cursor.moveToPrevious();
        cursor.moveToPosition(2);*/
        listView.setAdapter(adapter);
        //db.close();
        // listView2.setAdapter(adapter2);
    }


    public void insert() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formatnow = format.format(date);
        String info = "333";
        //db.execSQL("INSERT INTO tableName VALUES ('" +  formatnow + "');");
        db.execSQL("INSERT INTO tableName VALUES ('" + formatnow + "', '" + info + "');");
        //db.close();
        //db.execSQL("INSERT INTO tableName VALUES ('" +  formatnow + "'");
        //String sql = String.format("INSERT INTO tableName VALUES('%s');",formatnow);
        //db.execSQL(sql);
        Toast.makeText(getApplicationContext(), "추가 성공", Toast.LENGTH_SHORT).show();
    }


    //프래그먼트 교체 일어나는곳
    private void setFrag(int n){
        fm =getSupportFragmentManager();
        ft = fm.beginTransaction();
        switch(n) {
            case 0:
                ft.replace(R.id.main_frame, frag1);
                ft.commit();
                break;
            case 1:
                ft.replace(R.id.main_frame, frag2);
                ft.commit();
                break;
            case 2:
                ft.replace(R.id.main_frame, frag3);
                ft.commit();
                break;
            case 3:
                ft.replace(R.id.main_frame, frag4);
                ft.commit();
                break;
            case 4:
                ft.replace(R.id.main_frame, frag5);
                ft.commit();
                break;
        }
    }

//    private void cringlog(){
//        ListView listView;
//        listView = findViewById(R.id.listview);
//        long now = System.currentTimeMillis();
//        Date date = new Date(now);
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String formatnow = format.format(date);
//        arr.add(formatnow);
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,arr);
//        listView.setAdapter(adapter);
//    }

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
                    //cringlog();
                    insert();
                    listUpdate();
                }
            }
        }
    }
}