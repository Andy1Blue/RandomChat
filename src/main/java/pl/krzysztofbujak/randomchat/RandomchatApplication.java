package pl.krzysztofbujak.randomchat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableWebSocket
public class RandomchatApplication {

	public static void main(String[] args) {
		SpringApplication.run(RandomchatApplication.class, args);
	}
}
