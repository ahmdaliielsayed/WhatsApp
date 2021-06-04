package com.demo.whatsapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.GroupChatViewHolder> {

    private Context context;
    private List<GroupMessages> userGroupMessageList;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private String currentGroupName;

    public GroupChatAdapter(List<GroupMessages> userGroupMessageList, Context context, String currentGroupName) {
        this.userGroupMessageList = userGroupMessageList;
        this.context = context;
        this.currentGroupName = currentGroupName;
    }

    @NonNull
    @Override
    public GroupChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_group_message_layout, parent, false);

        firebaseAuth = FirebaseAuth.getInstance();

        return new GroupChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupChatViewHolder holder, int position) {
        String messageSenderID = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        GroupMessages groupMessages = userGroupMessageList.get(position);
        String userID = groupMessages.getUserID();

        holder.getReceiverMessageText().setVisibility(View.GONE);
        holder.getSenderMessageText().setVisibility(View.GONE);

        if (messageSenderID.equals(userID)) {
            holder.getSenderMessageText().setVisibility(View.VISIBLE);
            holder.getSenderMessageText().setBackgroundResource(R.drawable.sender_messages_layout);
            holder.getTxtSenderName().setText(groupMessages.getName());
            holder.getTxtSenderMessage().setText(groupMessages.getMessage());
            holder.getTxtSenderDate().setText(groupMessages.getDate());
            holder.getTxtSenderTime().setText(groupMessages.getTime());

            holder.itemView.setOnClickListener(view -> {
                CharSequence[] options = new CharSequence[]{
                        "Yes", "No"
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                builder.setTitle("Delete Message?");
                builder.setItems(options, (dialogInterface, i) -> {
                    if (i == 0) {
                        deleteSentMessage(position, holder);
                    }
                });
                builder.show();
            });
        } else {
            holder.getReceiverMessageText().setVisibility(View.VISIBLE);
            holder.getReceiverMessageText().setBackgroundResource(R.drawable.receiver_messages_layout);
            holder.getTxtReceiverName().setText(groupMessages.getName());
            holder.getTxtReceiverMessage().setText(groupMessages.getMessage());
            holder.getTxtReceiverDate().setText(groupMessages.getDate());
            holder.getTxtReceiverTime().setText(groupMessages.getTime());
        }
    }

    @Override
    public int getItemCount() {
        return userGroupMessageList.size();
    }

    private void deleteSentMessage(final int position, final GroupChatAdapter.GroupChatViewHolder holder) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Groups").child(currentGroupName).child(userGroupMessageList.get(position).getMessageID()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(holder.itemView.getContext(), "Deleted Successfully!", Toast.LENGTH_SHORT).show();
                userGroupMessageList.remove(position);
                notifyDataSetChanged();
            } else {
                Toast.makeText(context, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class GroupChatViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout receiverMessageText, senderMessageText;
        private TextView txtReceiverName, txtReceiverMessage, txtReceiverDate, txtReceiverTime,
                txtSenderName, txtSenderMessage, txtSenderDate, txtSenderTime;

        public GroupChatViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public ConstraintLayout getReceiverMessageText() {
            if (receiverMessageText == null) {
                receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            }
            return receiverMessageText;
        }

        public ConstraintLayout getSenderMessageText() {
            if (senderMessageText == null) {
                senderMessageText = itemView.findViewById(R.id.sender_message_text);
            }
            return senderMessageText;
        }

        public TextView getTxtReceiverName() {
            if (txtReceiverName == null) {
                txtReceiverName = itemView.findViewById(R.id.txt_receiver_name);
            }
            return txtReceiverName;
        }

        public TextView getTxtReceiverMessage() {
            if (txtReceiverMessage == null) {
                txtReceiverMessage = itemView.findViewById(R.id.txt_receiver_message);
            }
            return txtReceiverMessage;
        }

        public TextView getTxtReceiverDate() {
            if (txtReceiverDate == null) {
                txtReceiverDate = itemView.findViewById(R.id.txt_receiver_date);
            }
            return txtReceiverDate;
        }

        public TextView getTxtReceiverTime() {
            if (txtReceiverTime == null) {
                txtReceiverTime = itemView.findViewById(R.id.txt_receiver_time);
            }
            return txtReceiverTime;
        }

        public TextView getTxtSenderName() {
            if (txtSenderName == null) {
                txtSenderName = itemView.findViewById(R.id.txt_sender_name);
            }
            return txtSenderName;
        }

        public TextView getTxtSenderMessage() {
            if (txtSenderMessage == null) {
                txtSenderMessage = itemView.findViewById(R.id.txt_sender_message);
            }
            return txtSenderMessage;
        }

        public TextView getTxtSenderDate() {
            if (txtSenderDate == null) {
                txtSenderDate = itemView.findViewById(R.id.txt_sender_date);
            }
            return txtSenderDate;
        }

        public TextView getTxtSenderTime() {
            if (txtSenderTime == null) {
                txtSenderTime = itemView.findViewById(R.id.txt_sender_time);
            }
            return txtSenderTime;
        }
    }
}
