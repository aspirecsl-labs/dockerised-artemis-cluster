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

import static java.lang.String.format;
import static java.time.LocalDateTime.now;

@Configuration
@SpringBootApplication
public class ArtemisProducerApplication implements CommandLineRunner {

    private final String id;
    private Session session;
    private final Queue queue;
    private MessageProducer producer;

    public ArtemisProducerApplication() throws JMSException {
        id = UUID.randomUUID().toString();
        queue = ActiveMQJMSClient.createQueue("example");
        session = getSession();
        producer = session.createProducer(queue);
    }

    public static void main(String[] args) {
        SpringApplication.run(ArtemisProducerApplication.class, args);
    }

    @Override
    public void run(String... args) {
        while (!ShutdownHook.shuttingDown) {
            try {
                final int groupId = ThreadLocalRandom.current().nextInt(5);
                final TextMessage message =
                        session.createTextMessage(format("[%s] - group [%s] timestamp: [%s]", id, groupId, now()));
                message.setStringProperty("JMSXGroupID", "Group-" + groupId);
                producer.send(message);
                System.out.println("Sent message: " + message.getText());
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (JMSException jmsEx) {
                if (!ShutdownHook.shuttingDown) {
                    System.err.println("|---JMS ERROR---|");
                    jmsEx.printStackTrace();
                    try {
                        System.out.println("Reconnecting...");
                        session = getSession();
                        producer = session.createProducer(queue);
                    } catch (JMSException je2) {
                        je2.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Session getSession() throws JMSException {
        final ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616", "artemis", "artemis");
        final Connection connection = connectionFactory.createConnection();
        final Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        connection.start();
        ShutdownHook.connection = connection;
        return session;
    }

    @Component
    public static class ShutdownHook {

        private static Connection connection;
        private static volatile boolean shuttingDown = false;

        @PreDestroy
        public void destroy() throws JMSException {
            System.out.println("shutting down...");
            shuttingDown = true;
            connection.close();
        }
    }
}
