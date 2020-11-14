package com.codeHans.videomeet.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codeHans.videomeet.R;
import com.codeHans.videomeet.network.ApiClient;
import com.codeHans.videomeet.network.ApiService;
import com.codeHans.videomeet.utilities.Constants;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;

public class IncomingInvitationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_invitation);

        ImageView imageMeetingType = findViewById(R.id.imageMeetingType);
        String meetingType = getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_TYPE);

        if (meetingType != null) {
            if (meetingType.equals("video")) {
                imageMeetingType.setImageResource(R.drawable.ic_video);
            }
        }

        TextView textVFirstChar = findViewById(R.id.txtVFirstChar);
        TextView textVUserName = findViewById(R.id.txtUserNameIncoming);
        TextView textVEmail = findViewById(R.id.txtVEmailIncoming);

        String firstName = getIntent().getStringExtra(Constants.KEY_FIRST_NAME);
        if (firstName != null) {
            textVFirstChar.setText(firstName.substring(0, 1));
        }

        textVUserName.setText(String.format(
                "%s %s",
                firstName,
                getIntent().getStringExtra(Constants.KEY_LAST_NAME)
        ));

        textVEmail.setText(getIntent().getStringExtra(Constants.KEY_EMAIL));

        ImageView imageAcceptInvitation = findViewById(R.id.imgVAcceptInvitation);
        imageAcceptInvitation.setOnClickListener(view -> sendInvitationResponse(
                Constants.REMOTE_MSG_INVITATION_ACCEPTED,
                getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN)
        ));

        ImageView imageRejectInvitation = findViewById(R.id.imgVRejectInvitation);
        imageRejectInvitation.setOnClickListener(view -> sendInvitationResponse(
                Constants.REMOTE_MSG_INVITATION_REJECTED,
                getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN)
        ));

    }

    private void sendInvitationResponse(String type, String receiverToken) {
        try {

            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, type);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), type);

        } catch (Exception exception) {
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void sendRemoteMessage(String remoteMessageBody, String type) {
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(
                Constants.getRemoteMessageHeaders(), remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)) {
                        Toast.makeText(IncomingInvitationActivity.this, "Invitation Accepted", Toast.LENGTH_LONG).show();

                        try {
                            URL serverURL = new URL("https://meet.jit.si");
                            JitsiMeetConferenceOptions conferenceOptions =
                                    new JitsiMeetConferenceOptions.Builder()
                                            .setServerURL(serverURL)
                                            .setWelcomePageEnabled(false)
                                            .setRoom(getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_ROOM))
                                            .build();
                            JitsiMeetActivity.launch(IncomingInvitationActivity.this, conferenceOptions);
                            finish();

                        } catch (Exception exception) {
                            Toast.makeText(IncomingInvitationActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(IncomingInvitationActivity.this, "Invitation Rejected", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(IncomingInvitationActivity.this, response.message(), Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(IncomingInvitationActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if (type != null) {
                if (type.equals(Constants.REMOTE_MSG_INVITATION_CANCELLED)) {
                    Toast.makeText(context, "Invitation Cancelled", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                invitationResponseReceiver,
                new IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                invitationResponseReceiver
        );
    }
}