package am.api;

import am.api.model.AMCredential;
import am.api.model.AMString;
import am.api.osgi.XAPooledAMConnectionFactory;
import am.api.wrapper.PoolableObjectFactory;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestConnectionFactory extends TestCase {

	private AMCredential amCredential;

	public static Test suite() {
		return new TestSuite(TestConnectionFactory.class);
	}

	public TestConnectionFactory(String testName) {
		super(testName);
	}

	public void testLocalConnectionCreation() {

		XAPooledAMConnectionFactory pFactory = new XAPooledAMConnectionFactory();
		pFactory.setDefaultCredential(new AMCredential("DEV", "someuser", "somepassword"));
		pFactory.setPoolableObjectFactory(new PoolableObjectFactory());
		pFactory.init();

		AMConnection amConnection = null;

		try {

			amConnection = pFactory.getConnection(amCredential);
			
			Assert.assertNotNull(amConnection);

			AMString loginName = AMString.create(20);

			Assert.assertTrue(amConnection.loginName(loginName) == 0);

			Assert.assertEquals(amCredential.getUserName(), loginName.toString());

			AMHandle query = amConnection.queryCreate();

			long status = amConnection.queryExec(query, "SELECT UserLogin FROM amEmplDept WHERE UserLogin = 'Admin'");

			if (status == 0L) {

				AMString value = AMString.create(255);

				amConnection.getFieldStrValue(query, 1, value);

			}

			amConnection.releaseHandle(query);

		} catch (Throwable e) {
			Assert.assertTrue("error occured", false);
		} finally {
			if (amConnection != null) {
				amConnection.close();
			}
		}

	}

	public void setUp() {
		amCredential = new AMCredential("DEV", "someuser", "somepassword");
	}

}
