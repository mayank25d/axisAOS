package helloworld;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "helloworld")
public class AOSApplication {

  public static void main(String[] args) {
    SpringApplication.run(AOSApplication.class, args);
  }

}
