package org.capybara.dsss;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class MessageQueueListener implements Runnable {

	private static Logger log = Logger.getLogger(MessageQueueListener.class);
	private static LedSignWriter sign = new LedSignWriter("http://192.168.111.4:8080/signservice/");

	public MessageQueueListener() {
		// set up rabbitmq
	}

	@Override
	public void run() {
		Connection conn = null;
		Channel channel = null;
		try {
			final String exchangeName = "door.entry";
			log.debug("connecting to amqp server");
			ConnectionFactory factory = new ConnectionFactory();
			factory.setUsername("ledsign");
			factory.setPassword("blinkielights");
			factory.setVirtualHost("/");
			factory.setHost("192.168.111.14");
			factory.setPort(5672);
			conn = factory.newConnection();

			channel = conn.createChannel();
			log.debug("binding to exchange");
			channel.exchangeDeclare(exchangeName, "fanout");
			String queueName = channel.queueDeclare().getQueue();
			channel.queueBind(queueName, exchangeName, "");

			boolean autoAck = true;
			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(queueName, autoAck, consumer);
			while (true) {
				QueueingConsumer.Delivery delivery;
				try {
					log.debug("waiting for message");
					delivery = consumer.nextDelivery();
					log.debug("message received");
				} catch (InterruptedException ie) {
					return;
				}
				byte[] body = delivery.getBody();
				log.debug("message body: "+new String(body));
				try {
					JAXBContext context = JAXBContext.newInstance(DoorAccessEvent.class);
					log.debug("unmarshalling message");
					Unmarshaller unmarshaller = context.createUnmarshaller();
					DoorAccessEvent dae = (DoorAccessEvent) unmarshaller.unmarshal(new ByteArrayInputStream(body));
					log.debug("triggering door access event");
					sign.doorActionEvent(dae);
					log.debug("done!");
				} catch (JAXBException e) {
					log.error("jaxb error",e);
				}				
			}
		} catch (IOException e) {
			log.fatal("could not set up message queue",e);
		} finally {
			if (channel != null) {
				try {
					channel.close();
				} catch (IOException e) {
					log.error("error closing amqp channel",e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (IOException e) {
					log.error("error closing amqp connection",e);
				}
			}
		}
	}
}
