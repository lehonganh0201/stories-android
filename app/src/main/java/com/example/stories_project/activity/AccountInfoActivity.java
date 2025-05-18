package com.example.stories_project.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.stories_project.MainActivity;
import com.example.stories_project.R;
import com.example.stories_project.model.ApiResponse;
import com.example.stories_project.network.RetrofitClient;
import com.example.stories_project.network.response.UserResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity to display user account information and menu options.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class AccountInfoActivity extends AppCompatActivity {

    private static final String PREF_NAME = "UserPrefs";
    private static final int REQUEST_CODE_EDIT = 100;
    private TextView fullNameTextView, phoneNumberTextView, dateOfBirthTextView, genderTextView, tvEdit;
    private ImageView avatarImage, plusButton;
    private ProgressBar progressBar;
    private ListView menuList;
    private String username;

    private Uri selectedImageUri;
    private Bitmap capturedBitmap;

    private static final DateTimeFormatter API_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final String[] menuItems = {
            "Danh sách truyện yêu thích",
            "Lịch sử đọc truyện",
            "Đổi mật khẩu",
            "Thông tin ứng dụng",
            "Đăng xuất"
    };

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Quyền camera bị từ chối", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> requestStoragePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openGallery();
                } else {
                    Toast.makeText(this, "Quyền truy cập bộ nhớ bị từ chối", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    capturedBitmap = (Bitmap) extras.get("data");
                    showImagePreview();
                }
            });

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    showImagePreview();
                }
            });

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
        avatarImage = findViewById(R.id.avatarImage);
        plusButton = findViewById(R.id.plusButton);
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

        plusButton.setOnClickListener(v -> showImageSourceDialog());

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

                                // Chọn avatar mặc định dựa trên giới tính
                                int defaultAvatar = "MALE".equalsIgnoreCase(user.gender()) ?
                                        R.drawable.default_avatar_male : R.drawable.default_avatar_female;

                                // Tải avatar
                                Log.d("AccountInfoActivity", "Giới tính: " + user.gender() + ", Avatar mặc định: " + defaultAvatar);
                                if (user.avatarUrl() != null && !user.avatarUrl().isEmpty()) {
                                    Glide.with(AccountInfoActivity.this)
                                            .load(user.avatarUrl())
                                            .placeholder(defaultAvatar)
                                            .error(defaultAvatar)
                                            .into(avatarImage);
                                } else {
                                    avatarImage.setImageResource(defaultAvatar);
                                }
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

    private void showImageSourceDialog() {
        String[] options = {"Chụp ảnh", "Chọn từ album"};
        new AlertDialog.Builder(this)
                .setTitle("Chọn nguồn ảnh")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        requestCameraPermission();
                    } else {
                        requestStoragePermission();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            openGallery();
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            requestStoragePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(cameraIntent);
        } else {
            Toast.makeText(this, "Không tìm thấy ứng dụng camera", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    private void showImagePreview() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_image_preview, null);
        ImageView previewImage = dialogView.findViewById(R.id.previewImage);

        if (capturedBitmap != null) {
            previewImage.setImageBitmap(capturedBitmap);
        } else if (selectedImageUri != null) {
            Glide.with(this).load(selectedImageUri).into(previewImage);
        }

        builder.setView(dialogView)
                .setTitle("Xác nhận ảnh")
                .setPositiveButton("Xác nhận", (dialog, which) -> uploadAvatar())
                .setNegativeButton("Hủy", (dialog, which) -> {
                    capturedBitmap = null;
                    selectedImageUri = null;
                })
                .show();
    }

    private void uploadAvatar() {
        Log.d("AccountInfoActivity", "Đang tải avatar cho username: " + username); // Thêm log để kiểm tra
        progressBar.setVisibility(View.VISIBLE);

        File imageFile;
        try {
            imageFile = createTempFile();
        } catch (IOException e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Lỗi khi tạo file ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), imageFile);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", imageFile.getName(), fileBody);

        RetrofitClient.getStoryApiService().uploadAvatar(username, filePart).enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                progressBar.setVisibility(View.GONE);
                capturedBitmap = null;
                selectedImageUri = null;

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<UserResponse> apiResponse = response.body();
                    if ("SUCCESS".equals(apiResponse.getMeta().getStatus())) {
                        UserResponse user = apiResponse.getData();
                        if (user != null && user.avatarUrl() != null) {
                            int defaultAvatar = "MALE".equalsIgnoreCase(user.gender()) ?
                                    R.drawable.default_avatar_male : R.drawable.default_avatar_female;

                            Log.d("AccountInfoActivity", "Giới tính sau upload: " + user.gender() + ", Avatar mặc định: " + defaultAvatar);
                            Glide.with(AccountInfoActivity.this)
                                    .load(user.avatarUrl())
                                    .placeholder(defaultAvatar)
                                    .error(defaultAvatar)
                                    .into(avatarImage);
                            Toast.makeText(AccountInfoActivity.this, "Cập nhật avatar thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AccountInfoActivity.this, "Không nhận được URL avatar", Toast.LENGTH_SHORT).show();
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
                capturedBitmap = null;
                selectedImageUri = null;
                Toast.makeText(AccountInfoActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File createTempFile() throws IOException {
        File imageFile = File.createTempFile("avatar", ".jpg", getCacheDir());
        if (capturedBitmap != null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            capturedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byte[] bitmapData = bos.toByteArray();
            FileOutputStream fos = new FileOutputStream(imageFile);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
        } else if (selectedImageUri != null) {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byte[] bitmapData = bos.toByteArray();
            FileOutputStream fos = new FileOutputStream(imageFile);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
        }
        return imageFile;
    }
}