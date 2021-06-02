package com.demo.whatsapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
public class RequestsFragment extends Fragment {

    private View requestsFragmentView;
    private RecyclerView requestsList;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference chatRequestDatabaseReference, usersDatabaseReference, contactsDatabaseReference;
    private String currentUserID;

    public RequestsFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        requestsFragmentView = inflater.inflate(R.layout.fragment_requests, container, false);

        initializeFields();

        return requestsFragmentView;
    }

    private void initializeFields() {
        requestsList = requestsFragmentView.findViewById(R.id.chat_requests_list);
        requestsList.setLayoutManager(new LinearLayoutManager(getContext()));

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        chatRequestDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        contactsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Contacts");
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatRequestDatabaseReference.child(currentUserID), Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, RequestsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RequestsViewHolder holder, int position, @NonNull Contacts model) {
                holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);

                final String listUserId = getRef(position).getKey();

                DatabaseReference getTypeDatabaseReference = getRef(position).child("request_type").getRef();
                getTypeDatabaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String type = Objects.requireNonNull(snapshot.getValue()).toString();

                            if (type.equals("received")) {
                                usersDatabaseReference.child(listUserId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.hasChild("image")) {
                                            final String requestUserImage = Objects.requireNonNull(snapshot.child("image").getValue()).toString();

                                            Glide.with(Objects.requireNonNull(getContext()))
                                                    .load(requestUserImage)
                                                    .placeholder(R.drawable.person_photo)
                                                    .into(holder.userImage);
                                        }
                                        final String requestUserName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                                        final String requestUserStatus = Objects.requireNonNull(snapshot.child("status").getValue()).toString();

                                        holder.userName.setText(requestUserName);
                                        holder.userStatus.setText("wants to contact with you!");

                                        holder.itemView.setOnClickListener(view -> {
                                            CharSequence options1[] = new CharSequence[] {"Accept", "Cancel"};
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                            builder.setTitle(requestUserName + " Chat Request");
                                            builder.setItems(options1, (dialogInterface, i) -> {
                                                if (i == 0) {
                                                    contactsDatabaseReference.child(currentUserID).child(listUserId).child("Contact").setValue("Saved").addOnCompleteListener(task -> {
                                                        if (task.isSuccessful()) {
                                                            contactsDatabaseReference.child(listUserId).child(currentUserID).child("Contact").setValue("Saved").addOnCompleteListener(task1 -> {
                                                                if (task1.isSuccessful()) {
                                                                    chatRequestDatabaseReference.child(currentUserID).child(listUserId).removeValue().addOnCompleteListener(task11 -> {
                                                                        if (task11.isSuccessful()) {
                                                                            chatRequestDatabaseReference.child(listUserId).child(currentUserID).removeValue().addOnCompleteListener(task111 -> {
                                                                                if (task111.isSuccessful()) {
                                                                                    Toast.makeText(getContext(), "new contact added successfully!", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            });
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        }
                                                    });
                                                } else if (i == 1) {
                                                    chatRequestDatabaseReference.child(currentUserID).child(listUserId).removeValue().addOnCompleteListener(task -> {
                                                        if (task.isSuccessful()) {
                                                            chatRequestDatabaseReference.child(listUserId).child(currentUserID).removeValue().addOnCompleteListener(task12 -> {
                                                                if (task12.isSuccessful()) {
                                                                    Toast.makeText(getContext(), "Request cancelled successfully!", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                            builder.show();
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            } else if (type.equals("sent")) {
                                Button requestSentButton = holder.itemView.findViewById(R.id.request_accept_btn);
                                requestSentButton.setText("request sent");
                                requestSentButton.setPadding(30,0,30,0);

                                holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.INVISIBLE);

                                usersDatabaseReference.child(listUserId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.hasChild("image")) {
                                            final String requestUserImage = Objects.requireNonNull(snapshot.child("image").getValue()).toString();

                                            Glide.with(Objects.requireNonNull(getContext()))
                                                    .load(requestUserImage)
                                                    .placeholder(R.drawable.person_photo)
                                                    .into(holder.userImage);
                                        }
                                        final String requestUserName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                                        final String requestUserStatus = Objects.requireNonNull(snapshot.child("status").getValue()).toString();

                                        holder.userName.setText(requestUserName);
                                        holder.userStatus.setText("you have sent a request to " + requestUserName);

                                        holder.itemView.setOnClickListener(view -> {
                                            CharSequence options1[] = new CharSequence[] {"Cancel Chat Request"};
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                            builder.setTitle("Already Sent Request");
                                            builder.setItems(options1, (dialogInterface, i) -> {
                                                if (i == 0) {
                                                    chatRequestDatabaseReference.child(currentUserID).child(listUserId).removeValue().addOnCompleteListener(task -> {
                                                        if (task.isSuccessful()) {
                                                            chatRequestDatabaseReference.child(listUserId).child(currentUserID).removeValue().addOnCompleteListener(task12 -> {
                                                                if (task12.isSuccessful()) {
                                                                    Toast.makeText(getContext(), "You have cancelled tha chat request successfully!", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                            builder.show();
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                RequestsViewHolder holder = new RequestsViewHolder(view);
                return holder;
            }
        };

        requestsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userStatus;
        CircleImageView userImage;
        Button btnAccept, btnCancel;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            userImage = itemView.findViewById(R.id.user_profile_image);
            btnAccept = itemView.findViewById(R.id.request_accept_btn);
            btnCancel = itemView.findViewById(R.id.request_cancel_btn);
        }
    }
}