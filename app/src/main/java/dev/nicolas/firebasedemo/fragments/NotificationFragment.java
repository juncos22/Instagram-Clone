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
import java.util.Collections;
import java.util.List;

import dev.nicolas.firebasedemo.R;
import dev.nicolas.firebasedemo.adapter.NotificationAdapter;
import dev.nicolas.firebasedemo.model.Notification;

public class NotificationFragment extends Fragment {
    private RecyclerView recyclerNotifications;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        recyclerNotifications = view.findViewById(R.id.recycler_notifications);
        recyclerNotifications.setHasFixedSize(true);
        recyclerNotifications.setLayoutManager(new LinearLayoutManager(getContext()));

        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(getContext(), notificationList);

        recyclerNotifications.setAdapter(notificationAdapter);

        readNotifications();
        return view;
    }

    private void readNotifications() {
        FirebaseDatabase.getInstance().getReference().child("Notifications")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        notificationList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            notificationList.add(data.getValue(Notification.class));
                        }
                        Collections.reverse(notificationList);
                        notificationAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}