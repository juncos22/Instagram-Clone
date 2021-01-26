package dev.nicolas.firebasedemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import dev.nicolas.firebasedemo.R;
import dev.nicolas.firebasedemo.fragments.PostDetailFragment;
import dev.nicolas.firebasedemo.model.Post;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {
    private final Context context;
    private final List<Post> posts;

    public PhotoAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.photo_item, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Post post = posts.get(position);
        Picasso.get().load(post.getImageUrl()).placeholder(R.drawable.icon)
                .into(holder.postImage);
        holder.postImage.setOnClickListener(v -> {
            context.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
                    .edit().putString("postId", post.getPostId()).apply();

            ((FragmentActivity)context).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PostDetailFragment()).commit();
        });

    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView postImage;
        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.post_image);
        }
    }
}
