package org.capybara.dsss;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
	
public class DoorSystemSignServiceProperties {

	private final String amqpHost;
	private final int amqpPort;
	private final String amqpVirtualHost;
	private final String amqpUsername;
	private final String amqpPassword;
	private final String amqpQueue;
	private final String ledSignServiceUrl;
	
	private static Properties properties;
	private DoorSystemSignServiceProperties instance;
	
	private DoorSystemSignServiceProperties() { 		
		properties = new Properties();
		try {
			properties.load(new FileInputStream("doorsystemsignservice.properties"));
		} catch (IOException e) {
			throw new IllegalStateException("couldn't load properties",e);
		}

		this.amqpHost = properties.getProperty("amqp.host");
		if (this.amqpHost == null) {
			throw new IllegalArgumentException("property amqp.host cannot be blank");
		}
		
		try {
			this.amqpPort = Integer.parseInt(properties.getProperty("amqp.port"));
		} catch (NullPointerException e) {			
			throw new IllegalArgumentException("property amqp.port cannot be blank");
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("property amqp.port must be an integer");
		}
		
		this.amqpUsername = properties.getProperty("amqp.username");
		if (this.amqpUsername == null) {
			throw new IllegalArgumentException("property amqp.username cannot be blank");
		}
		
		this.amqpPassword = properties.getProperty("amqp.password");
		if (this.amqpPassword == null) {
			throw new IllegalArgumentException("property amqp.password cannot be blank");
		}
		
		this.amqpQueue = properties.getProperty("amqp.exchangeName");
		if (this.amqpQueue == null) {
			throw new IllegalArgumentException("property amqp.exchangeName cannot be blank");
		}
		
		this.amqpVirtualHost = properties.getProperty("amqp.virtualHost");
		if (this.amqpVirtualHost == null) {
			throw new IllegalArgumentException("propety amqp.virtualHost cannot be blank");
		}
		
		this.ledSignServiceUrl = properties.getProperty("ledSignServiceUrl");
		if (this.ledSignServiceUrl == null) {
			throw new IllegalArgumentException("property ledSignServiceUrl cannot be blank");
		}
		
	}
	
	public DoorSystemSignServiceProperties getInstance() {
		if (instance == null) {
			instance = new DoorSystemSignServiceProperties();
		}
		return instance;
	}

	public String getAmqpHost() {
		return amqpHost;
	}

	public int getAmqpPort() {
		return amqpPort;
	}

	public String getAmqpVirtualHost() {
		return amqpVirtualHost;
	}

	public String getAmqpUsername() {
		return amqpUsername;
	}

	public String getAmqpPassword() {
		return amqpPassword;
	}

	public String getAmqpQueue() {
		return amqpQueue;
	}

	public String getLedSignServiceUrl() {
		return ledSignServiceUrl;
	}

	
	
}
