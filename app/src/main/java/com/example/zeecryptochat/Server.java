package com.example.zeecryptochat;

import android.util.Log;
import android.util.Pair;


import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Server {

    Map<Long, String> names = new ConcurrentHashMap<>();
    private WebSocketClient client;
    private Consumer<Pair<String, String>> onMessageReceived;

    public Server(Consumer<Pair<String, String>> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    public void connect() {
        URI address;
        try {
            address = new URI("ws://35.214.3.133:8881");
        } catch (URISyntaxException e) {
            Log.e("SERVER", "Cant connect to server", e);
            return;
        }
        client = new WebSocketClient(address) {
            // При подключении
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                Log.i("SERVER", "Connection to server is open");
            }

            @Override
            public void onMessage(String message) {
                Log.i("SERVER", "Got message from server: " + message);
                // При поступлении сообщения с сервера
                int type = Protocol.getType(message);
                if (type == Protocol.USER_STATUS) {
                    // обработать факт подключения или отключения пользователя
                    userStatusChanged(message);
                }
                if (type == Protocol.MESSAGE) {
                    // показать сообщение на экране
                    displayIncomingMessage(message);
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.i("SERVER", "Server connection closed");
                // При закрытии соединения
            }

            @Override
            public void onError(Exception ex) {
                Log.i("SERVER", "Error occured: " + ex.getMessage());
                // При ошибке
            }
        };
        client.connect();
    }


    private void displayIncomingMessage(String json) {
        Protocol.Message message = Protocol.unpackMessage(json);
        String name = names.get(message.getSender());
        if (name == null) {
            name = "Unnamed";
        }
        String text = message.getEncodedText();
        try {
            text = Crypto.decrypt(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        onMessageReceived.accept(new Pair<>(name, text));
    }

    private void userStatusChanged(String json) {
        Protocol.UserStatus userStatus = Protocol.unpackStatus(json);
        if (userStatus.isConnected()) {
            names.put(userStatus.getUser().getId(), userStatus.getUser().getName());
        } else {
            names.remove(userStatus.getUser().getId(), userStatus.getUser().getName());
        }
    }

    public void sendMessage(String message) {
        if (client == null || !client.isOpen()){
            return;
        }
        try {
            message = Crypto.encrypt(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Protocol.Message m = new Protocol.Message(message);
        m.setReceiver(Protocol.GROUP_CHAT);
        String packedMessage = Protocol.packMessage(m);
        Log.i("SERVER", "Sending message: " + packedMessage);

        client.send(packedMessage);
    }

    public void sendUserName(String name){
        String myName = Protocol.packName(new Protocol.UserName(name));
        Log.i("SERVER", "Sending name to server: " + myName);
        client.send(myName);
    }
}
