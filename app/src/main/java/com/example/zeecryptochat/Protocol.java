package com.example.zeecryptochat;

import com.google.gson.Gson;

public class Protocol {
    public final static int GROUP_CHAT = 1;

    public final static int USER_STATUS = 1; // 1 - статус пользователя (онлайн или оффлайн)
    public final static int MESSAGE = 2; // 2 - текст
    public final static int USER_NAME = 3; // 3 - имя пользователя

    static class UserName {
        private String name;

        public UserName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    // для входящих и исходящих
    static class Message {
        private long sender; // отправитель
        private String encodedText; // текст сообщения (шифрован)
        private long receiver; // получатель

        public Message(String encodedText) {
            this.encodedText = encodedText;
        }

        public long getSender() {
            return sender;
        }

        public void setSender(long sender) {
            this.sender = sender;
        }

        public String getEncodedText() {
            return encodedText;
        }

        public void setEncodedText(String encodedText) {
            this.encodedText = encodedText;
        }

        public long getReceiver() {
            return receiver;
        }

        public void setReceiver(long receiver) {
            this.receiver = receiver;
        }
    }

    static class User {
        private String name;
        private long id;

        public User() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }
    }

    static class UserStatus {
        private boolean connected;
        private User user;

        public UserStatus() {
        }

        public boolean isConnected() {
            return connected;
        }

        public void setConnected(boolean connected) {
            this.connected = connected;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }

    //2{ sender: 1, receiver: 4, encodedText "text"}" -> Message
    public static Message unpackMessage(String json) {
        Gson g = new Gson();
        return g.fromJson(json.substring(1), Message.class);
    }

    public static UserStatus unpackStatus (String json) {
        Gson g = new Gson();
        return g.fromJson(json.substring(1), UserStatus.class);
    }

    public static String packMessage(Message message) {
        Gson g = new Gson();
        return MESSAGE + g.toJson(message); // "2{ sender: 1, receiver: 4, encodedText "text"}"
    }

    // UserName -> "3{ name: \"Zee\"}"
    public static String packName(UserName name) {
        Gson g = new Gson();
        return USER_NAME + g.toJson(name); // "3{ name: \"Zee\"}"
    }

    public static int getType(String json) {
        if (json == null || json.length() == 0) {
            return -1;
        }
        return Integer.parseInt(json.substring(0, 1));
    }
}
