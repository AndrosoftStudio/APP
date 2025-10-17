package com.example.offlinellama;

class ChatMessage {
    private final String content;
    private final boolean fromUser;

    ChatMessage(String content, boolean fromUser) {
        this.content = content;
        this.fromUser = fromUser;
    }

    String getContent() {
        return content;
    }

    boolean isFromUser() {
        return fromUser;
    }
}
