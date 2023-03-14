package entelect.training.incubator.spring.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import entelect.training.incubator.spring.notification.sms.client.impl.MoloCellSmsClient;
import entelect.training.incubator.spring.loyalty.server.RewardsServiceImpl;

@SpringBootApplication
public class BookingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookingServiceApplication.class, args);
    }

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public MoloCellSmsClient getMoloCellSmsClient() {
        return new MoloCellSmsClient();
    }

    @Bean
    public RewardsServiceImpl getRewardsServiceImpl() {
        return new RewardsServiceImpl();
    }

}
