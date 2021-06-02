package com.demo.whatsapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context context;
    private List<Messages> userMessageList;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    public MessageAdapter(List<Messages> userMessageList, Context context) {
        this.userMessageList = userMessageList;
        this.context = context;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout, parent, false);

        firebaseAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String messageSenderID = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        Messages messages = userMessageList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("image")) {
                    String receiverImage = Objects.requireNonNull(snapshot.child("image").getValue()).toString();
                    Glide.with(context)
                            .load(receiverImage)
                            .placeholder(R.drawable.person_photo)
                            .into(holder.getReceiverProfileImage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.getReceiverMessageText().setVisibility(View.GONE);
        holder.getReceiverProfileImage().setVisibility(View.GONE);
        holder.getSenderMessageText().setVisibility(View.GONE);
        holder.getMessageSenderPicture().setVisibility(View.GONE);
        holder.getMessageReceiverPicture().setVisibility(View.GONE);

        if (fromMessageType.equals("text")) {
            if (fromUserID.equals(messageSenderID)) {
                holder.getSenderMessageText().setVisibility(View.VISIBLE);
                holder.getSenderMessageText().setBackgroundResource(R.drawable.sender_messages_layout);
                holder.getSenderMessageText().setText(messages.getMessage() + "\n\n" + messages.getTime() + " - " + messages.getDate());
            } else {
                holder.getReceiverMessageText().setVisibility(View.VISIBLE);
                holder.getReceiverProfileImage().setVisibility(View.VISIBLE);

                holder.getReceiverMessageText().setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.getReceiverMessageText().setText(messages.getMessage() + "\n\n" + messages.getTime() + " - " + messages.getDate());
            }
        } else if (fromMessageType.equals("image")) {
            if (fromUserID.equals(messageSenderID)) {
                holder.getMessageSenderPicture().setVisibility(View.VISIBLE);

                Glide.with(context)
                        .load(messages.getMessage())
                        .placeholder(R.drawable.person_photo)
                        .into(holder.getMessageSenderPicture());
            } else {
                holder.getReceiverProfileImage().setVisibility(View.VISIBLE);
                holder.getMessageReceiverPicture().setVisibility(View.VISIBLE);

                Glide.with(context)
                        .load(messages.getMessage())
                        .placeholder(R.drawable.person_photo)
                        .into(holder.getMessageReceiverPicture());
            }
        } else if (fromMessageType.equals("pdf") || (fromMessageType.equals("docx"))) {
            if (fromUserID.equals(messageSenderID)) {
                holder.getMessageSenderPicture().setVisibility(View.VISIBLE);
                if (fromMessageType.equals("pdf")) {
                    holder.getMessageSenderPicture().setBackgroundResource(R.drawable.pdf);
                } else {
                    holder.getMessageSenderPicture().setBackgroundResource(R.drawable.ms_word);
                }
            } else {
                holder.getReceiverProfileImage().setVisibility(View.VISIBLE);
                holder.getMessageReceiverPicture().setVisibility(View.VISIBLE);
                if (fromMessageType.equals("pdf")) {
                    holder.getMessageReceiverPicture().setBackgroundResource(R.drawable.pdf);
                } else {
                    holder.getMessageReceiverPicture().setBackgroundResource(R.drawable.ms_word);
                }
            }
        }

        if (fromUserID.equals(messageSenderID)) {
            holder.itemView.setOnClickListener(view -> {
                if (userMessageList.get(position).getType().equals("pdf") || userMessageList.get(position).getType().equals("docx")) {
                    CharSequence options[] = new CharSequence[]{
                            "Delete for me", "Download and View this document", "Cancel", "Delete for everyone"
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message?");
                    builder.setItems(options, (dialogInterface, i) -> {
                        if (i == 0) {
                            deleteSentMessage(position, holder);
                        } else if (i == 1) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                            holder.itemView.getContext().startActivity(intent);
                        } else if (i == 3) {
                            deleteMessageForEveryOne(position, holder);
                        }
                    });
                    builder.show();
                } else if (userMessageList.get(position).getType().equals("text")) {
                    CharSequence options[] = new CharSequence[]{
                            "Delete for me", "Cancel", "Delete for everyone"
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message?");
                    builder.setItems(options, (dialogInterface, i) -> {
                        if (i == 0) {
                            deleteSentMessage(position, holder);
                        }else if (i == 2) {
                            deleteMessageForEveryOne(position, holder);
                        }
                    });
                    builder.show();
                } else if (userMessageList.get(position).getType().equals("image")) {
                    CharSequence options[] = new CharSequence[]{
                            "Delete for me", "View this image", "Cancel", "Delete for everyone"
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message?");
                    builder.setItems(options, (dialogInterface, i) -> {
                        if (i == 0) {
                            deleteSentMessage(position, holder);
                        } else if (i == 1) {
                            Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                            intent.putExtra("url", userMessageList.get(position).getMessage());
                            holder.itemView.getContext().startActivity(intent);
                        } else if (i == 3) {
                            deleteMessageForEveryOne(position, holder);
                        }
                    });
                    builder.show();
                }
            });
        } else {
            holder.itemView.setOnClickListener(view -> {
                if (userMessageList.get(position).getType().equals("pdf") || userMessageList.get(position).getType().equals("docx")) {
                    CharSequence options[] = new CharSequence[]{
                            "Delete for me", "Download and View this document", "Cancel"
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message?");
                    builder.setItems(options, (dialogInterface, i) -> {
                        if (i == 0) {
                            deleteReceiveMessage(position, holder);
                        } else if (i == 1) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                            holder.itemView.getContext().startActivity(intent);
                        }
                    });
                    builder.show();
                } else if (userMessageList.get(position).getType().equals("text")) {
                    CharSequence options[] = new CharSequence[]{
                            "Delete for me", "Cancel"
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message?");
                    builder.setItems(options, (dialogInterface, i) -> {
                        if (i == 0) {
                            deleteReceiveMessage(position, holder);
                        }
                    });
                    builder.show();
                } else if (userMessageList.get(position).getType().equals("image")) {
                    CharSequence options[] = new CharSequence[]{
                            "Delete for me", "View this image", "Cancel"
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Delete Message?");
                    builder.setItems(options, (dialogInterface, i) -> {
                        if (i == 0) {
                            deleteReceiveMessage(position, holder);
                        } else if (i == 1) {
                            Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                            intent.putExtra("url", userMessageList.get(position).getMessage());
                            holder.itemView.getContext().startActivity(intent);
                        }
                    });
                    builder.show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }

    private void deleteSentMessage(final int position, final MessageViewHolder holder) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Messages").child(userMessageList.get(position).getFrom()).child(userMessageList.get(position).getTo()).child(userMessageList.get(position).getMessageID()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(holder.itemView.getContext(), "Deleted Successfully!", Toast.LENGTH_SHORT).show();
                userMessageList.remove(position);
                notifyDataSetChanged();
            } else {
                Toast.makeText(context, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteReceiveMessage(final int position, final MessageViewHolder holder) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Messages").child(userMessageList.get(position).getTo()).child(userMessageList.get(position).getFrom()).child(userMessageList.get(position).getMessageID()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(holder.itemView.getContext(), "Deleted Successfully!", Toast.LENGTH_SHORT).show();
                userMessageList.remove(position);
                notifyDataSetChanged();
            } else {
                Toast.makeText(context, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteMessageForEveryOne(final int position, final MessageViewHolder holder) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Messages").child(userMessageList.get(position).getTo()).child(userMessageList.get(position).getFrom()).child(userMessageList.get(position).getMessageID()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                databaseReference.child("Messages").child(userMessageList.get(position).getFrom()).child(userMessageList.get(position).getTo()).child(userMessageList.get(position).getMessageID()).removeValue().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(holder.itemView.getContext(), "Deleted Successfully!", Toast.LENGTH_SHORT).show();
                        userMessageList.remove(position);
                        notifyDataSetChanged();
                    } else {
                        Toast.makeText(context, "Error: " + Objects.requireNonNull(task1.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(context, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        private TextView senderMessageText, receiverMessageText;
        private CircleImageView receiverProfileImage;
        private ImageView messageSenderPicture, messageReceiverPicture;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public TextView getSenderMessageText() {
            if (senderMessageText == null) {
                senderMessageText = itemView.findViewById(R.id.sender_message_text);
            }
            return senderMessageText;
        }

        public TextView getReceiverMessageText() {
            if (receiverMessageText == null) {
                receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            }
            return receiverMessageText;
        }

        public CircleImageView getReceiverProfileImage() {
            if (receiverProfileImage == null) {
                receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            }
            return receiverProfileImage;
        }

        public ImageView getMessageSenderPicture() {
            if (messageSenderPicture == null) {
                messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
            }
            return messageSenderPicture;
        }

        public ImageView getMessageReceiverPicture() {
            if (messageReceiverPicture == null) {
                messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            }
            return messageReceiverPicture;
        }
    }
}