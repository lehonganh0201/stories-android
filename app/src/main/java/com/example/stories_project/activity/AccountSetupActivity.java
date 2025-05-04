package com.example.stories_project.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stories_project.R;
import com.example.stories_project.model.ApiResponse;
import com.example.stories_project.network.request.AccountRequest;
import com.example.stories_project.network.request.RegisterRequest;
import com.example.stories_project.network.request.UserRequest;
import com.example.stories_project.network.response.AccountResponse;
import com.example.stories_project.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountSetupActivity extends AppCompatActivity {
    private EditText etUsername, etPassword, etEmail;
    private Button btnComplete;
    private UserRequest userRequest;
    private TextView tvTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup);

        userRequest = (UserRequest) getIntent().getSerializableExtra("userRequest");

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etEmail = findViewById(R.id.etEmail);
        btnComplete = findViewById(R.id.btnComplete);

        btnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String email = etEmail.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                    Toast.makeText(AccountSetupActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                AccountRequest accountRequest = new AccountRequest(username, password, email);

                RegisterRequest registerRequest = new RegisterRequest(
                        accountRequest,
                        userRequest
                );

                Call<ApiResponse<AccountResponse>> call = RetrofitClient.getStoryApiService().register(registerRequest);

                call.enqueue(new Callback<ApiResponse<AccountResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AccountResponse>> call, Response<ApiResponse<AccountResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getMeta().getStatus().equals("SUCCESS")) {
                            Toast.makeText(AccountSetupActivity.this, "Gửi yêu cầu đăng ký thành công, vui lòng kiểm tra email để lấy mã OTP", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AccountSetupActivity.this, OtpVerificationActivity.class);
                            intent.putExtra("email", email);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(AccountSetupActivity.this, "Đăng ký thất bại. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AccountResponse>> call, Throwable t) {
                        Toast.makeText(AccountSetupActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}