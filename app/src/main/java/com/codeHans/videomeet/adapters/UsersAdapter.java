package com.codeHans.videomeet.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codeHans.videomeet.R;
import com.codeHans.videomeet.listeners.UsersListener;
import com.codeHans.videomeet.models.User;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private List<User> users;
    private UsersListener usersListener;

    public UsersAdapter(List<User> users, UsersListener usersListener) {
        this.users = users;
        this.usersListener = usersListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_user,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }


    class UserViewHolder extends RecyclerView.ViewHolder {

        TextView txtVFirstChar, txtVUserName, txtVEmail;
        ImageView imgAudioMeeting, imgVideoMeeting;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            txtVFirstChar = itemView.findViewById(R.id.txtV_FirstChar);
            txtVUserName = itemView.findViewById(R.id.txtV_user_name);
            txtVEmail = itemView.findViewById(R.id.txtV_email);
            imgAudioMeeting = itemView.findViewById(R.id.imgV_AudioMeeting);
            imgVideoMeeting = itemView.findViewById(R.id.imgV_VideoMeeting);
        }

        void setUserData(User user) {
            txtVFirstChar.setText(user.firstName.substring(0, 1));
            txtVUserName.setText(String.format("%s %s", user.firstName, user.lastName));
            txtVEmail.setText(user.email);
            imgAudioMeeting.setOnClickListener(view -> usersListener.initiateAudioMeeting(user));
            imgVideoMeeting.setOnClickListener(view -> usersListener.initiateVideoMeeting(user));
        }
    }
}
