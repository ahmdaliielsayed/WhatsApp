package com.demo.whatsapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference, groupNameReference, groupMessageKeyReference;

    private final List<GroupMessages> groupMessagesList = new ArrayList<>();
    private RecyclerView userGroupMessagesList;
    private LinearLayoutManager linearLayoutManager;
    private GroupChatAdapter groupChatAdapter;

    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        initializeFields();

        getUserInfo();

        sendMessageButton.setOnClickListener(view -> {
            saveMessageInfoToDatabase();

            userMessageInput.setText("");
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        groupMessagesList.clear();
        groupChatAdapter.notifyDataSetChanged();
        
        groupNameReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                GroupMessages groupMessages = snapshot.getValue(GroupMessages.class);
                groupMessagesList.add(groupMessages);
                groupChatAdapter.notifyDataSetChanged();

                userGroupMessagesList.smoothScrollToPosition(Objects.requireNonNull(userGroupMessagesList.getAdapter()).getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initializeFields() {
        toolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(currentGroupName);

        sendMessageButton = findViewById(R.id.send_message_button);
        userMessageInput = findViewById(R.id.input_group_message);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        groupNameReference = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        groupChatAdapter = new GroupChatAdapter(groupMessagesList, getApplicationContext(), currentGroupName);
        userGroupMessagesList = findViewById(R.id.private_group_messages_list_of_user);
        linearLayoutManager = new LinearLayoutManager(this);
        userGroupMessagesList.setLayoutManager(linearLayoutManager);
        userGroupMessagesList.setAdapter(groupChatAdapter);
    }

    private void getUserInfo() {
        databaseReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUserName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void saveMessageInfoToDatabase() {
        String message = userMessageInput.getText().toString();
        String messageKey = groupNameReference.push().getKey();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please, write message first!", Toast.LENGTH_SHORT).show();
        } else {
            Calendar calendarMessage = Calendar.getInstance();

            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormat.format(calendarMessage.getTime());

            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calendarMessage.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            groupNameReference.updateChildren(groupMessageKey);

            groupMessageKeyReference = groupNameReference.child(messageKey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
                messageInfoMap.put("name", currentUserName);
                messageInfoMap.put("userID", Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid());
                messageInfoMap.put("messageID", messageKey);
                messageInfoMap.put("message", message);
                messageInfoMap.put("date", currentDate);
                messageInfoMap.put("time", currentTime);
            groupMessageKeyReference.updateChildren(messageInfoMap);
        }
    }
}