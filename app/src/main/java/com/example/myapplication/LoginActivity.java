package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        dbHelper = new DatabaseHelper(this);
        EditText etUser = findViewById(R.id.et_username);
        EditText etPass = findViewById(R.id.et_password);
        Button btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(v -> {//点击登陆后判断
            String u = etUser.getText().toString();
            String p = etPass.getText().toString();
            if (u.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, "请输入账号密码", Toast.LENGTH_SHORT).show();//用土司提示窗口提示
                return;
            }

            if (dbHelper.loginOrRegister(u, p)) {//使用loginOrRegister方法判断是注册还是登录
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("USER", u);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show();
            }
        });
    }
}