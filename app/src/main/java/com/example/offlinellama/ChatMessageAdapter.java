package com.example.offlinellama;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

class ChatMessageAdapter extends ArrayAdapter<ChatMessage> {

    ChatMessageAdapter(@NonNull Context context, @NonNull List<ChatMessage> messages) {
        super(context, 0, messages);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_message, parent, false);
        }

        ChatMessage message = getItem(position);
        TextView textView = view.findViewById(R.id.txtMessage);
        LinearLayout container = (LinearLayout) view;

        if (message != null) {
            textView.setText(message.getContent());
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) textView.getLayoutParams();
            if (params == null) {
                params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }

            if (message.isFromUser()) {
                params.gravity = android.view.Gravity.END;
                textView.setBackgroundResource(R.drawable.message_background_user);
                container.setGravity(android.view.Gravity.END);
            } else {
                params.gravity = android.view.Gravity.START;
                textView.setBackgroundResource(R.drawable.message_background_assistant);
                container.setGravity(android.view.Gravity.START);
            }
            params.setMargins(0, 0, 0, 0);
            textView.setLayoutParams(params);
        }

        return view;
    }
}
