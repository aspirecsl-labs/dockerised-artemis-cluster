package com.demo.artemis;

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

@Configuration
@SpringBootApplication
public class ArtemisProducerApplication implements CommandLineRunner {

    private static final String PAYLOAD = "{\n"
            + "   \"id\": \"18120620080936489\",\n"
            + "   \"version\": 0,\n"
            + "   \"scheme_id\": \"MC\",\n"
            + "   \"product_class_id\": \"TC3_ORIAN_STANDARD\",\n"
            + "   \"created_date_time\": \"2018-12-06T20:08:09+0000\",\n"
            + "   \"txn_lifecycle_status\": \"AUTHORISATION_REQUESTED\",\n"
            + "   \"payee\": {\n"
            + "     \"acceptor\": {\n"
            + "       \"name\": \"C3 MAIN BRANCH\",\n"
            + "       \"id_auth\": \"123456\",\n"
            + "       \"acquirer_id_auth\": \"234567\",\n"
            + "       \"category_auth\": \"5011\",\n"
            + "       \"country\": \"ARE\"\n"
            + "     }\n"
            + "   },\n"
            + "   \"payer\": {\n"
            + "     \"account\": {\n"
            + "       \"account_no\": \"64059399943562\",\n"
            + "       \"balance_code\": \"MAIN\",\n"
            + "       \"balances\": [\n"
            + "         {\n"
            + "           \"type\": \"AVAILABLE_TO_SPEND\",\n"
            + "           \"amount\": {\n"
            + "             \"amount\": 36200,\n"
            + "             \"currency\": {\n"
            + "               \"code\": \"AED\",\n"
            + "               \"exponent\": 2\n"
            + "             }\n"
            + "           }\n"
            + "         },\n"
            + "         {\n"
            + "           \"type\": \"CLEARED\",\n"
            + "           \"amount\": {\n"
            + "             \"amount\": 36200,\n"
            + "             \"currency\": {\n"
            + "               \"code\": \"AED\",\n"
            + "               \"exponent\": 2\n"
            + "             }\n"
            + "           }\n"
            + "         }\n"
            + "       ]\n"
            + "     },\n"
            + "     \"card\": {\n"
            + "       \"serial_no\": \"6709077650\",\n"
            + "       \"masked_pan\": \"531160******7862\"\n"
            + "     }\n"
            + "   },\n"
            + "   \"txn_type\": {\n"
            + "     \"code\": \"REDEMPTION\",\n"
            + "     \"direction\": \"DEBIT\",\n"
            + "     \"reversal\": false,\n"
            + "     \"financial\": true,\n"
            + "     \"activation\": false\n"
            + "   },\n"
            + "   \"acceptance\": {\n"
            + "     \"acceptance_method\": \"CHIP\",\n"
            + "     \"verification_results\": []\n"
            + "   },\n"
            + "   \"authorisation\": {\n"
            + "     \"auth_date_time\": \"2018-12-06T20:08:09+0000\",\n"
            + "     \"local_date_time\": \"2018-12-06T20:08:09+0000\",\n"
            + "     \"txn_amounts\": {\n"
            + "       \"requested_amount\": {\n"
            + "         \"amount\": 100,\n"
            + "         \"currency\": {\n"
            + "           \"code\": \"AED\",\n"
            + "           \"exponent\": 2\n"
            + "         }\n"
            + "       },\n"
            + "       \"cardholder_amount\": {\n"
            + "         \"amount\": 100,\n"
            + "         \"currency\": {\n"
            + "           \"code\": \"AED\",\n"
            + "           \"exponent\": 2\n"
            + "         }\n"
            + "       },\n"
            + "       \"cardholder_final_amount\": {\n"
            + "         \"amount\": 100,\n"
            + "         \"currency\": {\n"
            + "           \"code\": \"AED\",\n"
            + "           \"exponent\": 2\n"
            + "         }\n"
            + "       }\n"
            + "     },\n"
            + "     \"txn_reference\": {\n"
            + "       \"scheme_txn_reference\": \"889889\",\n"
            + "       \"retrieval_reference\": \"PCZ257186800\"\n"
            + "     }\n"
            + "   }\n"
            + "}";

    private final Queue queue;
    private Session session;
    private MessageProducer producer;

    public ArtemisProducerApplication() throws JMSException {
        queue = ActiveMQJMSClient.createQueue("example");
        session = getSession();
        producer = session.createProducer(queue);
    }

    public static void main(String[] args) {
        SpringApplication.run(ArtemisProducerApplication.class, args);
    }

    @Override
    public void run(String... args) {
        long idx = 1L;
        while (!ShutdownHook.shuttingDown) {
            if (idx == Long.MAX_VALUE) {
                idx = 1;
            }
            try {
                final String groupId = "Group-" + ThreadLocalRandom.current().nextInt(2);
                final TextMessage message =
                        session.createTextMessage(format("group: %s | index: %s | payload: [%s]", groupId, idx++, PAYLOAD));
                message.setStringProperty("JMSXGroupID", groupId);
                producer.send(message);
                System.out.println(message.getText());
                TimeUnit.MILLISECONDS.sleep(100);
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
        final ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://themis:61616", "artemis", "artemis");
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
