package com.shokkoh.dbsparking.boosters;

import java.util.Date;

public class PlayerBoost {
	private int instanceCode;
	private int boostCode;
	private String playerUUID;
	private Date createdDate;
	private Date expirationDate;
	private long timeLeft;
	private String autor;
	private Boost boost;

	public int getInstanceCode() {
		return instanceCode;
	}

	public void setInstanceCode(int instanceCode) {
		this.instanceCode = instanceCode;
	}

	public int getBoostCode() {
		return boostCode;
	}

	public void setBoostCode(int boostCode) {
		this.boostCode = boostCode;
	}

	public String getPlayerUUID() {
		return playerUUID;
	}

	public void setPlayerUUID(String playerUUID) {
		this.playerUUID = playerUUID;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public long getTimeLeft() {
		return timeLeft;
	}

	public void setTimeLeft(long timeLeft) {
		this.timeLeft = timeLeft;
	}

	public String getAutor() {
		return autor;
	}

	public void setAutor(String autor) {
		this.autor = autor;
	}

	public Boost getBoost() {
		return boost;
	}

	public void setBoost(Boost boost) {
		this.boost = boost;
	}
}