package com.example.chatapp2.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chatapp2.Adapter.ChatsAdapter;
import com.example.chatapp2.Adapter.UserAdapter;
import com.example.chatapp2.Model.Chat;
import com.example.chatapp2.Model.Chatlist;
import com.example.chatapp2.Model.User;
import com.example.chatapp2.Notification.Token;
import com.example.chatapp2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;


public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;

    private ChatsAdapter chatsAdapter;
    private List<User> mUsers;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    private List<Chatlist> usersList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        usersList = new ArrayList<>();
        Query query = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(firebaseUser.getUid()).orderByChild("timestamp");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    usersList.add(chatlist);
                }

                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        updateToken(FirebaseInstanceId.getInstance().getToken());

        return view;
    }

    private void updateToken(String token) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(firebaseUser.getUid()).setValue(token1);
    }

    private void chatList() {
        mUsers = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                    for (Chatlist chatlist: usersList) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            User user = snapshot.getValue(User.class);
                            if (user.getId().equals(chatlist.getId())) {
                                mUsers.add(user);
                                break;
                            }
                        }
                    }
                ChatsAdapter chatsAdapter = new ChatsAdapter(getContext(), mUsers);
                recyclerView.setAdapter(chatsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}


//    private void readChats() {
//        mUsers = new ArrayList<>();
//
//        reference = FirebaseDatabase.getInstance().getReference("Users");
//
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                mUsers.clear();
//
//                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
//                    User user = snapshot.getValue(User.class);
//
//                    for (String id: usersList) {
//                        if (user.getId().equals(id)) {
//                            if (mUsers.size() != 0) {
//                                boolean flag = true;
//                                for (User user1: mUsers) {
//                                    if (user.getId().equals(user1.getId())) {
//                                        flag = false;
//                                        break;
//                                    }
//                                }
//                                if (flag)
//                                    mUsers.add(user);
//                            } else {
//                                mUsers.add(user);
//                            }
//                        }
//                    }
//                }
//                Log.d("CFTest", "onDataChange: " + mUsers.size());
//                ChatsAdapter chatsAdapter = new ChatsAdapter(getContext(), mUsers);
//                recyclerView.setAdapter(chatsAdapter);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }
