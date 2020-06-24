package com.example.chatapp2.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp2.Model.Post;
import com.example.chatapp2.Model.User;
import com.example.chatapp2.ProfileActivity;
import com.example.chatapp2.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private Context mContext;
    private List<Post> mPosts;

    private DatabaseReference reference;
    private FirebaseUser firebaseUser;

    public PostAdapter(Context mContext, List<Post> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_post, parent, false);
        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Post post = mPosts.get(position);

        reference = FirebaseDatabase.getInstance().getReference("Users").child(post.getUserid());

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                holder.username.setText(user.getUsername());
                if (user.getImageURL().equals("default")) {
                    holder.profile_image.setImageResource(R.mipmap.ic_user_default);
                } else {
                    Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.status.setText(post.getStatus());
        if (post.getImage().equals("none")){
            holder.image_post.setVisibility(View.GONE);
        } else {
            Glide.with(mContext).load(post.getImage()).into(holder.image_post);
        }

        holder.like_count.setText(post.getLike_count() + " people liked this post");

        holder.like_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("id")
                        .equalTo(post.getId());
                Log.d("like", "onClick: " + post.getId());
                ValueEventListener eventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            Log.d("like", "onDataChange: ");
                            snapshot.child("like_count").getRef().setValue(post.getLike_count() + 1);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };
                query.addListenerForSingleValueEvent(eventListener);
                post.setLike_count(post.getLike_count() + 1);
                holder.like_count.setText(post.getLike_count() + " people liked this post");
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public ImageView profile_image;
        public TextView status;
        public ImageView image_post;
        public TextView like_count;
        public ImageButton like_button;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            status = itemView.findViewById(R.id.status);
            image_post = itemView.findViewById(R.id.image_post);
            like_count = itemView.findViewById(R.id.like_count);
            like_button = itemView.findViewById(R.id.like_button);
        }
    }
}
