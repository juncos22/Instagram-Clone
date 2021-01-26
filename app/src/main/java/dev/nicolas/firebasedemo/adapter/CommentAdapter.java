package dev.nicolas.firebasedemo.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.nicolas.firebasedemo.MainActivity;
import dev.nicolas.firebasedemo.R;
import dev.nicolas.firebasedemo.model.Comment;
import dev.nicolas.firebasedemo.model.User;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private final Context context;
    private final List<Comment> comments;
    private final FirebaseUser fUser;
    private String postId;

    public CommentAdapter(Context context, List<Comment> comments, String postId) {
        this.context = context;
        this.comments = comments;
        this.postId = postId;
        fUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.textComment.setText(comment.getComment());
        FirebaseDatabase.getInstance().getReference().child("Users").child(comment.getPublisher())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        holder.textUsername.setText(user.getUsername());
                        if (user.getImgUrl().equals("default")) {
                            holder.imgProfile.setImageResource(R.drawable.icon);
                        } else {
                            Picasso.get().load(user.getImgUrl()).placeholder(R.drawable.icon)
                                    .into(holder.imgProfile);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        holder.textComment.setOnClickListener(v -> {
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("publisherId", comment.getPublisher());
            context.startActivity(intent);
        });
        holder.imgProfile.setOnClickListener(v -> {
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("publisherId", comment.getPublisher());
            context.startActivity(intent);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (comment.getPublisher().endsWith(fUser.getUid())) {
                AlertDialog ad = new AlertDialog.Builder(context).create();
                ad.setTitle("Do you want to delete it?");
                ad.setButton(AlertDialog.BUTTON_NEUTRAL, "No", (dialog, which) -> {
                    dialog.dismiss();
                });
                ad.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", (dialog, which) -> {
                    FirebaseDatabase.getInstance().getReference().child("Comments").child(postId)
                            .child(comment.getId()).removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Comment deleted!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    });
                });
                ad.show();
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imgProfile;
        TextView textUsername, textComment;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.image_profile);
            textUsername = itemView.findViewById(R.id.username);
            textComment = itemView.findViewById(R.id.comment);
        }
    }
}
