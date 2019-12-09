package com.demo.artemis;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
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

import static java.time.LocalDateTime.now;

@Configuration
@SpringBootApplication
public class ArtemisProducerApplication implements CommandLineRunner {

    private final String id;
    private final Queue queue;
    private final Connection connection;

    public ArtemisProducerApplication() throws JMSException {
        id = UUID.randomUUID().toString();
        queue = ActiveMQJMSClient.createQueue("example");
        final ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://127.0.0.1:61616", "artemis", "artemis");
        this.connection = connectionFactory.createConnection();
        ShutdownHook.connection = this.connection;
    }

    public static void main(String[] args) {
        SpringApplication.run(ArtemisProducerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        connection.start();
        final MessageProducer producer = session.createProducer(queue);
        while (true) {
            final int groupId = ThreadLocalRandom.current().nextInt(5);
            final TextMessage message =
                    session.createTextMessage(
                            String.format("[%s] - message with group id [%s] generated at: [%s]", id, groupId, now()));
            message.setStringProperty("JMSXGroupID", "Group-" + groupId);
            producer.send(message);
            System.out.println("Sent message: " + message.getText() + " to node 0");
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
