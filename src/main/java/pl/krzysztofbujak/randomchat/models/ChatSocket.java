package pl.krzysztofbujak.randomchat.models;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

@Component
public class ChatSocket extends TextWebSocketHandler implements WebSocketConfigurer {

    private List<UserModel> userList = new ArrayList<>();
    private Queue<String> lastTenMessages = new ArrayDeque<>();

    private TextMessage getActualTime() {
        String hour = "";
        String minute = "";
        String secound = "";
        LocalDateTime timePoint = LocalDateTime.now(
        );
        int hLength = String.valueOf(timePoint.getHour()).length();
        if (hLength == 1){
            hour = "0" + String.valueOf(timePoint.getHour());
        } else {
            hour = String.valueOf(timePoint.getHour());
        }
        int mLength = String.valueOf(timePoint.getMinute()).length();
        if (mLength == 1){
            minute = "0" + String.valueOf(timePoint.getMinute());
        } else {
            minute = String.valueOf(timePoint.getMinute());
        }
        int sLength = String.valueOf(timePoint.getSecond()).length();
        if (sLength == 1){
            secound = "0" + String.valueOf(timePoint.getSecond());
        } else {
            secound = String.valueOf(timePoint.getSecond());
        }
        return new TextMessage("(" + hour + ":" + minute + ":" + secound + ") ");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        UserModel sender = findBySession(session);

        if(message.getPayload().startsWith("nickname:")){
            if(sender.getNickname() == null){
                sender.setNickname(message.getPayload().replace("nickname:", ""));
            }else{
                sender.sendMessage(new TextMessage("Nie możesz zmienić nicku więcej razy!"));
            }
            return;
        }

        if(sender.getNickname() == null){
            sender.sendMessage(new TextMessage("Najpierw ustal nick!"));
            return;
        }

        addMessageToQue(getActualTime().getPayload() + sender.getNickname() + ": " + message.getPayload());
        userList.forEach(s -> {
            try {
                s.sendMessage(new TextMessage(getActualTime().getPayload() + sender.getNickname() + ": " + message.getPayload()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    private void sendMessgaeToAllWithoutSender(String message, UserModel sender){
        userList.stream()
                .filter(s -> !s.equals(sender))
                .forEach(s -> {
                    try {
                        s.sendMessage(new TextMessage(message));

                    } catch (IOException e){
                        e.printStackTrace();
                    }
                });
    }

    private UserModel findBySession(WebSocketSession webSocketSession){
        return userList.stream()
                .filter(s -> s.getUserSession().getId().equals(webSocketSession.getId()))
                .findAny()
                .orElseThrow(IllegalStateException::new);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        UserModel userWhoIsExiting = findBySession(session);
        userList.remove(userWhoIsExiting);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        userList.add(new UserModel(session));

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
        if (lastTenMessages.size() == 0) {
            lastTenMessages.offer("Ostatnie 10 wiadomości:");
        }
        if (lastTenMessages.size() >= 10) {
            lastTenMessages.poll();
        }
        lastTenMessages.offer(message);
    }
}
