package com.example.bono_kim;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Frag3 extends Fragment {

    private View view;
    DBHelper_memo dbHelper;

    SQLiteDatabase db = null;
    Cursor cursor;
    ArrayAdapter adapter;
    Context ct;
    private String memo;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag3, container, false);
        ct = container.getContext();
        EditText edit_memo= (EditText) view.findViewById(R.id.edit_memo);
        ListView listView = (ListView)view.findViewById(R.id.listView_memo);
        dbHelper = new DBHelper_memo(ct, 4);
        db = dbHelper.getWritableDatabase();    // 읽기/쓰기 모드로 데이터베이스를 오픈
        cursor = db.rawQuery("SELECT * FROM memo", null);
        // startManagingCursor(cursor);    //엑티비티의 생명주기와 커서의 생명주기를 같게 한다.
        adapter = new ArrayAdapter(ct,
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

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.memomenu,menu);


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        int click = item.getItemId();

        switch (item.getItemId()) {
            case R.id.memo_menu:
                Toast.makeText(ct, "메모 입력 메뉴 클릭", Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
