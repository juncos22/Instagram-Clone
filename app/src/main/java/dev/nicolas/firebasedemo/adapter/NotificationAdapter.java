package dev.nicolas.firebasedemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.nicolas.firebasedemo.R;
import dev.nicolas.firebasedemo.fragments.PostDetailFragment;
import dev.nicolas.firebasedemo.fragments.ProfileFragment;
import dev.nicolas.firebasedemo.model.Notification;
import dev.nicolas.firebasedemo.model.Post;
import dev.nicolas.firebasedemo.model.User;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private final Context context;
    private final List<Notification> notifications;

    public NotificationAdapter(Context context, List<Notification> notifications) {
        this.context = context;
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(context).inflate(R.layout.notification_item, parent, false);
        return new NotificationViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        getUser(holder.imgProfile, holder.username, notification.getUserId());
        holder.comment.setText(notification.getText());

        if (notification.isPost()) {
            holder.postImage.setVisibility(View.VISIBLE);
            getPostImage(holder.postImage,  notification.getPostId());
        }else {
            holder.postImage.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(v -> {
            if (notification.isPost()) {
                context.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
                        .edit().putString("postId", notification.getPostId()).apply();

                ((FragmentActivity)context).getSupportFragmentManager()
                        .beginTransaction().replace(R.id.fragment_container, new PostDetailFragment())
                        .commit();
            }else {
                context.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
                        .edit().putString("profileId", notification.getUserId()).apply();

                ((FragmentActivity)context).getSupportFragmentManager()
                        .beginTransaction().replace(R.id.fragment_container, new ProfileFragment())
                        .commit();
            }
        });
    }

    private void getPostImage(ImageView imageView, String postId) {
        FirebaseDatabase.getInstance().getReference().child("Posts").child(postId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Post post = snapshot.getValue(Post.class);
                        Picasso.get().load(post.getImageUrl()).placeholder(R.drawable.icon)
                                .into(imageView);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getUser(CircleImageView imageView, TextView textView, String userId) {
        FirebaseDatabase.getInstance().getReference().child("Users").child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user.getImgUrl().equals("default")) {
                            imageView.setImageResource(R.drawable.icon);
                        }else {
                            Picasso.get().load(user.getImgUrl()).placeholder(R.drawable.icon)
                                    .into(imageView);
                        }
                        textView.setText(user.getUsername());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView imgProfile;
        private TextView username, comment;
        private ImageView postImage;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
            postImage = itemView.findViewById(R.id.post_image);
        }
    }
}
