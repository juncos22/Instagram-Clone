package dev.nicolas.firebasedemo.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.nicolas.firebasedemo.FollowersActivity;
import dev.nicolas.firebasedemo.MainActivity;
import dev.nicolas.firebasedemo.R;
import dev.nicolas.firebasedemo.fragments.ProfileFragment;
import dev.nicolas.firebasedemo.model.User;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private Context mContext;
    private List<User> users;
    private boolean isFragment;
    private FirebaseUser firebaseUser;

    public UserAdapter(Context mContext, List<User> users, boolean isFragment) {
        this.mContext = mContext;
        this.users = users;
        this.isFragment = isFragment;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        User user = users.get(position);

        holder.btnFollow.setVisibility(View.VISIBLE);
        holder.username.setText(user.getUsername());
        holder.fullname.setText(user.getFullname());

        Picasso.get().load(user.getImgUrl()).placeholder(R.mipmap.ic_launcher).into(holder.imageProfile);
        isFollowed(user.getId(), holder.btnFollow);

        if (user.getId().equals(firebaseUser.getUid())) {
            holder.btnFollow.setVisibility(View.GONE);
        }
        holder.btnFollow.setOnClickListener(v -> {
            if (holder.btnFollow.getText().toString().equals("follow")) {
                FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                        .child("following").child(user.getId()).setValue(true);
                FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId())
                        .child("followers").child(firebaseUser.getUid()).setValue(true);

                addNotification(user.getId());
            }else {
                FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                        .child("following").child(user.getId()).removeValue();
                FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId())
                        .child("followers").child(firebaseUser.getUid()).removeValue();
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (isFragment) {
                mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
                        .edit().putString("profileId", user.getId()).apply();

                ((FragmentActivity)mContext).getSupportFragmentManager()
                        .beginTransaction().replace(R.id.fragment_container, new ProfileFragment())
                        .commit();
            }else {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra("publisherId", user.getId());
                mContext.startActivity(intent);
            }
        });
    }

    private void addNotification(String userId) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userId", userId);
        hashMap.put("text", "Started following you");
        hashMap.put("postId", "");
        hashMap.put("post", false);
        FirebaseDatabase.getInstance().getReference().child("Notifications").child(firebaseUser.getUid())
                .push().setValue(hashMap);
    }

    private void isFollowed(final String id, final AppCompatButton button) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Follow")
                .child(firebaseUser.getUid())
                .child("following");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(id).exists()) {
                    button.setText("following");
                }else {
                    button.setText("follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView imageProfile;
        public TextView username, fullname;
        public AppCompatButton btnFollow;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProfile = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.username);
            fullname = itemView.findViewById(R.id.fullname);
            btnFollow = itemView.findViewById(R.id.btn_follow);
        }
    }
}
