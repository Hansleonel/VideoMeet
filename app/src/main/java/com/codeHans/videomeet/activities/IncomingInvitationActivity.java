package com.codeHans.videomeet.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.codeHans.videomeet.R;
import com.codeHans.videomeet.utilities.Constants;

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

    }
}