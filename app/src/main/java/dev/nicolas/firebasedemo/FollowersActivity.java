package dev.nicolas.firebasedemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import dev.nicolas.firebasedemo.adapter.UserAdapter;
import dev.nicolas.firebasedemo.model.User;

public class FollowersActivity extends AppCompatActivity {
    private String userId;
    private String title;
    private List<String> idList;
    private RecyclerView recyclerFollowers;
    private UserAdapter userAdapter;
    private List<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        title = intent.getStringExtra("title");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerFollowers = findViewById(R.id.recycler_followers);
        recyclerFollowers.setHasFixedSize(true);
        recyclerFollowers.setLayoutManager(new LinearLayoutManager(this));

        users = new ArrayList<>();
        userAdapter = new UserAdapter(this, users, false);
        recyclerFollowers.setAdapter(userAdapter);

        idList = new ArrayList<>();
        switch (title) {
            case "followers":
                getFollowers();
                break;
            case "followings":
                getFollowings();
                break;
            case "likes":
                getLikes();
                break;
        }
    }

    private void getFollowers() {
        FirebaseDatabase.getInstance().getReference().child("Follow").child(userId)
                .child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    idList.add(data.getKey());
                }
                showUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFollowings() {
        FirebaseDatabase.getInstance().getReference().child("Follow").child(userId)
                .child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    idList.add(data.getKey());
                }
                showUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getLikes() {
        FirebaseDatabase.getInstance().getReference().child("Likes")
                .child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    idList.add(data.getKey());
                }
                showUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showUsers() {
        FirebaseDatabase.getInstance().getReference().child("Users")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        users.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            User user = data.getValue(User.class);
                            for (String id : idList) {
                                if (user.getId().equals(id)) {
                                    users.add(user);
                                }
                            }
                        }
                        userAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}