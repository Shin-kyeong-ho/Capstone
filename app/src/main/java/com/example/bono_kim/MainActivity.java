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
import android.widget.EditText;
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
    Button btn_save;
    EditText edit_memo;
    String memo;
    private TextView tv_id, tv_pass;
    static int counter = 0;
    Button btn_month1; //????????????

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
    DBHelper_memo dbHelper_memo;
    SQLiteDatabase db = null;
    SQLiteDatabase db_memo = null;
    Cursor cursor;
    ArrayAdapter adapter;


    @Override
    public void onBackPressed() {
        AlertDialog.Builder alBuilder = new AlertDialog.Builder(this);
        alBuilder.setMessage("?????????????????? ?????????????????????????");

        alBuilder.setPositiveButton("???", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ActivityCompat.finishAffinity(MainActivity.this);
                System.exit(0);
            }
        });

        alBuilder.setNegativeButton("?????????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                return;
            }
        });
        alBuilder.setTitle("???????????? ??????");
        alBuilder.show();


    }//???????????? ????????? ????????? ????????? ????????? ????????? ???????????????

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        listView = findViewById(R.id.listView);
        dbHelper = new DBHelper(this, 4);
        db = dbHelper.getWritableDatabase();    // ??????/?????? ????????? ????????????????????? ??????

        dbHelper_memo = new DBHelper_memo(this, 4);
        db_memo = dbHelper_memo.getWritableDatabase();    // ??????/?????? ????????? ????????????????????? ??????

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
                }
                return true;
            }
        });
        frag1 = new Frag1();
        frag2 = new Frag2();
        frag3 = new Frag3();
        frag4 = new Frag4();
        frag5 = new Frag5();
        setFrag(0);//??? ??????????????? ?????? ???????????? ??????


        Intent intent = getIntent();

        Dexter.withContext(this).withPermission(Manifest.permission.CAMERA).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                runFlashlight(0);
            }
            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
            }
            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
            }
        }).check();
        connet();
    }

    public void connet(){
        //messageText.setText("");
        TimerTask tt = new TimerTask() { // ???????????? ?????? ???????????? ??????????????? ???????????? ???????????? ??????
            @Override
            public void run() {
                MyClientTask myClientTask = new MyClientTask("192.168.0.3", // ????????????????????? ip????????? 8091????????? ...????????? ???????????? ????????????.
                        Integer.parseInt("8091"), "send");
                myClientTask.execute(); // ?????? ?????? ?????????????????? ??????????????? ??????
            }
        };
        Timer timer = new Timer(); // ????????? ??????
        timer.schedule(tt,0,200); // ???????????? ????????? 0.2?????? ??????(????????? ????????? ??????)
    }

    public void listUpdate() {
        listView = findViewById(R.id.listView);
        dbHelper = new DBHelper(this, 4);
        db = dbHelper.getWritableDatabase();    // ??????/?????? ????????? ????????????????????? ??????
        cursor = db.rawQuery("SELECT * FROM tableName", null);
        startManagingCursor(cursor);    //??????????????? ??????????????? ????????? ??????????????? ?????? ??????.
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
        Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
    }
    public void month1(View v) {
        Toast.makeText(getApplicationContext(), "??????", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this,month1.class);
        startActivity(intent);
    } // ???????????? ??????

    public void month2(View v) {
        Toast.makeText(getApplicationContext(), "??????", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this,month2.class);
        startActivity(intent);
    } // ???????????? ??????

    public void month3(View v) {
        Toast.makeText(getApplicationContext(), "??????", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this,month3.class);
        startActivity(intent);
    } // ???????????? ??????

    public void memo(View v) {
        Toast.makeText(getApplicationContext(), "??????", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this,Memo_insert.class);
        startActivity(intent);
    } // ???????????? ??????

    public void save_memo(View v) {

        EditText edit_memo= (EditText)findViewById(R.id.edit_memo);
        memo= edit_memo.getText().toString();
        //db.execSQL("INSERT INTO tableName VALUES ('" +  formatnow + "');");
        db_memo.execSQL("INSERT INTO memo VALUES ('" + memo +"');");
        listmemoUpdate();
        //db.close();
        //db.execSQL("INSERT INTO tableName VALUES ('" +  formatnow + "'");
        //String sql = String.format("INSERT INTO memo VALUES('%s');",memo);
        //db_memo.execSQL(sql);
        Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();

    }


    public void listmemoUpdate() {
        ListView listView_memo = findViewById(R.id.listView_memo);
        dbHelper_memo = new DBHelper_memo(this, 4);
        db_memo = dbHelper_memo.getWritableDatabase();    // ??????/?????? ????????? ????????????????????? ??????
        Cursor cursor_memo = db_memo.rawQuery("SELECT * FROM memo", null);
        startManagingCursor(cursor_memo);    //??????????????? ??????????????? ????????? ??????????????? ?????? ??????.
        ArrayAdapter adapter_memo = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1);
        while (cursor_memo.moveToNext()) {
            adapter_memo.add(cursor_memo.getString(0));
        }
        /*cursor.moveToFirst();
        cursor.moveToPrevious();
        cursor.moveToPosition(2);*/
        listView_memo.setAdapter(adapter_memo);
        //db.close();

    }

    public void memo_insert(View v) {
        EditText edit_memo= (EditText)findViewById(R.id.edit_memo);
        memo= edit_memo.getText().toString();
        //db.execSQL("INSERT INTO tableName VALUES ('" +  formatnow + "');");
        db_memo.execSQL("INSERT INTO memo VALUES ('" + memo +"');");
        //db.close();
        //db.execSQL("INSERT INTO tableName VALUES ('" +  formatnow + "'");
        //String sql = String.format("INSERT INTO memo VALUES('%s');",memo);
        //db_memo.execSQL(sql);
        Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
    }

    //??????????????? ?????? ???????????????
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

    //????????? ??????
    @SuppressLint("NewApi")
    private void runFlashlight(int kh_flash){
        if(kh_flash == 1) {
            CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            try {
                String cameraId = cameraManager.getCameraIdList()[0];
                for (int i = 0; i < 3; i++) {
                    cameraManager.setTorchMode(cameraId, true);
                    Thread.sleep(250);
                    cameraManager.setTorchMode(cameraId, false);
                    Thread.sleep(250);
                }
            } catch (CameraAccessException | InterruptedException e) {
            }
        } else{};
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
                //??????
                OutputStream out = socket.getOutputStream();
                out.write("send".getBytes());

                //??????
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
            //recieveText.setText(response);
            super.onPostExecute(result);
            if (response.contains("baby crying")) { //?????????????????? push?????? ?????? ?????????????????? ????????? ??????
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//????????? ?????? ??????????????? ????????? ??????????????? ????????? ????????????
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    String Noti_Channel_ID = "Noti";
                    String Noti_Channel_Group_ID = "Noti_Group";
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    NotificationChannel notificationChannel = new NotificationChannel(Noti_Channel_ID, Noti_Channel_Group_ID, importance);
                    if (notificationManager.getNotificationChannel(Noti_Channel_ID) != null) {
                    } else {                                                                 //????????? ???????????? ????????? ??????????????????.
                        notificationManager.createNotificationChannel(notificationChannel);
                    }
                    notificationManager.createNotificationChannel(notificationChannel);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), Noti_Channel_ID) //?????? ?????? ????????????
                            .setLargeIcon(null).setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setWhen(System.currentTimeMillis()).setShowWhen(true)
                            .setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_MAX)
                            .setContentTitle("????????? ?????????1")
                            .setDefaults(Notification.DEFAULT_VIBRATE)
                            .setDefaults(Notification.DEFAULT_SOUND)
                            .setContentText("????????? ?????????2");
                    notificationManager.notify(0, builder.build()); // ?????? ????????????
                    runFlashlight(1);
                    insert();
                    listUpdate();
                }
            }
        }
    }
}