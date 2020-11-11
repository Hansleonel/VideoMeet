package com.codeHans.videomeet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codeHans.videomeet.R;
import com.codeHans.videomeet.adapters.UsersAdapter;
import com.codeHans.videomeet.listeners.UsersListener;
import com.codeHans.videomeet.models.User;
import com.codeHans.videomeet.utilities.Constants;
import com.codeHans.videomeet.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity implements UsersListener {

    private PreferenceManager preferenceManager;
    private List<User> users;
    private UsersAdapter usersAdapter;

    private TextView textTitle, txtVErrorMessage;
    private RecyclerView userRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferenceManager = new PreferenceManager(getApplicationContext());

        textTitle = findViewById(R.id.txtV_title);
        textTitle.setText(String.format("%s %s",
                preferenceManager.getString(Constants.KEY_FIRST_NAME),
                preferenceManager.getString(Constants.KEY_LAST_NAME)
        ));

        findViewById(R.id.txtV_SignOut).setOnClickListener(view -> signOut());

        // TODO getToken from FCM
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                sendFCMTokenDatabase(task.getResult().getToken());
            }
        });

        userRecyclerView = findViewById(R.id.userRecyclerView);
        txtVErrorMessage = findViewById(R.id.txtV_ErrorMessage);

        users = new ArrayList<>();
        usersAdapter = new UsersAdapter(users, this);
        userRecyclerView.setAdapter(usersAdapter);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshMain);
        swipeRefreshLayout.setOnRefreshListener(this::getUsers);

        getUsers();
    }

    private void getUsers() {
        swipeRefreshLayout.setRefreshing(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    swipeRefreshLayout.setRefreshing(false);
                    String myUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        users.clear();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            // TODO principal user continue
                            if (myUserId.equals(documentSnapshot.getId())) {
                                continue;
                            }
                            User user = new User();
                            user.firstName = documentSnapshot.getString(Constants.KEY_FIRST_NAME);
                            user.lastName = documentSnapshot.getString(Constants.KEY_LAST_NAME);
                            user.email = documentSnapshot.getString(Constants.KEY_EMAIL);
                            user.token = documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            users.add(user);
                        }
                        if (users.size() > 0) {
                            usersAdapter.notifyDataSetChanged();
                        } else {
                            txtVErrorMessage.setText(String.format("%s", "No Users available"));
                            txtVErrorMessage.setVisibility(View.VISIBLE);
                        }

                    } else {
                        txtVErrorMessage.setText(String.format("%s", "No Users available"));
                        txtVErrorMessage.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void sendFCMTokenDatabase(String token) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                // .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "Token Update Successfully", Toast.LENGTH_LONG).show())
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Unable to send Token" + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void signOut() {
        Toast.makeText(this, "Sign Out", Toast.LENGTH_LONG).show();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(aVoid -> {
                    preferenceManager.clearPreferences();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Unable to sign out", Toast.LENGTH_LONG).show());
    }

    @Override
    public void initiateVideoMeeting(User user) {
        if (user.token == null || user.token.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    user.firstName + " " + user.lastName + " " + "is not available for Video meeting",
                    Toast.LENGTH_LONG
            ).show();
        } else {
            Toast.makeText(
                    this,
                    user.firstName + " " + user.lastName + " " + "Great, is available for Video meeting",
                    Toast.LENGTH_LONG
            ).show();
            Intent intent = new Intent(getApplicationContext(), OutgoingInvitationActivity.class);
            intent.putExtra("user", user);
            intent.putExtra("type", "video");
            startActivity(intent);
        }
    }

    @Override
    public void initiateAudioMeeting(User user) {
        if (user.token == null || user.token.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    user.firstName + " " + user.lastName + " " + "is not available for Audio meeting",
                    Toast.LENGTH_LONG
            ).show();
        } else {
            Toast.makeText(
                    this,
                    user.firstName + " " + user.lastName + " " + "Great, is available for Audio meeting",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}
