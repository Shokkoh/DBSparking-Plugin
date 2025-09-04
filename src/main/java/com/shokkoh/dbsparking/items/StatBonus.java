package com.shokkoh.dbsparking.items;

public class StatBonus {
	private int id, itemId;
	private String bonusID;
	private Stat statType;
	private ModifierType modifierType;
	private double value;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public Stat getStatType() {
		return statType;
	}

	public void setStatType(Stat statType) {
		this.statType = statType;
	}

	public ModifierType getModifierType() {
		return modifierType;
	}

	public void setModifierType(ModifierType modifierType) {
		this.modifierType = modifierType;
	}

	public double getValue() {
		return Math.abs(value);
	}

	public void setValue(double value) {
		this.value = value;
	}

	public String getBonusID() {
		return bonusID;
	}

	public void setBonusID(String bonusID) {
		this.bonusID = bonusID;
	}


}
