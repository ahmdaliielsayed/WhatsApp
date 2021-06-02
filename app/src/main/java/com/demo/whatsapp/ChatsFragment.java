package com.demo.whatsapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View privateChatView;
    private RecyclerView chatsList;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference chatsDatabaseReference, usersDatabaseReference;
    private String currentUserID;

    public ChatsFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        privateChatView = inflater.inflate(R.layout.fragment_chats, container, false);

        initializeFields();

        return privateChatView;
    }

    private void initializeFields() {
        chatsList = privateChatView.findViewById(R.id.chats_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        chatsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatsDatabaseReference, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ChatsViewHolder holder, int position, @NonNull Contacts model) {
                final String userIDs = getRef(position).getKey();
                final String[] profileImage = {"default_image"};

                usersDatabaseReference.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            if (snapshot.hasChild("image")) {
                                profileImage[0] = Objects.requireNonNull(snapshot.child("image").getValue()).toString();

                                try {
                                    Glide.with(Objects.requireNonNull(getContext()))
                                            .load(profileImage[0])
                                            .placeholder(R.drawable.person_photo)
                                            .into(holder.userImage);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            String profileName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                            String profileStatus = Objects.requireNonNull(snapshot.child("status").getValue()).toString();

                            holder.userName.setText(profileName);
                            holder.userStatus.setText("last seen: " + "\n" + "Date " + "Time ");

                            if (snapshot.child("user_state").hasChild("status")) {
                                String status = snapshot.child("user_state").child("status").getValue().toString();
                                String date = snapshot.child("user_state").child("date").getValue().toString();
                                String time = snapshot.child("user_state").child("time").getValue().toString();

                                if (status.equals("online")) {
                                    holder.userStatus.setText("online");
                                } else if (status.equals("offline")) {
                                    holder.userStatus.setText("last seen: " + date + " " + time);
                                }
                            } else {
                                holder.userStatus.setText("offline");
                            }

                            holder.itemView.setOnClickListener(view -> {
                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("visit_user_id", userIDs);
                                chatIntent.putExtra("visit_user_name", profileName);
                                chatIntent.putExtra("visit_user_image", profileImage[0]);
                                startActivity(chatIntent);
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                return new ChatsViewHolder(view);
            }
        };

        chatsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userStatus;
        CircleImageView userImage;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            userImage = itemView.findViewById(R.id.user_profile_image);
        }
    }
}