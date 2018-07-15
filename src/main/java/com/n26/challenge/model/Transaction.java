package com.n26.challenge.model;

import java.math.BigDecimal;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceLeaseExpiration;
import com.gigaspaces.annotation.pojo.SpaceProperty;

/**
 * N26 Java Code Challenge - Transaction Model
 *
 * @author Santiago Alzate S.
 * @version 1.0.0
 * @since 1.0.0
 */
@SpaceClass
public class Transaction {

	/**
	 * The name of the amount field.
	 */
	public static final String AMOUNT_FIELD_NAME = "amount";

	/**
	 * The class identifier.
	 */
	private String id;

	/**
	 * The transaction amount.
	 */
	private BigDecimal amount;

	/**
	 * The transaction time in epoch in millis in UTC time zone.
	 */
	private Long timeStamp;

	/**
	 * The transaction expiration lease
	 */
	private long lease;

	/**
	 * Default class constructor
	 */
	public Transaction() {
	}

	/**
	 * Class constructor with the required fields.
	 *
	 * @param amount The transaction amount
	 * @param timeStamp The transaction time stamp
	 */
	public Transaction(final BigDecimal amount, final Long timeStamp) {

		this.amount = amount;
		this.timeStamp = timeStamp;
	}

	/**
	 * Returns the transaction identifier
	 *
	 * @return the transaction identifier
	 */
	@SpaceId(autoGenerate = true)
	public String getId() {

		return id;
	}

	/**
	 * Returns the transaction's amount
	 *
	 * @return the amount
	 */
	@SpaceProperty
	public BigDecimal getAmount() {

		return amount;
	}

	/**
	 * Returns the transaction's time stamp
	 *
	 * @return the time stamp
	 */
	public Long getTimeStamp() {

		return timeStamp;
	}

	/**
	 * Returns the object lease time
	 *
	 * @return the object lease time
	 */
	@SpaceLeaseExpiration
	public long getLease() {

		return lease;
	}

	/**
	 * Sets the transaction identifier
	 *
	 * @param id the id to set
	 */
	public void setId(final String id) {

		this.id = id;
	}

	/**
	 * Sets the transaction amount
	 *
	 * @param amount the amount to set
	 */
	public void setAmount(final BigDecimal amount) {

		this.amount = amount;
	}

	/**
	 * Sets the transaction time stamp
	 *
	 * @param timeStamp the time stamp to set
	 */
	public void setTimeStamp(final Long timeStamp) {

		this.timeStamp = timeStamp;
	}

	/**
	 * Sets the object lease time
	 *
	 * @param lease the lease to set
	 */
	public void setLease(final long lease) {

		this.lease = lease;
	}

}
