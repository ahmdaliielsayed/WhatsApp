package com.demo.whatsapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID, senderUserID, currentState;

    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessageRequestButton, declineMessageRequestButton;

    private DatabaseReference userDatabaseReference, chatRequestDatabaseReference, contactsDatabaseReference, notificationDatabaseReference;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();

        initializeComponents();

        retrieveUserInfo();
    }

    private void initializeComponents() {
        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_user_name);
        userProfileStatus = findViewById(R.id.visit_user_status);
        sendMessageRequestButton = findViewById(R.id.send_message_request_button);
        declineMessageRequestButton = findViewById(R.id.decline_message_request_button);

        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Contacts");
        currentState = "new";
        notificationDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Notifications");

        firebaseAuth = FirebaseAuth.getInstance();
        senderUserID = firebaseAuth.getCurrentUser().getUid();
    }

    private void retrieveUserInfo() {
        userDatabaseReference.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("image")) {
                    String userName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                    String userStatus = Objects.requireNonNull(snapshot.child("status").getValue()).toString();
                    String userImage = Objects.requireNonNull(snapshot.child("image").getValue()).toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    Glide.with(ProfileActivity.this)
                            .load(userImage)
                            .placeholder(R.drawable.person_photo)
                            .into(userProfileImage);

                    manageChatRequests();
                } else if (snapshot.exists()){
                    String userName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                    String userStatus = Objects.requireNonNull(snapshot.child("status").getValue()).toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manageChatRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void manageChatRequests() {

        chatRequestDatabaseReference.child(senderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(receiverUserID)) {
                    String requestType = snapshot.child(receiverUserID).child("request_type").getValue().toString();

                    if (requestType.equals("sent")) {
                        currentState = "request_sent";
                        sendMessageRequestButton.setText("Cancel Chat Request");
                    } else if (requestType.equals("received")) {
                        currentState = "request_received";
                        sendMessageRequestButton.setText("Accept Chat Request");

                        declineMessageRequestButton.setVisibility(View.VISIBLE);
                        declineMessageRequestButton.setOnClickListener(view -> cancelChatRequest());
                    }
                } else {
                    contactsDatabaseReference.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(receiverUserID)) {
                                currentState = "friends";
                                sendMessageRequestButton.setText("Remove this Contact");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (senderUserID.equals(receiverUserID)) {
            sendMessageRequestButton.setVisibility(View.GONE);
        } else {
            sendMessageRequestButton.setOnClickListener(view -> {
                sendMessageRequestButton.setEnabled(false);

                if (currentState.equals("new")) {
                    sendChatRequest();
                }

                if (currentState.equals("request_sent")) {
                    cancelChatRequest();
                }

                if (currentState.equals("request_received")) {
                    acceptChatRequest();
                }

                if (currentState.equals("friends")) {
                    removeSpecificContact();
                }
            });
        }
    }

    private void sendChatRequest() {
        chatRequestDatabaseReference.child(senderUserID).child(receiverUserID).child("request_type").setValue("sent").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                chatRequestDatabaseReference.child(receiverUserID).child(senderUserID).child("request_type").setValue("received").addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        HashMap<String, String> chatNotificationMap = new HashMap<>();
                        chatNotificationMap.put("from", senderUserID);
                        chatNotificationMap.put("type", "request");

                        notificationDatabaseReference.child(receiverUserID).push().setValue(chatNotificationMap).addOnCompleteListener(task2 -> {
                            if (task2.isSuccessful()) {
                                sendMessageRequestButton.setEnabled(true);
                                currentState = "request_sent";
                                sendMessageRequestButton.setText("Cancel Chat Request");
                            }
                        });
                    }
                });
            }
        });
    }

    private void cancelChatRequest() {
        chatRequestDatabaseReference.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                chatRequestDatabaseReference.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        sendMessageRequestButton.setEnabled(true);
                        currentState = "new";
                        sendMessageRequestButton.setText("Send Message");

                        declineMessageRequestButton.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    private void acceptChatRequest() {
        contactsDatabaseReference.child(senderUserID).child(receiverUserID).child("Contacts").setValue("Saved").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                contactsDatabaseReference.child(receiverUserID).child(senderUserID).child("Contacts").setValue("Saved").addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        chatRequestDatabaseReference.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(task11 -> {
                            if (task11.isSuccessful()) {
                                chatRequestDatabaseReference.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(task111 -> {
                                    sendMessageRequestButton.setEnabled(true);
                                    currentState = "friends";
                                    sendMessageRequestButton.setText("Remove this Contact");
                                    declineMessageRequestButton.setVisibility(View.GONE);
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    private void removeSpecificContact() {
        contactsDatabaseReference.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                contactsDatabaseReference.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        sendMessageRequestButton.setEnabled(true);
                        currentState = "new";
                        sendMessageRequestButton.setText("Send Message");

                        declineMessageRequestButton.setVisibility(View.GONE);
                    }
                });
            }
        });
    }
}