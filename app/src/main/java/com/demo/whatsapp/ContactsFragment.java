package com.demo.whatsapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
public class ContactsFragment extends Fragment {

    private View contactsView;
    private RecyclerView contactsList;
    private DatabaseReference contactsDatabaseReference, usersDatabaseReference;
    private FirebaseAuth firebaseAuth;
    private String currentUserID;

    public ContactsFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contactsView = inflater.inflate(R.layout.fragment_contacts, container, false);

        initializeComponents();

        return contactsView;
    }

    private void initializeComponents() {
        contactsList = contactsView.findViewById(R.id.contacts_list);
        contactsList.setLayoutManager(new LinearLayoutManager(getContext()));

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        contactsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsDatabaseReference, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ContactsViewHolder holder, int position, @NonNull Contacts model) {
                String userID = getRef(position).getKey();

                usersDatabaseReference.child(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            if (snapshot.child("user_state").hasChild("status")) {
                                String status = snapshot.child("user_state").child("status").getValue().toString();
                                String date = snapshot.child("user_state").child("date").getValue().toString();
                                String time = snapshot.child("user_state").child("time").getValue().toString();

                                if (status.equals("online")) {
                                    holder.getOnlineIcon().setVisibility(View.VISIBLE);
                                } else if (status.equals("offline")){
                                    holder.getOnlineIcon().setVisibility(View.INVISIBLE);
                                }
                            } else {
                                holder.getOnlineIcon().setVisibility(View.INVISIBLE);
                            }

                            if (snapshot.hasChild("image")) {
                                String profileImage = Objects.requireNonNull(snapshot.child("image").getValue()).toString();

                                try {
                                    Glide.with(Objects.requireNonNull(getContext()))
                                            .load(profileImage)
                                            .placeholder(R.drawable.person_photo)
                                            .into(holder.getUserImage());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            String profileName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                            String profileStatus = Objects.requireNonNull(snapshot.child("status").getValue()).toString();

                            holder.getUserName().setText(profileName);
                            holder.getUserStatus().setText(profileStatus);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                ContactsViewHolder contactsViewHolder = new ContactsViewHolder(view);
                return contactsViewHolder;
            }
        };

        contactsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {

        private TextView userName, userStatus;
        private CircleImageView userImage;
        private ImageView onlineIcon;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public TextView getUserName() {
            if (userName == null) {
                userName = itemView.findViewById(R.id.user_profile_name);
            }
            return userName;
        }

        public TextView getUserStatus() {
            if (userStatus == null) {
                userStatus = itemView.findViewById(R.id.user_status);
            }
            return userStatus;
        }

        public CircleImageView getUserImage() {
            if (userImage == null) {
                userImage = itemView.findViewById(R.id.user_profile_image);
            }
            return userImage;
        }

        public ImageView getOnlineIcon() {
            if (onlineIcon == null) {
                onlineIcon = itemView.findViewById(R.id.user_online_status);
            }
            return onlineIcon;
        }
    }
}