package com.example.stories_project.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.stories_project.R;
import com.example.stories_project.model.ApiResponse;
import com.example.stories_project.network.RetrofitClient;
import com.example.stories_project.network.request.ResetPasswordRequest;
import com.example.stories_project.network.response.AccountResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity to change user password.
 */
public class ChangePasswordActivity extends AppCompatActivity {

    private static final String PREF_NAME = "UserPrefs";
    private TextInputEditText newPasswordEditText, confirmPasswordEditText;
    private TextInputLayout newPasswordLayout, confirmPasswordLayout;
    private ProgressBar progressBar;
    private Button changePasswordButton, cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);

        // Khởi tạo các view
        newPasswordLayout = findViewById(R.id.newPasswordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        newPasswordEditText = findViewById(R.id.newPassword);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        progressBar = findViewById(R.id.progressBar);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        cancelButton = findViewById(R.id.cancelButton);

        // Xử lý window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Lấy email từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String savedEmail = prefs.getString("email", null);

        // Xử lý nút Đổi mật khẩu
        changePasswordButton.setOnClickListener(v -> changePassword(savedEmail));

        // Xử lý nút Hủy
        cancelButton.setOnClickListener(v -> finish());
    }

    private void changePassword(String email) {
        newPasswordLayout.setError(null);
        confirmPasswordLayout.setError(null);

        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (newPassword.isEmpty()) {
            newPasswordLayout.setError("Vui lòng nhập mật khẩu mới");
            return;
        }
        if (newPassword.length() < 8) {
            newPasswordLayout.setError("Mật khẩu phải có ít nhất 8 ký tự");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Mật khẩu xác nhận không khớp");
            return;
        }

        ResetPasswordRequest request = new ResetPasswordRequest(email, newPassword);
        progressBar.setVisibility(View.VISIBLE);

        RetrofitClient.getStoryApiService().resetPassword(request).enqueue(new Callback<ApiResponse<AccountResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AccountResponse>> call, Response<ApiResponse<AccountResponse>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && "SUCCESS".equals(response.body().getMeta().getStatus())) {
                    showSuccessDialog();
                } else {
                    String errorMessage = response.body() != null ? response.body().getMeta().getMessage() : response.message();
                    Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thất bại: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AccountResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ChangePasswordActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Thành công")
                .setMessage("Mật khẩu đã được đổi thành công.")
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}