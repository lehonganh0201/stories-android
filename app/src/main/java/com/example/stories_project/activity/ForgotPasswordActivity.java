package com.example.stories_project.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stories_project.R;
import com.example.stories_project.network.RetrofitClient;
import com.example.stories_project.network.request.ForgotPasswordRequest;
import com.example.stories_project.model.ApiResponse;
import com.example.stories_project.network.response.AccountResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText etEmail;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.etEmail);
        btnSubmit = findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();

                if (email.isEmpty()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                    return;
                }

                ForgotPasswordRequest request = new ForgotPasswordRequest(email);
                Call<ApiResponse<AccountResponse>> call = RetrofitClient.getStoryApiService().forgotPassword(request);

                call.enqueue(new Callback<ApiResponse<AccountResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AccountResponse>> call, Response<ApiResponse<AccountResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<AccountResponse> apiResponse = response.body();
                            if ("SUCCESS".equals(apiResponse.getMeta().getStatus())) {
                                Toast.makeText(ForgotPasswordActivity.this, apiResponse.getMeta().getMessage(), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ForgotPasswordActivity.this, OtpVerificationActivity.class);
                                intent.putExtra("email", email);
                                intent.putExtra("source", "forgot_password");
                                startActivity(intent);
                            } else {
                                Toast.makeText(ForgotPasswordActivity.this, apiResponse.getMeta().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ForgotPasswordActivity.this, "Yêu cầu thất bại. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AccountResponse>> call, Throwable t) {
                        Toast.makeText(ForgotPasswordActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}