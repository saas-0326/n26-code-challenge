package com.n26.challenge.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.gigaspaces.query.aggregators.AggregationResult;
import com.gigaspaces.query.aggregators.AggregationSet;
import com.j_spaces.core.client.SQLQuery;
import com.n26.challenge.api.model.StatisticsResult;
import com.n26.challenge.model.Transaction;

/**
 * Service implementation for {@link IStatisticsService} interface.
 *
 * @author Santiago Alzate S. (santiago.alzate@payulatam.com)
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
public class StatisticsService implements IStatisticsService {

	/**
	 * Default transaction lease
	 */
	private static final long DEFAULT_LEASE = 60_000;

	/**
	 * The In-Memory Data Grid accessor
	 */
	private final GigaSpace gigaSpace;

	/**
	 * Default service constructor
	 */
	@Autowired
	public StatisticsService(@Value("${statistics.space.create}") final boolean newSpace,
			@Value("${statistics.space.name}") final String spaceName) {

		final String spaceUrl = newSpace ? "/./" + spaceName : "jini://*/*/" + spaceName;
		this.gigaSpace = new GigaSpaceConfigurer(new UrlSpaceConfigurer(spaceUrl)).gigaSpace();
	}

	/**
	 * Default service constructor
	 */
	public StatisticsService(final GigaSpace gigaSpace) {

		this.gigaSpace = gigaSpace;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see IStatisticsService#createTransaction(long, double)
	 */
	@Override
	public long createTransaction(final long timeStamp, final double amount) {

		final long currentTimeStamp = Instant.now().toEpochMilli();
		if (timeStamp + DEFAULT_LEASE < currentTimeStamp || timeStamp > currentTimeStamp) {
			throw new IllegalArgumentException(
					"The transaction timestamp can not be older than 60 seconds nor in the future.");
		}

		// Writes the object with 60 seconds lease (starting from the sent
		// timestamp)
		final BigDecimal modelAmount = new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP);
		return gigaSpace.write(new Transaction(modelAmount, timeStamp), timeStamp + DEFAULT_LEASE - currentTimeStamp)
				.getExpiration();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see IStatisticsService#getStatistics()
	 */
	public StatisticsResult getStatistics() {

		final AggregationResult aggregate = gigaSpace.aggregate(new SQLQuery<>(Transaction.class, ""),
				new AggregationSet().average(Transaction.AMOUNT_FIELD_NAME).count(Transaction.AMOUNT_FIELD_NAME)
						.maxValue(Transaction.AMOUNT_FIELD_NAME).minValue(Transaction.AMOUNT_FIELD_NAME)
						.sum(Transaction.AMOUNT_FIELD_NAME));

		final double avg = getDoubleValue(aggregate.get(0));
		final long count = aggregate.getLong(1);
		final double max = getDoubleValue(aggregate.get(2));
		final double min = getDoubleValue(aggregate.get(3));
		final double sum = getDoubleValue(aggregate.get(4));

		return new StatisticsResult(avg, count, max, min, sum);
	}

	/**
	 * Returns the duble value of the specified object, by casting it into a
	 * BigDecimal and then invoking the {@link BigDecimal#doubleValue()} method.
	 * 
	 * @param object
	 *            The object to convert
	 * @return the double value of the object
	 */
	private double getDoubleValue(final Object object) {

		final BigDecimal bigDecimal = (BigDecimal) object;
		return bigDecimal == null ? 0d : bigDecimal.setScale(2, RoundingMode.HALF_UP).doubleValue();
	}

}
