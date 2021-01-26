package dev.nicolas.firebasedemo.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.nicolas.firebasedemo.EditProfileActivity;
import dev.nicolas.firebasedemo.FollowersActivity;
import dev.nicolas.firebasedemo.OptionsActivity;
import dev.nicolas.firebasedemo.R;
import dev.nicolas.firebasedemo.StartActivity;
import dev.nicolas.firebasedemo.adapter.PhotoAdapter;
import dev.nicolas.firebasedemo.adapter.PostAdapter;
import dev.nicolas.firebasedemo.model.Post;
import dev.nicolas.firebasedemo.model.User;

public class ProfileFragment extends Fragment {
    private RecyclerView recyclerPictures, recyclerSavedPictures;
    private PhotoAdapter photoAdapter, savedPostsAdapter;
    private List<Post> savedPosts;
    private List<Post> photoList;
    private CircleImageView imageProfile;
    private ImageView imgOptions;
    private TextView textPosts, textFollowers, textFollowing, textFullname, textBio, textUsername;
    private ImageButton imgMyPictures, imgSavedPictures;
    private AppCompatButton btnEditProfile;
    private FirebaseUser fUser;
    String profileId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        String data = getContext().getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
                .getString("profileId", "none");

        if (data.equals("none")) {
            profileId = fUser.getUid();
        }else {
            profileId = data;
        }


        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        imageProfile = root.findViewById(R.id.image_profile);
        imgOptions = root.findViewById(R.id.image_options);
        textBio = root.findViewById(R.id.text_bio);
        textPosts = root.findViewById(R.id.text_posts);
        textFollowers = root.findViewById(R.id.text_followers);
        textFollowing = root.findViewById(R.id.text_following);
        textFullname = root.findViewById(R.id.text_fullname);
        textBio = root.findViewById(R.id.text_bio);
        textUsername = root.findViewById(R.id.text_username);
        imgMyPictures = root.findViewById(R.id.my_pictures);
        imgSavedPictures = root.findViewById(R.id.saved_pictures);
        btnEditProfile = root.findViewById(R.id.btn_edit_profile);

        recyclerPictures = root.findViewById(R.id.recycler_pictures);
        recyclerPictures.setHasFixedSize(true);
        recyclerPictures.setLayoutManager(new GridLayoutManager(getContext(), 3));

        photoList = new ArrayList<>();
        photoAdapter = new PhotoAdapter(getContext(), photoList);
        recyclerPictures.setAdapter(photoAdapter);

        recyclerSavedPictures = root.findViewById(R.id.recycler_saved);
        recyclerSavedPictures.setHasFixedSize(true);
        recyclerSavedPictures.setLayoutManager(new GridLayoutManager(getContext(), 3));

        savedPosts = new ArrayList<>();
        savedPostsAdapter = new PhotoAdapter(getContext(), savedPosts);
        recyclerSavedPictures.setAdapter(savedPostsAdapter);

        getUserInfo();
        getFollowersAndFollowingCount();
        getPostCount();
        getPhotos();
        getSavedPosts();

        if (profileId.equals(fUser.getUid())) {
            btnEditProfile.setText("EDIT PROFILE");
        }else {
            checkFollowingStatus();
        }
        btnEditProfile.setOnClickListener(v -> {
            String buttonText = btnEditProfile.getText().toString();
            if (buttonText.equals("EDIT PROFILE")) {
                startActivity(new Intent(getContext(), EditProfileActivity.class));
            }else {
                if (buttonText.equals("follow")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(fUser.getUid())
                            .child("following").child(profileId).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId)
                            .child("followers").child(fUser.getUid()).setValue(true);
                }else {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(fUser.getUid())
                            .child("following").child(profileId).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId)
                            .child("followers").child(fUser.getUid()).removeValue();
                }
            }
        });

        imgMyPictures.setOnClickListener(v -> {
            recyclerPictures.setVisibility(View.VISIBLE);
            recyclerSavedPictures.setVisibility(View.GONE);
        });
        imgSavedPictures.setOnClickListener(v -> {
            recyclerPictures.setVisibility(View.GONE);
            recyclerSavedPictures.setVisibility(View.VISIBLE);
        });

        textFollowers.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), FollowersActivity.class);
            intent.putExtra("userId", profileId);
            intent.putExtra("title", "followers");
            startActivity(intent);
        });
        textFollowing.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), FollowersActivity.class);
            intent.putExtra("userId", profileId);
            intent.putExtra("title", "followings");
            startActivity(intent);
        });

        imgOptions.setOnClickListener(v -> startActivity(new Intent(getContext(), OptionsActivity.class)));
        return root;
    }

    private void getSavedPosts() {
        List<String> savedIds = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("Saves").child(fUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        savedIds.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            savedIds.add(data.getKey());
                        }
                        FirebaseDatabase.getInstance().getReference().child("Posts")
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        savedPosts.clear();
                                        for (DataSnapshot data : snapshot.getChildren()) {
                                            Post post = data.getValue(Post.class);
                                            for (String id : savedIds) {
                                                if (post.getPostId().equals(id)) {
                                                    savedPosts.add(post);
                                                }
                                            }
                                        }
                                        savedPostsAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getPhotos() {
        FirebaseDatabase.getInstance().getReference().child("Posts")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        photoList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Post post = data.getValue(Post.class);
                            if (post.getPublisher().equals(profileId)) {
                                photoList.add(post);
                            }
                        }
                        Collections.reverse(photoList);
                        photoAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkFollowingStatus() {
        FirebaseDatabase.getInstance().getReference().child("Follow").child(fUser.getUid()).child("following")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child(profileId).exists()) {
                            btnEditProfile.setText("following");
                        }else {
                            btnEditProfile.setText("follow");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getPostCount() {
        FirebaseDatabase.getInstance().getReference().child("Posts")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int counter = 0;
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Post post = data.getValue(Post.class);
                            if (post.getPublisher().equals(profileId)) {
                                counter++;
                            }
                        }
                        textPosts.setText(String.format("%s", counter));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getFollowersAndFollowingCount() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId);
        ref.child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                textFollowers.setText(String.format("%s", snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        ref.child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                textFollowing.setText(String.format("%s", snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUserInfo() {
        FirebaseDatabase.getInstance().getReference().child("Users").child(profileId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user.getImgUrl().equals("default")) {
                            imageProfile.setImageResource(R.drawable.icon);
                        }else {
                            Picasso.get().load(user.getImgUrl()).placeholder(R.drawable.icon)
                                    .into(imageProfile);
                            textUsername.setText(user.getUsername());
                            textFullname.setText(user.getFullname());
                            textBio.setText(user.getBio());

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}