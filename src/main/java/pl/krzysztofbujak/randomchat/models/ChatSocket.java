package pl.krzysztofbujak.randomchat.models;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;

@Component
public class ChatSocket extends TextWebSocketHandler implements WebSocketConfigurer {

    private List<WebSocketSession> userList = new ArrayList<>();
    private Queue<String> lastTenMessages = new ArrayDeque<>();

    private TextMessage getActualTime() {
        Calendar d = Calendar.getInstance();
        return new TextMessage("(" + d.get(Calendar.HOUR) + ":" + d.get(Calendar.MINUTE) + ":" + d.get(Calendar.SECOND) + ") ");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        addMessageToQue(getActualTime().getPayload() + message.getPayload());
        userList.forEach(s -> {
            try {
                s.sendMessage(new TextMessage(getActualTime().getPayload()+message.getPayload()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        userList.remove(session);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        userList.add(session);

        lastTenMessages.forEach(s -> {
            try {
                session.sendMessage(new TextMessage(s));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(this, "/room")
                .setAllowedOrigins("*");
    }

    private void addMessageToQue(String message) {
        if (lastTenMessages.size() >= 10) {
            lastTenMessages.poll();
        }

        lastTenMessages.offer(message);
    }
}
