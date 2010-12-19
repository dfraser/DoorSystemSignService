package org.capybara.dsss;


import java.net.URL;
import java.net.URLEncoder;

import org.apache.log4j.Logger;

public class LedSignWriter {

	private static Logger log = Logger.getLogger(LedSignWriter.class);
	private String ledSignServiceUrl;
	
	/**
	 * Writes a message on the Hacklab LED sign using St3fan's web service.
	 * 
	 * @param ledSignServiceUrl
	 */
	public LedSignWriter(String ledSignServiceUrl) {
		this.ledSignServiceUrl = ledSignServiceUrl;
	}
	
	/**
	 * Handle a {@link DoorAccessEvent} by writing a message on the LED sign.
	 * @param event
	 */
	public void doorActionEvent(DoorAccessEvent event) {
		log.debug("led sign event gotten!");
		String message;
		if (event.isAllowed()) {
			message = event.getNickName()+"\nhas entered";
		} else {
			message = event.getCardId()+"\nunknown hid card";
		}
		
		try {
			log.debug("writing url");
			URL url = new URL(ledSignServiceUrl+"SignService?FontSize=10&Action=ShowMessage&Message="+URLEncoder.encode(message, "UTF-8")+"&Version=2009-02-03");
			url.getContent();
			log.debug("url returned");
		} catch (Exception e) {
			log.error("couldn't make url", e);
		}

	}
	
}


