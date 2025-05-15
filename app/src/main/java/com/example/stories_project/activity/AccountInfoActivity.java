package com.example.stories_project.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.stories_project.MainActivity;
import com.example.stories_project.R;
import com.example.stories_project.model.ApiResponse;
import com.example.stories_project.network.RetrofitClient;
import com.example.stories_project.network.response.UserResponse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity to display user account information and menu options.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class AccountInfoActivity extends AppCompatActivity {

    private static final String PREF_NAME = "UserPrefs";
    private static final int REQUEST_CODE_EDIT = 100; // Request code for EditAccountInfoActivity
    private TextView fullNameTextView, phoneNumberTextView, dateOfBirthTextView, genderTextView, tvEdit;
    private ProgressBar progressBar;
    private ListView menuList;
    private String username;

    private static final DateTimeFormatter API_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final String[] menuItems = {
            "Danh sách truyện yêu thích",
            "Lịch sử đọc truyện",
            "Đổi mật khẩu",
            "Thông tin ứng dụng",
            "Đăng xuất"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_info);

        fullNameTextView = findViewById(R.id.fullName);
        phoneNumberTextView = findViewById(R.id.phoneNumber);
        dateOfBirthTextView = findViewById(R.id.dateOfBirth);
        genderTextView = findViewById(R.id.gender);
        tvEdit = findViewById(R.id.tvEdit);
        progressBar = findViewById(R.id.progressBar);
        menuList = findViewById(R.id.menuList);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, menuItems);
        menuList.setAdapter(adapter);

        menuList.setOnItemClickListener((parent, view, position, id) -> {
            switch (position) {
                case 0: // Danh sách truyện yêu thích
                    startActivity(new Intent(AccountInfoActivity.this, FavoritesActivity.class));
                    break;
                case 1: // Lịch sử đọc truyện
                    startActivity(new Intent(AccountInfoActivity.this, ReadingHistoryActivity.class));
                    break;
                case 2: // Đổi mật khẩu
                    startActivity(new Intent(AccountInfoActivity.this, ChangePasswordActivity.class));
                    break;
                case 3: // Thông tin ứng dụng
                    startActivity(new Intent(AccountInfoActivity.this, AppInfoActivity.class));
                    break;
                case 4: // Đăng xuất
                    logout();
                    break;
            }
        });

        tvEdit.setOnClickListener(v -> {
            Intent intent = new Intent(AccountInfoActivity.this, EditAccountInfoActivity.class);
            intent.putExtra("username", username);
            startActivityForResult(intent, REQUEST_CODE_EDIT);
        });

        // Lấy username từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        username = prefs.getString("username", null);

        if (username == null) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        fetchUserInfo(username);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT && resultCode == RESULT_OK) {
            fetchUserInfo(username);
        }
    }

    private void fetchUserInfo(String username) {
        progressBar.setVisibility(View.VISIBLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            RetrofitClient.getStoryApiService().getUserByUsername(username).enqueue(new Callback<ApiResponse<UserResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                    progressBar.setVisibility(View.GONE);

                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<UserResponse> apiResponse = response.body();
                        if ("SUCCESS".equals(apiResponse.getMeta().getStatus())) {
                            UserResponse user = apiResponse.getData();
                            if (user != null) {
                                // Hiển thị thông tin người dùng
                                fullNameTextView.setText(user.fullName());
                                phoneNumberTextView.setText(user.phoneNumber());

                                try {
                                    LocalDate dateOfBirth = LocalDate.parse(user.dateOfBirth(), API_DATE_FORMATTER);
                                    dateOfBirthTextView.setText(dateOfBirth.format(DISPLAY_DATE_FORMATTER));
                                } catch (DateTimeParseException e) {
                                    dateOfBirthTextView.setText(user.dateOfBirth());
                                    Toast.makeText(AccountInfoActivity.this, "Lỗi định dạng ngày sinh", Toast.LENGTH_SHORT).show();
                                }

                                String genderDisplay = convertGenderToVietnamese(user.gender());
                                genderTextView.setText(genderDisplay);
                            } else {
                                Toast.makeText(AccountInfoActivity.this, "Không có dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(AccountInfoActivity.this, apiResponse.getMeta().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(AccountInfoActivity.this, "Lỗi server: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AccountInfoActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(AccountInfoActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
    }

    private String convertGenderToVietnamese(String gender) {
        if ("MALE".equalsIgnoreCase(gender)) {
            return "Nam";
        } else if ("FEMALE".equalsIgnoreCase(gender)) {
            return "Nữ";
        } else if ("OTHER".equalsIgnoreCase(gender)) {
            return "Khác";
        } else {
            return gender;
        }
    }
}