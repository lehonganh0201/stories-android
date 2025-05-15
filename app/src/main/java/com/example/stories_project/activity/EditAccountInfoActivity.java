package com.example.stories_project.activity;

import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.stories_project.R;
import com.example.stories_project.model.ApiResponse;
import com.example.stories_project.network.RetrofitClient;
import com.example.stories_project.network.request.UpdateUserRequest;
import com.example.stories_project.network.response.UserResponse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity to edit user account information.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class EditAccountInfoActivity extends AppCompatActivity {

    private EditText fullNameEditText, phoneNumberEditText, dateOfBirthEditText;
    private Spinner genderSpinner;
    private ProgressBar progressBar;
    private Button saveButton, cancelButton;
    private String username;

    // Định dạng ngày
    private static final DateTimeFormatter API_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Danh sách giới tính
    private static final String[] GENDER_OPTIONS = {"Nam", "Nữ", "Khác"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_account_info);

        // Khởi tạo các view
        fullNameEditText = findViewById(R.id.fullName);
        phoneNumberEditText = findViewById(R.id.phoneNumber);
        dateOfBirthEditText = findViewById(R.id.dateOfBirth);
        genderSpinner = findViewById(R.id.gender);
        progressBar = findViewById(R.id.progressBar);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, GENDER_OPTIONS);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);

        setupDatePicker();

        username = getIntent().getStringExtra("username");
        if (username == null) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        fetchUserInfo(username);

        // Xử lý nút Lưu
        saveButton.setOnClickListener(v -> updateUserInfo());

        // Xử lý nút Hủy
        cancelButton.setOnClickListener(v -> finish());
    }

    private void setupDatePicker() {
        dateOfBirthEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    EditAccountInfoActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                        dateOfBirthEditText.setText(selectedDate);
                    },
                    year, month, day);
            datePickerDialog.show();
        });
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
                                fullNameEditText.setText(user.fullName());
                                phoneNumberEditText.setText(user.phoneNumber());

                                // Chuyển đổi dateOfBirth từ yyyy-MM-dd sang dd/MM/yyyy
                                try {
                                    LocalDate dateOfBirth = LocalDate.parse(user.dateOfBirth(), API_DATE_FORMATTER);
                                    dateOfBirthEditText.setText(dateOfBirth.format(DISPLAY_DATE_FORMATTER));
                                } catch (DateTimeParseException e) {
                                    dateOfBirthEditText.setText(user.dateOfBirth());
                                    Toast.makeText(EditAccountInfoActivity.this, "Lỗi định dạng ngày sinh", Toast.LENGTH_SHORT).show();
                                }

                                // Chuyển đổi gender sang tiếng Việt và chọn trong Spinner
                                String genderDisplay = convertGenderToVietnamese(user.gender());
                                genderSpinner.setSelection(Arrays.asList(GENDER_OPTIONS).indexOf(genderDisplay));
                            } else {
                                Toast.makeText(EditAccountInfoActivity.this, "Không có dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(EditAccountInfoActivity.this, apiResponse.getMeta().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(EditAccountInfoActivity.this, "Lỗi server: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(EditAccountInfoActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateUserInfo() {
        String fullName = fullNameEditText.getText().toString().trim();
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        String dateOfBirth = dateOfBirthEditText.getText().toString().trim();
        String genderDisplay = genderSpinner.getSelectedItem().toString();

        if (fullName.isEmpty() || phoneNumber.isEmpty() || dateOfBirth.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!phoneNumber.matches("\\+?[0-9]{10,12}")) {
            Toast.makeText(this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            LocalDate.parse(dateOfBirth, DISPLAY_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            Toast.makeText(this, "Ngày sinh phải có định dạng dd/MM/yyyy", Toast.LENGTH_SHORT).show();
            return;
        }

        String gender = convertGenderToApiFormat(genderDisplay);

        UpdateUserRequest request = new UpdateUserRequest(fullName, phoneNumber, dateOfBirth, gender);
        progressBar.setVisibility(View.VISIBLE);

        RetrofitClient.getStoryApiService().updateUserInfo(username, request).enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && "SUCCESS".equals(response.body().getMeta().getStatus())) {
                    Toast.makeText(EditAccountInfoActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String errorMessage = response.body() != null ? response.body().getMeta().getMessage() : response.message();
                    Toast.makeText(EditAccountInfoActivity.this, "Cập nhật thất bại: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EditAccountInfoActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String convertGenderToVietnamese(String gender) {
        if ("MALE".equalsIgnoreCase(gender)) {
            return "Nam";
        } else if ("FEMALE".equalsIgnoreCase(gender)) {
            return "Nữ";
        } else if ("OTHER".equalsIgnoreCase(gender)) {
            return "Khác";
        } else {
            return "Khác";
        }
    }

    private String convertGenderToApiFormat(String genderDisplay) {
        return switch (genderDisplay) {
            case "Nam" -> "MALE";
            case "Nữ" -> "FEMALE";
            case "Khác" -> "OTHER";
            default -> "OTHER";
        };
    }
}