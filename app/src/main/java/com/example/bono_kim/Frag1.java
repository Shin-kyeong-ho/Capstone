package com.example.bono_kim;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Frag1 extends Fragment {

    private View view;
    private TextView textView;
    private TextView textView2;
    DBHelper dbHelper;

    SQLiteDatabase db = null;
    Cursor cursor;
    ArrayAdapter adapter;
    Context ct;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag1, container, false);
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat format = new SimpleDateFormat("yyyy / MM / dd ");
        String formatnow = format.format(date);
        textView = view.findViewById(R.id.now);
        textView.setText(formatnow);

        ct = container.getContext();
        ListView listView = (ListView)view.findViewById(R.id.listView);
        dbHelper = new DBHelper(ct, 4);
        db = dbHelper.getWritableDatabase();    // 읽기/쓰기 모드로 데이터베이스를 오픈
        cursor = db.rawQuery("SELECT * FROM (select * from tableName limit -1)", null);
        // startManagingCursor(cursor);    //엑티비티의 생명주기와 커서의 생명주기를 같게 한다.
        textView2 = view.findViewById(R.id.last_crying);
        while (cursor.moveToNext()) {
            textView2.setText("마지막 울음 : " + cursor.getString(0));
        }


        return view;
    }
}