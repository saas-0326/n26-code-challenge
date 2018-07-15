package com.n26.challenge;

import org.easymock.EasyMock;
import org.easymock.internal.MocksControl;
import org.easymock.internal.RecordState;
import org.testng.annotations.BeforeMethod;

/**
 * Abstract generic mock test
 *
 * @author <a href=manuel.vieda@payulatam.com>Manuel E. Vieda</a>
 * @version 4.7.3
 * @since 4.7.3
 */
public abstract class AbstractGenericMockTest {

	/**
	 * Array that contains all the mocks used by the test suite
	 */
	protected Object[] mocks = new Object[]{};

	/**
	 * Register the mock for an automatic reset/verify process
	 *
	 * @param mock The mock to register
	 */
	protected void registerMocks(final Object... mocks) {

		this.mocks = mocks;
	}

	/**
	 * Resets all the mock objects of the test suite.
	 * <p>
	 * This method is invocted before any test case call
	 */
	@BeforeMethod
	protected void resetMocks() {

		EasyMock.reset(mocks);
	}

	/**
	 * Switches all the mock objects of the test suite to replay mode.
	 */
	protected void replayMocks() {

		EasyMock.replay(mocks);
	}

	/**
	 * Verifies all the mock objects of the test suite.
	 */
	protected void verifyMocks() {

		for (final Object mock : mocks) {
			if (MocksControl.getControl(mock).getState() instanceof RecordState) {
				EasyMock.replay(mock);
			}
		}
		EasyMock.verify(mocks);

	}

}
