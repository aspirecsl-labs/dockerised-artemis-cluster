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
    private MessageConsumer consumer;

    public ArtemisConsumerApplication() throws JMSException {
        queue = ActiveMQJMSClient.createQueue("example");
        final Session session = getSession();
        consumer = session.createConsumer(queue);
    }

    public static void main(String[] args) {
        SpringApplication.run(ArtemisConsumerApplication.class, args);
    }

    @Override
    public void run(String... args) {
        while (!ShutdownHook.shuttingDown) {
            try {
                final TextMessage message = (TextMessage) consumer.receive(5000);
                if (message != null) {
                    System.out.println("Received message: " + message.getText());
                    message.acknowledge();
                }
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (JMSException je1) {
                if (!ShutdownHook.shuttingDown) {
                    System.err.println("|---JMS ERROR---|");
                    je1.printStackTrace();
                    try {
                        System.out.println("Reconnecting...");
                        consumer = getSession().createConsumer(queue);
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
