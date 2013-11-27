package com.example.testgcm;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4687370139017338330L;
	private long id;
	private String from;
	private String to;
	private String msg;
	private Date timestamp;
	private Boolean delivered;
	private Boolean sent;
	private Boolean isMine;
	private String type;
	
	public static class Types{
		public static String NEW_GAME_REQUEST = "new_game_request";
	}
	
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Message(){
		isMine = false;
		delivered = false;
		sent = false;
	}

	public Boolean getIsMine() {
		return isMine;
	}

	public void setIsMine(Boolean isMine) {
		this.isMine = isMine;
	}

	public Message(String msg, Date date, Boolean isMine) {
		this.msg = msg;
		this.timestamp = date;
		this.isMine = isMine;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Boolean getDelivered() {
		return delivered;
	}

	public void setDelivered(Boolean delivered) {
		this.delivered = delivered;
	}

	public Boolean getSent() {
		return sent;
	}

	public void setSent(Boolean sent) {
		this.sent = sent;
	}

}
