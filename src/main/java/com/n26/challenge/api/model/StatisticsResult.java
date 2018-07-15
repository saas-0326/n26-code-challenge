package com.n26.challenge.api.model;

/**
 * The intermediate class with the statistical information found for
 * the transactions. <br/>
 * The objects created by this class should be immutable, hence no
 * 'set' methods are implemented.
 *
 * @author Santiago Alzate S.
 * @version 1.0
 * @since 1.0
 */
public class StatisticsResult {

	/**
	 * The transactions' average amount
	 */
	private double avg;

	/**
	 * The number of transactions
	 */
	private long count;

	/**
	 * The transactions' maximum amount
	 */
	private double max;

	/**
	 * The transactions' minimum amount
	 */
	private double min;

	/**
	 * The transactions' total amount
	 */
	private double sum;

	/**
	 * Default class constructor
	 */
	public StatisticsResult() {
		super();
	}

	/**
	 * Creates the Statistical Result object with the given information.
	 *
	 * @param avg the transactions' average amount
	 * @param count the number of transactions
	 * @param max the transactions' maximum amount
	 * @param min the transactions' minimum amount
	 * @param sum the transactions' total amount
	 */
	public StatisticsResult(final double avg, final long count,
			final double max, final double min, final double sum) {

		this.avg = avg;
		this.count = count;
		this.max = max;
		this.min = min;
		this.sum = sum;
	}

	/**
	 * Returns the average amount
	 *
	 * @return the average
	 */
	public double getAvg() {

		return avg;
	}

	/**
	 * Returns the number of transactions
	 *
	 * @return the number of transactions
	 */
	public long getCount() {

		return count;
	}

	/**
	 * Returns the maximum amount
	 *
	 * @return the maximum amount
	 */
	public double getMax() {

		return max;
	}

	/**
	 *  Returns the minimum amount
	 *
	 * @return the minimum amount
	 */
	public double getMin() {

		return min;
	}

	/**
	 * Returns the total amount
	 *
	 * @return the total amount
	 */
	public double getSum() {

		return sum;
	}

}
