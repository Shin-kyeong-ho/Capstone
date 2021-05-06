package com.example.bono_kim;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterbabyActivity extends AppCompatActivity {

    private EditText et_babyName, et_babyBirth, et_babyBloodType;
    private Button btn_register;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registerbaby);

        // 아이디 값 찾아주기
        et_babyName = findViewById(R.id.et_babyName);
        et_babyBirth = findViewById(R.id.et_babyBirth);
        et_babyBloodType = findViewById(R.id.et_babyBloodType);

        Intent intent = getIntent();
        String userID = intent.getStringExtra("입력한 id");
        String userPass = intent.getStringExtra("입력한 pass");
        String userName = intent.getStringExtra("입력한 name");
        int userAge = intent.getIntExtra("입력한 age",0);
        if (userID.equals(""))
            Toast.makeText(this,"입력한 아이디가 없습니다!",Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this,"입력한 데이터" + userID + " " +userPass + " " + userName + " " + userAge,Toast.LENGTH_SHORT).show();

        btn_register = findViewById(R.id.btn_register);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // EditText에 현재 입력되어있는 값을 get(가져온다)해온다.
                String babyName = et_babyName.getText().toString();
                String babyBirth = et_babyBirth.getText().toString();
                String babyBloodType = et_babyBloodType.getText().toString();

                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if (success) { // 회원등록에 성공한 경우
                                Toast.makeText(getApplicationContext(),"회원 등록에 성공하였습니다.",Toast.LENGTH_SHORT).show();
                                Intent intent2 = new Intent(RegisterbabyActivity.this, LoginActivity.class);
                                startActivity(intent2);

                            } else { // 회원등록에 실패한 경우
                                Toast.makeText(getApplicationContext(),"회원 등록에 실패하였습니다.",Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                };
                // 서버로 Volley를 이용해서 요청을 함.
                RegisterRequest registerRequest = new RegisterRequest(userID,userPass,userName,userAge,babyName,babyBirth,babyBloodType, responseListener);
                RequestQueue queue = Volley.newRequestQueue(RegisterbabyActivity.this);
                queue.add(registerRequest);

            }
        });

    }
}