package pl.shakhner.PetrushkaProductsBot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PetrushkaProductsBotApplication {

	public static void main(String[] args) {
		System.setProperty("user.timezone", "Europe/Minsk");
		SpringApplication.run(PetrushkaProductsBotApplication.class, args);
	}

}
