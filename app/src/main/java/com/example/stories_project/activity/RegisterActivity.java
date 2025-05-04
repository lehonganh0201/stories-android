package com.example.stories_project.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stories_project.MainActivity;
import com.example.stories_project.R;
import com.example.stories_project.network.request.UserRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class RegisterActivity extends AppCompatActivity {
    private EditText etFullName, etPhoneNumber, etDateOfBirth;
    private Spinner spGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFullName = findViewById(R.id.etFullName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etDateOfBirth = findViewById(R.id.etDateOfBirth);
        spGender = findViewById(R.id.spGender);
        Button btnContinue = findViewById(R.id.btnContinue);
        TextView tvLogin = findViewById(R.id.tvLogin);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGender.setAdapter(adapter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            btnContinue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String fullName = etFullName.getText().toString().trim();
                    String phoneNumber = etPhoneNumber.getText().toString().trim();
                    String dateOfBirthStr = etDateOfBirth.getText().toString().trim();
                    String genderDisplay = spGender.getSelectedItem().toString();

                    if (fullName.isEmpty() || phoneNumber.isEmpty() || dateOfBirthStr.isEmpty() || genderDisplay.equals("Chọn giới tính")) {
                        Toast.makeText(RegisterActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    LocalDateTime dateOfBirth;
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        dateOfBirth = LocalDateTime.parse(dateOfBirthStr + " 00:00:00", DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                    } catch (DateTimeParseException e) {
                        Toast.makeText(RegisterActivity.this, "Định dạng ngày sinh không hợp lệ (dd/MM/yyyy)", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String gender;
                    switch (genderDisplay) {
                        case "Nam":
                            gender = "MALE";
                            break;
                        case "Nữ":
                            gender = "FEMALE";
                            break;
                        case "Khác":
                            gender = "OTHER";
                            break;
                        default:
                            Toast.makeText(RegisterActivity.this, "Giới tính không hợp lệ", Toast.LENGTH_SHORT).show();
                            return;
                    }

                    String dateOfBirthFormatted = dateOfBirth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                    UserRequest userRequest = new UserRequest(fullName, phoneNumber, dateOfBirthFormatted, gender);

                    Intent intent = new Intent(RegisterActivity.this, AccountSetupActivity.class);
                    intent.putExtra("userRequest", userRequest);
                    startActivity(intent);
                }
            });
        }

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}