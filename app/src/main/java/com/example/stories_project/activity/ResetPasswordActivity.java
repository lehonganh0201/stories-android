package com.example.stories_project.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stories_project.MainActivity;
import com.example.stories_project.R;
import com.example.stories_project.network.RetrofitClient;
import com.example.stories_project.network.request.ResetPasswordRequest;
import com.example.stories_project.model.ApiResponse;
import com.example.stories_project.network.response.AccountResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {
    private EditText etNewPassword, etConfirmPassword;
    private ImageView ivTogglePassword, ivToggleConfirmPassword;
    private Button btnReset;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        ivToggleConfirmPassword = findViewById(R.id.ivToggleConfirmPassword);
        btnReset = findViewById(R.id.btnReset);

        email = getIntent().getStringExtra("email");

        ivTogglePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPasswordVisible = !isPasswordVisible;
                if (isPasswordVisible) {
                    etNewPassword.setTransformationMethod(null);
                    ivTogglePassword.setImageResource(R.drawable.ic_eye_off);
                } else {
                    etNewPassword.setTransformationMethod(new PasswordTransformationMethod());
                    ivTogglePassword.setImageResource(R.drawable.ic_eye_on);
                }
                etNewPassword.setSelection(etNewPassword.getText().length());
            }
        });

        ivToggleConfirmPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isConfirmPasswordVisible = !isConfirmPasswordVisible;
                if (isConfirmPasswordVisible) {
                    etConfirmPassword.setTransformationMethod(null);
                    ivToggleConfirmPassword.setImageResource(R.drawable.ic_eye_off);
                } else {
                    etConfirmPassword.setTransformationMethod(new PasswordTransformationMethod());
                    ivToggleConfirmPassword.setImageResource(R.drawable.ic_eye_on);
                }
                etConfirmPassword.setSelection(etConfirmPassword.getText().length());
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPassword = etNewPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();

                if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(ResetPasswordActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(ResetPasswordActivity.this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                    return;
                }

                ResetPasswordRequest request = new ResetPasswordRequest(email, newPassword);
                Call<ApiResponse<AccountResponse>> call = RetrofitClient.getStoryApiService().resetPassword(request);

                call.enqueue(new Callback<ApiResponse<AccountResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AccountResponse>> call, Response<ApiResponse<AccountResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<AccountResponse> apiResponse = response.body();
                            if ("SUCCESS".equals(apiResponse.getMeta().getStatus())) {
                                Toast.makeText(ResetPasswordActivity.this, apiResponse.getMeta().getMessage(), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ResetPasswordActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(ResetPasswordActivity.this, apiResponse.getMeta().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ResetPasswordActivity.this, "Đặt lại mật khẩu thất bại. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AccountResponse>> call, Throwable t) {
                        Toast.makeText(ResetPasswordActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}