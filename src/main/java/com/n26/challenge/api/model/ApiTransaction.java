package com.n26.challenge.api.model;

/**
 * N26 Java Code Challenge - Transaction API Model
 *
 * @author Santiago Alzate S.
 * @version 1.0.0
 * @since 1.0.0
 */
public class ApiTransaction {

	/**
	 * The transaction amount.
	 */
	private Double amount;

	/**
	 * The transaction time in epoch in millis in UTC time zone.
	 */
	private Long timestamp;

	/**
	 * Returns the transaction's amount
	 *
	 * @return the amount
	 */
	public Double getAmount() {

		return amount;
	}

	/**
	 * Returns the transaction's timestamp
	 *
	 * @return the timestamp
	 */
	public Long getTimestamp() {

		return timestamp;
	}

	/**
	 * Sets the transaction amount
	 *
	 * @param amount the amount to set
	 */
	public void setAmount(final Double amount) {

		this.amount = amount;
	}

	/**
	 * Sets the transaction timestamp
	 *
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(final Long timestamp) {

		this.timestamp = timestamp;
	}

}
