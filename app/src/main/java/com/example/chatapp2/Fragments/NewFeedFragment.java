package com.example.chatapp2.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.example.chatapp2.Adapter.PostAdapter;
import com.example.chatapp2.MainActivity;
import com.example.chatapp2.Model.FriendRelation;
import com.example.chatapp2.Model.Post;
import com.example.chatapp2.Model.User;
import com.example.chatapp2.PostActivity;
import com.example.chatapp2.ProfileActivity;
import com.example.chatapp2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewFeedFragment extends Fragment {

    private CircleImageView profile_image;
    private Button post_button;
    private RecyclerView recyclerView;

    private PostAdapter postAdapter;

    private DatabaseReference reference;
    private FirebaseUser firebaseUser;
    private List<String> usersList;
    private List<Post> mPosts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_feed, container, false);
        profile_image = view.findViewById(R.id.profile_image);
        post_button = view.findViewById(R.id.post_button);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);

        usersList = new ArrayList<>();
        mPosts = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Friends_list");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        usersList.add(firebaseUser.getUid());

        readPost();

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();

                usersList.add(firebaseUser.getUid());

                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    FriendRelation chat = snapshot.getValue(FriendRelation.class);

                    if (chat.getUser().equals(firebaseUser.getUid())) {
                        usersList.add(chat.getFriend());
                    }
                }
                Log.d("newfeeddebug", "onDataChange: " + usersList.size());
                readPost();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (getContext() == null) {
                    return;
                }
                User user = dataSnapshot.getValue(User.class);
                if (user.getImageURL().equals("default")) {
                    profile_image.setImageResource(R.mipmap.ic_user_default);
                } else {
                    Glide.with(getContext()).load(user.getImageURL()).into(profile_image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        post_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), PostActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    private void readPost() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mPosts.clear();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    Log.d("newfeed", "onDataChangeSnapshot: " + post.getUserid());
                    for (String userId: usersList) {
                        Log.d("newfeed", "onUserlist: " + userId);
                        if (post.getUserid().equals(userId)) {
                            mPosts.add(post);
                        }
                    }
                }

                postAdapter = new PostAdapter(getContext(), mPosts);
                recyclerView.setAdapter(postAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        readPost();
    }
}
