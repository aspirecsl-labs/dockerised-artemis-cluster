package com.demo.artemis;

import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@SpringBootApplication
public class ArtemisConsumerApplication implements CommandLineRunner {

    private final Queue queue;
    private final Connection connection;

    public ArtemisConsumerApplication() throws JMSException {
        queue = ActiveMQJMSClient.createQueue("example");
        final ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616", "artemis", "artemis");
        this.connection = connectionFactory.createConnection();
        ShutdownHook.connection = this.connection;
    }

    public static void main(String[] args) {
        SpringApplication.run(ArtemisConsumerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        connection.start();
        final MessageConsumer consumer = session.createConsumer(queue);
        while (true) {
            final TextMessage message = (TextMessage) consumer.receive(5000);
            if (message != null) {
                System.out.println(String.format("Message received: [%s]", message.getText()));
                message.acknowledge();
            }
            TimeUnit.SECONDS.sleep(1);
        }
    }

    @Component
    public static class ShutdownHook {

        private static Connection connection;

        @PreDestroy
        public void destroy() throws JMSException {
            connection.close();
            System.out.println("shutting down system...");
        }
    }
}
