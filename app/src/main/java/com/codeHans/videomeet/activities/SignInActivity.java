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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private MaterialButton btnSignIn;
    private ProgressBar pbSignIn;

    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_in);

        preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        findViewById(R.id.txtV_SignUp).setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));

        edtEmail = findViewById(R.id.edtV_InputMail);
        edtPassword = findViewById(R.id.edtV_InputPassword);
        btnSignIn = findViewById(R.id.btn_SignIn);
        pbSignIn = findViewById(R.id.pbSignIn);

        btnSignIn.setOnClickListener(view -> {
            if (edtEmail.getText().toString().trim().isEmpty()) {
                Toast.makeText(SignInActivity.this, R.string.email, Toast.LENGTH_LONG).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(edtEmail.getText().toString()).matches()) {
                Toast.makeText(SignInActivity.this, R.string.email, Toast.LENGTH_LONG).show();
            } else if (edtPassword.getText().toString().trim().isEmpty()) {
                Toast.makeText(SignInActivity.this, R.string.password, Toast.LENGTH_LONG).show();
            } else {
                signIn();
            }
        });
    }

    private void signIn() {
        btnSignIn.setVisibility(View.INVISIBLE);
        pbSignIn.setVisibility(View.INVISIBLE);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, edtEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, edtPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_FIRST_NAME, documentSnapshot.getString(Constants.KEY_FIRST_NAME));
                        preferenceManager.putString(Constants.KEY_LAST_NAME, documentSnapshot.getString(Constants.KEY_LAST_NAME));
                        preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL));

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        pbSignIn.setVisibility(View.INVISIBLE);
                        btnSignIn.setVisibility(View.VISIBLE);
                        Toast.makeText(SignInActivity.this, R.string.warning_sign_in, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
