package com.example.stories_project.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stories_project.MainActivity;
import com.example.stories_project.R;
import com.example.stories_project.model.ApiResponse;
import com.example.stories_project.network.RetrofitClient;
import com.example.stories_project.network.request.VerifyOtpRequest;
import com.example.stories_project.network.response.AccountResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpVerificationActivity extends AppCompatActivity {
    private EditText etOtp;
    private Button btnVerify;
    private TextView tvTimer, tvEmail;
    private CountDownTimer countDownTimer;
    private String email;
    private String source;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        email = getIntent().getStringExtra("email");
        source = getIntent().getStringExtra("source"); // Get the source (e.g., "registration" or "forgot_password")

        etOtp = findViewById(R.id.etOtp);
        btnVerify = findViewById(R.id.btnVerify);
        tvTimer = findViewById(R.id.tvTimer);
        tvEmail = findViewById(R.id.tvEmail);
        tvEmail.setText(email);

        startTimer(300000);

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = etOtp.getText().toString().trim();

                if (otp.isEmpty()) {
                    Toast.makeText(OtpVerificationActivity.this, "Vui lòng nhập mã OTP", Toast.LENGTH_SHORT).show();
                    return;
                }

                VerifyOtpRequest verifyOtpRequest = new VerifyOtpRequest(email, otp);

                Call<ApiResponse<AccountResponse>> call = RetrofitClient.getStoryApiService().verifyOtp(verifyOtpRequest);

                call.enqueue(new Callback<ApiResponse<AccountResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AccountResponse>> call, Response<ApiResponse<AccountResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getMeta().getStatus().equals("SUCCESS")) {
                            countDownTimer.cancel();
                            Toast.makeText(OtpVerificationActivity.this, "Xác minh OTP thành công", Toast.LENGTH_SHORT).show();

                            Intent intent;
                            if ("forgot_password".equals(source)) {
                                intent = new Intent(OtpVerificationActivity.this, ResetPasswordActivity.class);
                                intent.putExtra("email", email);
                            } else {
                                intent = new Intent(OtpVerificationActivity.this, MainActivity.class);
                            }
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(OtpVerificationActivity.this, "Mã OTP không đúng hoặc đã hết hạn", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AccountResponse>> call, Throwable t) {
                        Toast.makeText(OtpVerificationActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void startTimer(long millisInFuture) {
        countDownTimer = new CountDownTimer(millisInFuture, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 1000 / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                tvTimer.setText(String.format("Thời gian còn lại: %02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("Hết thời gian!");
                btnVerify.setEnabled(false);
                Toast.makeText(OtpVerificationActivity.this, "Mã OTP đã hết hạn", Toast.LENGTH_SHORT).show();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}