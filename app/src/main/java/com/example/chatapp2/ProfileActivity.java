package com.example.chatapp2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatapp2.Model.FriendRelation;
import com.example.chatapp2.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    CircleImageView profileImage;
    TextView username;
    Button friendButton, chatButton;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profileImage = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        friendButton = findViewById(R.id.friend_button);
        chatButton = findViewById(R.id.chat_button);



        intent = getIntent();
        final String userId = intent.getStringExtra("userid");

        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
                intent.putExtra("userid",userId);
                ProfileActivity.this.startActivity(intent);
                finish();
            }
        });

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        processAddFriendButton(firebaseUser.getUid(), userId);


        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);


        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                getSupportActionBar().setTitle(user.getUsername());
                if (user.getImageURL().equals("default")) {
                    profileImage.setImageResource(R.mipmap.ic_user_default);
                } else {
                    Glide.with(ProfileActivity.this).load(user.getImageURL()).into(profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addFriend(String userid, String friendId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("user", userid);
        hashMap.put("friend", friendId);
        reference.child("Friends_list").push().setValue(hashMap);
        friendButton.setVisibility(View.GONE);
    }

    private void processAddFriendButton(final String userid, final String friendId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference =  FirebaseDatabase.getInstance().getReference("Friends_list");
        final boolean[] alreadyFriend = {false};
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    FriendRelation friendRelation = snapshot.getValue(FriendRelation.class);

                    if (userid.equals(friendRelation.getUser()) && friendId.equals(friendRelation.getFriend())) {
                        friendButton.setVisibility(View.GONE);
                        Log.d("friended", "onDataChange: true");
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (friendButton.getVisibility() != View.GONE) {
            friendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addFriend(userid, friendId);
                    Toast.makeText(ProfileActivity.this, "Add friend success", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
