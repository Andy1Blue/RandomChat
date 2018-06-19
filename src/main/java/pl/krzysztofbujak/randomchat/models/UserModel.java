package pl.krzysztofbujak.randomchat.models;

import lombok.Data;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Data
public class UserModel {
    private String nickname;
    private WebSocketSession userSession;

    public UserModel(WebSocketSession userSession) {
        this.userSession = userSession;
    }

    public void sendMessage(TextMessage textMessage) throws IOException {
        userSession.sendMessage(textMessage);
    }
}