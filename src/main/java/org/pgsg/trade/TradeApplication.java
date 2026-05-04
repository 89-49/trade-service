package org.pgsg.trade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaAuditing
@EntityScan(basePackages = {
		"org.pgsg.trade",
		"org.pgsg.common.domain"
})
@EnableJpaRepositories(basePackages = {
		"org.pgsg.trade.infrastructure.persistence.repository",
		"org.pgsg.common.domain"
})
public class TradeApplication {

	public static void main(String[] args) {
		SpringApplication.run(TradeApplication.class, args);
	}

}
