package com.example.chatapp2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chatapp2.Adapter.MessageAdapter;
import com.example.chatapp2.Model.Chat;
import com.example.chatapp2.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    CircleImageView profileImage;
    TextView username;
    TextView status;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    Intent intent;

    ImageButton btn_send;
    EditText text_send;

    MessageAdapter messageAdapter;
    List<Chat> mChats;

    RecyclerView recyclerView;

    ValueEventListener seenListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChatActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profileImage = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        status = findViewById(R.id.status);

        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);


        intent = getIntent();
        final String userId = intent.getStringExtra("userid");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = text_send.getText().toString();
                if (!msg.equals("")) {
                    sendMessage(firebaseUser.getUid(), userId, msg);
                }
                text_send.setText("");
            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    username.setText(user.getUsername());
                    status.setText("- " + user.getStatus());
                    if (user.getStatus().equals("online")) {
                        status.setTextColor(getResources().getColor(R.color.onlineColor));
                    } else {
                        status.setTextColor(getResources().getColor(R.color.offlineColor));
                    }

                if (user.getImageURL().equals("default")) {
                    profileImage.setImageResource(R.mipmap.ic_user_default);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profileImage);
                }

                readMessage(firebaseUser.getUid(), userId, user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        seenMessage(userId);
    }

    private void seenMessage(final String userId) {
        reference = FirebaseDatabase.getInstance().getReference("Chat_logs");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userId)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("is_seen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String sender, final String receiver, String message) {

        Log.d("messageTest", "sendMessage: message Sent");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("is_seen", false);

        reference.child("Chat_logs").push().setValue(hashMap);

//        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
//                .child(firebaseUser.getUid())
//                .child(receiver);
//
//        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (!dataSnapshot.exists()) {
//                    chatRef.child("id").setValue(receiver);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
    }

    private void readMessage(final String myId, final String userId, final String imageURL) {
        mChats = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chat_logs");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChats.clear();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
//                    Log.d("recTest", "onDataChange: messageLoadedFromeDatabase");
//                    Log.d("recTest", "Receiver: " + chat.getReceiver());
//                    Log.d("recTest", "Sender: " + chat.getSender());
//                    Log.d("recTest", "Message: " + chat.getMessage());
//                    Log.d("recTest", "User: " + myId);
//                    Log.d("recTest", "FriendId: " + userId);
                    if ((chat.getReceiver().equals(myId) && chat.getSender().equals(userId)) ||
                            (chat.getReceiver().equals(userId) && chat.getSender().equals(myId)) ) {
//                        Log.d("recTest", "Message: " + chat.getMessage());
                        mChats.add(chat);
                    }
//                    Log.d("recTest", "chat length: " + mChats.size());
                    messageAdapter = new MessageAdapter(ChatActivity.this, mChats, imageURL);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void status(String status) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
    }
}
