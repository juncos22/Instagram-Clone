package dev.nicolas.firebasedemo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.nicolas.firebasedemo.adapter.CommentAdapter;
import dev.nicolas.firebasedemo.model.Comment;
import dev.nicolas.firebasedemo.model.User;

public class CommentActivity extends AppCompatActivity {
    private EditText etAddComment;
    private CircleImageView imgProfile;
    private TextView tvPost;
    private String postId;
    private String authorId;
    private RecyclerView recyclerComments;
    private FirebaseUser fUser;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        authorId = intent.getStringExtra("authorId");

        recyclerComments = findViewById(R.id.recycler_comments);
        recyclerComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerComments.setHasFixedSize(true);

        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList, postId);
        recyclerComments.setAdapter(commentAdapter);

        getComments();
        etAddComment = findViewById(R.id.et_add_comment);
        imgProfile = findViewById(R.id.image_profile);
        tvPost = findViewById(R.id.post);

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        getUserImage();

        tvPost.setOnClickListener(v -> {
            if (TextUtils.isEmpty(etAddComment.getText().toString())) {
                Toast.makeText(CommentActivity.this, "No comment added", Toast.LENGTH_SHORT).show();
            }else {
                putComment();
            }
        });
    }

    private void getComments() {
        FirebaseDatabase.getInstance().getReference().child("Comments")
                .child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Comment comment = data.getValue(Comment.class);
                    commentList.add(comment);
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void putComment() {
        HashMap<String, Object> map = new HashMap<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Comments").child(postId);
        String id = ref.push().getKey();

        map.put("id", id);
        map.put("comment", etAddComment.getText().toString());
        map.put("publisher", fUser.getUid());
        etAddComment.getText().clear();

        ref.child(id).setValue(map).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(CommentActivity.this, "Comment added!", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(CommentActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void getUserImage() {
        FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                       if (user.getImgUrl().equals("default")) {
                           imgProfile.setImageResource(R.drawable.icon);
                       }else {
                           Picasso.get().load(user.getImgUrl())
                                   .placeholder(R.drawable.icon)
                                   .into(imgProfile);
                       }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}