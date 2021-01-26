package dev.nicolas.firebasedemo.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import dev.nicolas.firebasedemo.R;
import dev.nicolas.firebasedemo.adapter.PostAdapter;
import dev.nicolas.firebasedemo.model.Post;

public class HomeFragment extends Fragment {
    private PostAdapter postAdapter;
    private RecyclerView recyclerPosts;
    private List<Post> postList;
    private List<String> followingList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        recyclerPosts = root.findViewById(R.id.recycler_posts);
        recyclerPosts.setHasFixedSize(true);
        recyclerPosts.setLayoutManager(linearLayoutManager);

        followingList = new ArrayList<>();
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList);
        recyclerPosts.setAdapter(postAdapter);

        checkFollowingUsers();
        return root;
    }

    private void checkFollowingUsers() {
        FirebaseDatabase.getInstance().getReference().child("Follow")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("following")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        followingList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            followingList.add(data.getKey());
                        }
                        followingList.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        readPosts();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void readPosts() {
        FirebaseDatabase.getInstance().getReference().child("Posts")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        postList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Post post = data.getValue(Post.class);
                            for (String id : followingList) {
                                if (post.getPublisher().equals(id)) {
                                    postList.add(post);
                                }
                            }
                        }
                        postAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}