package com.n26.challenge.service;

import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.time.Instant;

import org.easymock.EasyMock;
import org.openspaces.core.GigaSpace;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gigaspaces.query.ISpaceQuery;
import com.gigaspaces.query.aggregators.AggregationResult;
import com.gigaspaces.query.aggregators.AggregationSet;
import com.j_spaces.core.LeaseProxy;
import com.n26.challenge.AbstractGenericMockTest;
import com.n26.challenge.api.model.StatisticsResult;
import com.n26.challenge.model.Transaction;

/**
 * Test class for {@link StatisticsService} using mocks.
 *
 * @author Santiago Alzate S. (santiago.alzate@payulatam.com)
 * @version 1.0.0
 * @since 1.0.0
 */
public class StatisticsServiceMockTest extends AbstractGenericMockTest {

	/**
	 * Class under test
	 */
	private StatisticsService service;

	/**
	 * The Giga Space mock
	 */
	private GigaSpace gigaSpaceMock;

	/**
	 * Creates the set up for the test cases
	 */
	@BeforeClass
	public void setUp() {

		gigaSpaceMock = EasyMock.createMock(GigaSpace.class);
		registerMocks(gigaSpaceMock);

		service = new StatisticsService(gigaSpaceMock);
	}

	/**
	 * Test case for {@link StatisticsService#createTransaction(long, double)} method when the time stamp is from an old date
	 */
	@Test(description = "Test case for createTransaction method when the time stamp is from an old date",
			expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp =
				"The transaction timestamp can not be older than 60 seconds nor in the future.")
	public void createTransactionTestOldDate() {

		// November 3rd 2016 time stamp
		final long timeStamp = 1478192204000L;
		service.createTransaction(timeStamp, 0d);
		fail("An exception should have been thrown");
	}

	/**
	 * Test case for {@link StatisticsService#createTransaction(long, double)} method when the time stamp is exactly 60 seconds in the past
	 */
	@Test(description = "Test case for createTransaction method when the time stamp is exactly 60 seconds in the past",
			expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp =
				"The transaction timestamp can not be older than 60 seconds nor in the future.")
	public void createTransactionTestLimitOldDate() {

		// Current Time stamp minus 60 seconds and 1 millisecond
		final long timeStamp = Instant.now().toEpochMilli() - 60_001;
		service.createTransaction(timeStamp, 0d);
		fail("An exception should have been thrown");
	}

	/**
	 * Test case for {@link StatisticsService#createTransaction(long, double)} method when the time stamp is from a future date
	 */
	@Test(description = "Test case for createTransaction method when the time stamp is from a future date",
			expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp =
				"The transaction timestamp can not be older than 60 seconds nor in the future.")
	public void createTransactionTestFutureDate() {

		// October 14th 2019 time stamp
		final long timeStamp = 1571081032000L;
		service.createTransaction(timeStamp, 0d);
		fail("An exception should have been thrown");
	}

	/**
	 * Test case for {@link StatisticsService#createTransaction(long, double)} method when the time stamp is 30 seconds in the past
	 */
	@SuppressWarnings("unchecked")
	@Test(description = "Test case for createTransaction method when the time stamp is 30 seconds in the past")
	public void createTransactionTest30SecondsOld() {

		final long currentTimeStamp = Instant.now().toEpochMilli();
		// Current Time stamp minus 30 seconds
		final long transactionTimeStamp = currentTimeStamp - 30_000;
		final LeaseProxy leaseContext = new LeaseProxy();
		leaseContext.setExpiration(currentTimeStamp + 30_000);

		resetMocks();
		EasyMock.expect(gigaSpaceMock.write(EasyMock.isA(Transaction.class), EasyMock.anyLong()))
				.andReturn(leaseContext);
		replayMocks();

		final long expiration = service.createTransaction(transactionTimeStamp, 123d);

		verifyMocks();
		// Transaction life time vs real expiration should equals.
		// Comparing difference, to 2 ms in case the test takes a while to run
		assertThat(expiration).as("Transaction life time should be 60 seconds").isCloseTo(transactionTimeStamp + 60_000,
				within(2L));
	}

	/**
	 * Test case for {@link StatisticsService#getStatistics()} method when all the information is empty
	 */
	@SuppressWarnings("unchecked")
	@Test(description = "Test case for getStatistics method when all the information is empty")
	public void getStatisticsTestNullInformation() {

		final Object[] values = { null, 0L, null, null, null };
		final AggregationResult result = new AggregationResult(values, null);

		resetMocks();
		EasyMock.expect(gigaSpaceMock.aggregate(EasyMock.isA(ISpaceQuery.class), EasyMock.isA(AggregationSet.class)))
				.andReturn(result);
		replayMocks();

		final StatisticsResult statistics = service.getStatistics();

		verifyMocks();
		assertThat(statistics.getAvg()).isEqualByComparingTo(0d);
		assertThat(statistics.getCount()).isEqualByComparingTo(0L);
		assertThat(statistics.getMax()).isEqualByComparingTo(0d);
		assertThat(statistics.getMin()).isEqualByComparingTo(0d);
		assertThat(statistics.getSum()).isEqualByComparingTo(0d);
	}

	/**
	 * Test case for {@link StatisticsService#getStatistics()} method when all the information is zero
	 */
	@SuppressWarnings("unchecked")
	@Test(description = "Test case for getStatistics method when all the information is zero")
	public void getStatisticsTestZeroInformation() {

		final Object[] values = { ZERO, 0L, ZERO, ZERO, ZERO };
		final AggregationResult result = new AggregationResult(values, null);

		resetMocks();
		EasyMock.expect(gigaSpaceMock.aggregate(EasyMock.isA(ISpaceQuery.class), EasyMock.isA(AggregationSet.class)))
				.andReturn(result);
		replayMocks();

		final StatisticsResult statistics = service.getStatistics();

		verifyMocks();
		assertThat(statistics.getAvg()).isEqualByComparingTo(0d);
		assertThat(statistics.getCount()).isEqualByComparingTo(0L);
		assertThat(statistics.getMax()).isEqualByComparingTo(0d);
		assertThat(statistics.getMin()).isEqualByComparingTo(0d);
		assertThat(statistics.getSum()).isEqualByComparingTo(0d);
	}

	/**
	 * Test case for {@link StatisticsService#getStatistics()} method with some information
	 */
	@SuppressWarnings("unchecked")
	@Test(description = "Test case for getStatistics method with some information")
	public void getStatisticsTestWithInformation() {

		final Object[] values = { new BigDecimal(100), 5L, new BigDecimal(200), new BigDecimal(50), BigDecimal.TEN };
		final AggregationResult result = new AggregationResult(values, null);

		resetMocks();
		EasyMock.expect(gigaSpaceMock.aggregate(EasyMock.isA(ISpaceQuery.class), EasyMock.isA(AggregationSet.class)))
				.andReturn(result);
		replayMocks();

		final StatisticsResult statistics = service.getStatistics();

		verifyMocks();
		assertThat(statistics.getAvg()).isEqualByComparingTo(100d);
		assertThat(statistics.getCount()).isEqualByComparingTo(5L);
		assertThat(statistics.getMax()).isEqualByComparingTo(200d);
		assertThat(statistics.getMin()).isEqualByComparingTo(50d);
		assertThat(statistics.getSum()).isEqualByComparingTo(10d);
	}

}
