package com.codeHans.videomeet.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.codeHans.videomeet.R;
import com.codeHans.videomeet.utilities.Constants;
import com.codeHans.videomeet.utilities.PreferenceManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private EditText edtFirstName, edtLastName, edtEmail, edtPassword, edtConfirmPassword;
    private MaterialButton btnSignUp;
    private ProgressBar pbSignUp;

    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);

        preferenceManager = new PreferenceManager(getApplicationContext());

        findViewById(R.id.imgBack).setOnClickListener(view -> onBackPressed());
        findViewById(R.id.txtV_SignIn).setOnClickListener(view -> onBackPressed());

        edtFirstName = findViewById(R.id.edtV_FirstName);
        edtLastName = findViewById(R.id.edtV_SecondName);
        edtEmail = findViewById(R.id.edtV_InputMailSignUp);
        edtPassword = findViewById(R.id.edtV_InputPasswordSignUp);
        edtConfirmPassword = findViewById(R.id.edtV_ConfirmPasswordSignUp);
        btnSignUp = findViewById(R.id.btn_SignUp);
        pbSignUp = findViewById(R.id.pbSignUp);

        btnSignUp.setOnClickListener(view -> {
            if (edtFirstName.getText().toString().trim().isEmpty()) {
                Toast.makeText(SignUpActivity.this, R.string.first_name, Toast.LENGTH_LONG).show();
            } else if (edtLastName.getText().toString().trim().isEmpty()) {
                Toast.makeText(SignUpActivity.this, R.string.second_name, Toast.LENGTH_LONG).show();
            } else if (edtEmail.getText().toString().trim().isEmpty()) {
                Toast.makeText(SignUpActivity.this, R.string.email, Toast.LENGTH_LONG).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(edtEmail.getText().toString()).matches()) {
                Toast.makeText(SignUpActivity.this, R.string.email, Toast.LENGTH_LONG).show();
            } else if (edtPassword.getText().toString().trim().isEmpty()) {
                Toast.makeText(SignUpActivity.this, R.string.password, Toast.LENGTH_LONG).show();
            } else if (!edtPassword.getText().toString().equals(edtConfirmPassword.getText().toString())) {
                Toast.makeText(SignUpActivity.this, R.string.confirm_password_same, Toast.LENGTH_LONG).show();
            } else {
                signUp();
            }
        });

    }

    private void signUp() {

        btnSignUp.setVisibility(View.INVISIBLE);
        pbSignUp.setVisibility(View.VISIBLE);

        FirebaseFirestore database = FirebaseFirestore.getInstance();

        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_FIRST_NAME, edtFirstName.getText().toString());
        user.put(Constants.KEY_LAST_NAME, edtLastName.getText().toString());
        user.put(Constants.KEY_EMAIL, edtEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, edtPassword.getText().toString());

        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {

                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Constants.KEY_FIRST_NAME, edtFirstName.getText().toString());
                    preferenceManager.putString(Constants.KEY_LAST_NAME, edtLastName.getText().toString());
                    preferenceManager.putString(Constants.KEY_EMAIL, edtEmail.getText().toString());

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                })
                .addOnFailureListener(e -> {

                    pbSignUp.setVisibility(View.INVISIBLE);
                    btnSignUp.setVisibility(View.VISIBLE);
                    Toast.makeText(SignUpActivity.this, R.string.warning + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
