package com.codeHans.videomeet.listeners;

import com.codeHans.videomeet.models.User;

public interface UsersListener {

    void initiateVideoMeeting(User user);

    void initiateAudioMeeting(User user);
}
