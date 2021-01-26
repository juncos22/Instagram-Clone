package dev.nicolas.firebasedemo.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.appcompat.widget.SocialTextView;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import dev.nicolas.firebasedemo.CommentActivity;
import dev.nicolas.firebasedemo.FollowersActivity;
import dev.nicolas.firebasedemo.R;
import dev.nicolas.firebasedemo.fragments.PostDetailFragment;
import dev.nicolas.firebasedemo.fragments.ProfileFragment;
import dev.nicolas.firebasedemo.model.Post;
import dev.nicolas.firebasedemo.model.User;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private Context context;
    private List<Post> posts;
    private FirebaseUser firebaseUser;

    public PostAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        Picasso.get().load(post.getImageUrl()).placeholder(R.drawable.icon).into(holder.imgPost);
        holder.stvDescription.setText(post.getDescription());
        FirebaseDatabase.getInstance().getReference().child("Users").child(post.getPublisher())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user.getImgUrl().equals("default")) {
                            holder.imgProfile.setImageResource(R.drawable.icon);
                        }else {
                            Picasso.get().load(user.getImgUrl()).placeholder(R.drawable.icon).into(holder.imgProfile);
                        }
                        holder.tvUsername.setText(user.getUsername());
                        holder.tvAuthor.setText(user.getFullname());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        isLiked(post.getPostId(), holder.imgLike);
        getNLikes(post.getPostId(), holder.tvNLikes);
        getComments(post.getPostId(), holder.tvNComments);
        isSaved(post.getPostId(), holder.imgSave);

        holder.imgLike.setOnClickListener(v -> {
            if (holder.imgLike.getTag().equals("Like")) {
                FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostId())
                        .child(firebaseUser.getUid()).setValue(true);

                addNotification(post.getPostId(), post.getPublisher());
            }else {
                FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostId())
                        .child(firebaseUser.getUid()).removeValue();
            }
        });
        holder.imgComment.setOnClickListener(v -> {
            Intent intent = new Intent(context, CommentActivity.class);
            intent.putExtra("postId", post.getPostId());
            intent.putExtra("authorId", post.getPublisher());
            context.startActivity(intent);
        });
        holder.tvNComments.setOnClickListener(v -> {
            Intent intent = new Intent(context, CommentActivity.class);
            intent.putExtra("postId", post.getPostId());
            intent.putExtra("authorId", post.getPublisher());
            context.startActivity(intent);
        });
        holder.imgSave.setOnClickListener(v -> {
            if (holder.imgSave.getTag().equals("save")) {
                FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                        .child(post.getPostId()).setValue(true);
            }else {
                FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                        .child(post.getPostId()).removeValue();
            }
        });
        holder.imgProfile.setOnClickListener(v -> {
            context.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
                    .edit().putString("profileId", post.getPublisher()).apply();
            ((FragmentActivity)context).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment()).commit();
        });
        holder.tvUsername.setOnClickListener(v -> {
            context.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
                    .edit().putString("profileId", post.getPublisher()).apply();
            ((FragmentActivity)context).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment()).commit();
        });
        holder.tvAuthor.setOnClickListener(v -> {
            context.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
                    .edit().putString("profileId", post.getPublisher()).apply();
            ((FragmentActivity)context).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment()).commit();
        });
        holder.imgPost.setOnClickListener(v -> {
            context.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
                    .edit().putString("postId", post.getPostId()).apply();

            ((FragmentActivity)context).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PostDetailFragment()).commit();
        });
        holder.tvNLikes.setOnClickListener(v -> {
            Intent intent = new Intent(context, FollowersActivity.class);
            intent.putExtra("userId", post.getPublisher());
            intent.putExtra("title", "likes");
            context.startActivity(intent);
        });
    }

    private void addNotification(String postId, String publisher) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userId", publisher);
        hashMap.put("text", "Liked your post");
        hashMap.put("postId", postId);
        hashMap.put("post", true);
        FirebaseDatabase.getInstance().getReference().child("Notifications").child(firebaseUser.getUid())
                .push().setValue(hashMap);
    }

    private void isSaved(String postId, ImageView imageView) {
        FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child(postId).exists()) {
                            imageView.setImageResource(R.drawable.ic_saved);
                            imageView.setTag("saved");
                        }else {
                            imageView.setImageResource(R.drawable.ic_save);
                            imageView.setTag("save");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    private void isLiked(String postId, ImageView imageView) {
        FirebaseDatabase.getInstance().getReference().child("Likes")
                .child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(firebaseUser.getUid()).exists()) {
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("Liked");
                }else {
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getNLikes(String postId, TextView textView) {
        FirebaseDatabase.getInstance().getReference().child("Likes")
                .child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                textView.setText(String.format("%s likes", snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getComments(String postId, TextView textView) {
        FirebaseDatabase.getInstance().getReference().child("Comments")
                .child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                textView.setText(String.format("View all %s comments", snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProfile, imgPost, imgLike, imgComment, imgSave, imgMore;
        TextView tvUsername, tvNLikes, tvAuthor, tvNComments;
        SocialTextView stvDescription;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.profile_image);
            imgPost = itemView.findViewById(R.id.post_image);
            imgLike = itemView.findViewById(R.id.like);
            imgComment = itemView.findViewById(R.id.comment);
            imgSave = itemView.findViewById(R.id.save);
            tvUsername = itemView.findViewById(R.id.username_text);
            tvNComments = itemView.findViewById(R.id.num_of_comments);
            tvNLikes = itemView.findViewById(R.id.num_of_likes);
            tvAuthor = itemView.findViewById(R.id.author_text);
            stvDescription = itemView.findViewById(R.id.description);
        }
    }
}
