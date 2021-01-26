package dev.nicolas.firebasedemo.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

import dev.nicolas.firebasedemo.R;
import dev.nicolas.firebasedemo.adapter.TagAdapter;
import dev.nicolas.firebasedemo.adapter.UserAdapter;
import dev.nicolas.firebasedemo.model.User;

public class SearchFragment extends Fragment {
    private RecyclerView recyclerView;
    private AutoCompleteTextView searchBar;
    private List<User> users;
    private UserAdapter userAdapter;
    private RecyclerView recyclerTags;
    private List<String> hashtags;
    private List<String> hashtagCounts;
    private TagAdapter tagAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerTags = view.findViewById(R.id.recycler_view_tags);
        recyclerTags.setHasFixedSize(true);
        recyclerTags.setLayoutManager(new LinearLayoutManager(getContext()));

        hashtags = new ArrayList<>();
        hashtagCounts = new ArrayList<>();
        tagAdapter = new TagAdapter(getContext(), hashtags, hashtagCounts);
        recyclerTags.setAdapter(tagAdapter);

        searchBar = view.findViewById(R.id.search_bar);

        users = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(), users, true);
        recyclerView.setAdapter(userAdapter);

        readUsers();
        readTags();
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUser(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });
        return view;
    }

    private void readTags() {
        FirebaseDatabase.getInstance().getReference().child("HashTags")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        hashtags.clear();
                        hashtagCounts.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            hashtags.add(data.getKey());
                            hashtagCounts.add(String.valueOf(data.getChildrenCount()));
                        }
                        tagAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void readUsers() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Users");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (TextUtils.isEmpty(searchBar.getText().toString())) {
                    users.clear();
                    for (DataSnapshot data : snapshot.getChildren()) {
                        User user = data.getValue(User.class);
                        users.add(user);
                    }
                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void searchUser(String s) {
        Query query = FirebaseDatabase.getInstance().getReference().child("Users")
                .orderByChild("username").startAt(s).endAt(s + "\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    User user = data.getValue(User.class);
                    users.add(user);
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void filter(String s) {
        List<String> searchTags = new ArrayList<>();
        List<String> searchTagsCount = new ArrayList<>();
        for (String tag : hashtags) {
            if (tag.toLowerCase().contains(s.toLowerCase())) {
                searchTags.add(tag);
                searchTagsCount.add(hashtagCounts.get(hashtags.indexOf(tag)));
            }
        }
        tagAdapter.filter(searchTags, searchTagsCount);
    }
}