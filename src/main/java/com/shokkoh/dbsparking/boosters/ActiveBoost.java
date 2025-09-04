package com.shokkoh.dbsparking.boosters;

import java.util.Date;

public class ActiveBoost {
	private int id;
	private String name;
	private BoostType boostType;
	private String target;
	private String targetName;
	private double amount;
	private long duration;
	private Date lastUpdated;
	private String author;

	private transient long expirationTimestamp = -1;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BoostType getBoostType() {
		return boostType;
	}

	public void setBoostType(BoostType boostType) {
		this.boostType = boostType;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public long getExpirationTimestamp() {
		if (expirationTimestamp == -1) {
			expirationTimestamp = new Date().getTime() + duration;
		}
		return expirationTimestamp;
	}

	public void setExpirationTimestamp(long expirationTimestamp) {
		this.expirationTimestamp = expirationTimestamp;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}
}