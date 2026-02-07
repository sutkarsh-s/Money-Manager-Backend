package in.utkarshsingh.money.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MoneyManagerCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneyManagerCoreApplication.class, args);
	}

}
