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

	private static final Logger log = Logger.getLogger(MessageQueueListener.class);
	private final LedSignWriter sign;

	private final DoorSystemSignServiceProperties properties;
	
	public MessageQueueListener() {
		properties = DoorSystemSignServiceProperties.getInstance();
		sign = new LedSignWriter(properties.getLedSignServiceUrl());
	}

	@Override
	public void run() {
		Connection conn = null;
		Channel channel = null;
		try {
			final String exchangeName = properties.getAmqpExchange();
			log.debug("connecting to amqp server");
			ConnectionFactory factory = new ConnectionFactory();
			factory.setUsername(properties.getAmqpUsername());
			factory.setPassword(properties.getAmqpPassword());
			factory.setVirtualHost(properties.getAmqpVirtualHost());
			factory.setHost(properties.getAmqpHost());
			factory.setPort(properties.getAmqpPort());
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
