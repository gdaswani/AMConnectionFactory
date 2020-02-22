/*
    This file is part of AMConnectionFactory.

    AMConnectionFactory is free software: you can redistribute it and/or modify
    it under the terms of the GNU LESSER General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    AMConnectionFactory is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with AMConnectionFactory.  If not, see <https://www.gnu.org/licenses/>.
 */
package am.server;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.sun.jna.platform.win32.Kernel32;

import am.api.AMHandle;
import am.api.exception.AMConnectionException;
import am.api.exception.CallTimeOutException;
import am.api.exception.IllegalConnectionStateException;
import am.api.model.AMDate;
import am.api.model.AMString;
import am.api.wrapper.AMConnectionDelegate;
import am.server.client.AMLibraryRemote;
import am.server.client.ReturnWithString;
import am.server.tasks.*;

public class AMAPIProcess implements AMLibraryRemote {

	private final static int DEFAULT_PORT = 10099;

	private final static String LOG_PATH = "C:\\services\\apache-kafaf\\data\\log\\";

	private final static Logger LOGGER = Logger.getLogger(AMAPIProcess.class.getPackage().getName());

	public final static String PARAM_RMI_SERVER_PORT = "am.server.port";

	public final static String PARAM_LOG_PATH = "am.log.path";

	private static CountDownLatch shutdownSignal = new CountDownLatch(1);

	public final static String STATUS_FAILED = "STATUS_FAILED";

	public final static String STATUS_READY = "STATUS_READY";

	public static void main(String[] args) {

		final String portParam = System.getProperty(PARAM_RMI_SERVER_PORT, Integer.toString(DEFAULT_PORT));

		final String logPath = System.getProperty(PARAM_LOG_PATH, LOG_PATH);

		final String lockFileName = String.format("%1$sAMAPIProcess_%2$s.lck", logPath, portParam);

		try (FileChannel channel = new RandomAccessFile(new File(lockFileName), "rw").getChannel()) {

			FileLock lock = channel.tryLock(0, 1024, false);

			if (lock != null) {

				LogManager.getLogManager()
						.readConfiguration(AMAPIProcess.class.getResourceAsStream("/am/server/logging.properties"));

				LOGGER.log(Level.INFO, AMAPIProcess.class.getName());

				AMLibraryRemote engine = null;

				LOGGER.log(Level.INFO, "Starting");

				LOGGER.log(Level.INFO, "Retrieving process id");

				int processId = Kernel32.INSTANCE.GetCurrentProcessId();

				LOGGER.log(Level.INFO, String.format("Process id [%1$s]", processId));

				LOGGER.log(Level.INFO, String.format("System property [%1$s] has a value of [%2$s]",
						PARAM_RMI_SERVER_PORT, portParam));

				int serverPort = Integer.parseInt(portParam);

				// create registry

				LOGGER.log(Level.INFO, "Creating Registry");

				Registry registry = LocateRegistry.createRegistry(serverPort);

				engine = new AMAPIProcess(serverPort);

				// bind service

				LOGGER.log(Level.INFO, "Binding Service");

				AMLibraryRemote stub = (AMLibraryRemote) UnicastRemoteObject.exportObject(engine, 0);

				registry.rebind(engine.getClass().getName(), stub);

				System.out.println(String.format("%1$s:%2$d", STATUS_READY, processId));

				// listen for service shutdown

				LOGGER.log(Level.INFO, "Waiting for shutdown event, maximum of 8 HOURS before self termination");

				shutdownSignal.await(8, TimeUnit.HOURS);

				LOGGER.log(Level.INFO, "Europe: It's the final countdown");

				LOGGER.log(Level.INFO, "Performing final cleanup");

				ExecutorService execService = Executors.newSingleThreadExecutor(new ThreadFactory() {
					public Thread newThread(Runnable r) {
						Thread t = Executors.defaultThreadFactory().newThread(r);
						t.setDaemon(true);
						return t;
					}
				});

				Future<Void> resultJob = execService.submit(new ShutdownProcess(registry, engine));

				try {
					resultJob.get(5L, TimeUnit.SECONDS);
				} catch (TimeoutException timeE) {
					LOGGER.log(Level.WARNING, "Timed out waiting for shutdown callable");
				}

			} else {

				// could not lock

				throw new IllegalStateException("Could not secure lock file");

			}

		} catch (Exception e) {

			LOGGER.log(Level.SEVERE, e.getMessage());

			System.out.println(String.format("%1$s:%2$s", STATUS_FAILED, e.getMessage()));
		}

		LOGGER.log(Level.INFO, "Shutdown");

		System.exit(0);
	}

	private long callTimeOutInMs;

	private final AMConnectionDelegate delegate;

	private final ExecutorService executorService;

	private volatile CallTimeOutException lastException;

	private final int serverPort;

	public AMAPIProcess(int serverPort) {

		super();

		this.delegate = new AMConnectionDelegate();

		this.serverPort = serverPort;

		this.callTimeOutInMs = Long.MAX_VALUE;

		this.lastException = null;

		this.executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
			public Thread newThread(Runnable r) {
				Thread t = Executors.defaultThreadFactory().newThread(r);
				t.setDaemon(true);
				return t;
			}
		});

	}

	private void assertValidState() {

		/*
		 * if we encountered a previous timeout - then fail fast
		 */

		if (lastException != null) {
			throw new IllegalConnectionStateException(lastException);
		}
	}

	@Override
	public void cleanup() {

		clearLastError();

		releaseRegisteredHandles();

		lastException = null;

	}

	@Override
	public long clearLastError() {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new ClearLastError(delegate));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public void close() {

		assertValidState();

		LOGGER.log(Level.FINE, "Trying to close connection as requested");

		releaseRegisteredHandles();

		Future<Void> resultJob = executorService.submit(new Close(delegate));

		try {
			resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);

		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long commit() {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new Commit(delegate));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public ReturnWithString connectionName(AMString connectionName) {

		assertValidState();

		Future<ReturnWithString> resultJob = executorService.submit(new ConnectionName(delegate, connectionName));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long convertDateBasicToUnix(long tmTime) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new ConvertDateBasicToUnix(delegate, tmTime));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long convertDateIntlToUnix(String dateAsString) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new ConvertDateIntlToUnix(delegate, dateAsString));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long convertDateStringToUnix(String dateAsString) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new ConvertDateStringToUnix(delegate, dateAsString));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long convertDateUnixToBasic(long dateAsUnix) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new ConvertDateUnixToBasic(delegate, dateAsUnix));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public ReturnWithString convertDateUnixToIntl(long unixDate, AMString dateAsIntlStr) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new ConvertDateUnixToIntl(delegate, unixDate, dateAsIntlStr));

		try {
			return new ReturnWithString(resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS), dateAsIntlStr);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public ReturnWithString convertDateUnixToString(long dateAsUnix, AMString dateAsStr) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new ConvertDateUnixToString(delegate, dateAsUnix, dateAsStr));

		try {
			return new ReturnWithString(resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS), dateAsStr);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public ReturnWithString convertDoubleToString(double dSrc, AMString dblAsString) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new ConvertDoubleToString(delegate, dSrc, dblAsString));

		try {
			return new ReturnWithString(resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS), dblAsString);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public ReturnWithString convertMonetaryToString(double dMonetarySrc, AMString dblAsString) {

		assertValidState();

		Future<Long> resultJob = executorService
				.submit(new ConvertMonetaryToString(delegate, dMonetarySrc, dblAsString));

		try {
			return new ReturnWithString(resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS), dblAsString);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public double convertStringToDouble(String dblAsString) {

		assertValidState();

		Future<Double> resultJob = executorService.submit(new ConvertStringToDouble(delegate, dblAsString));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public double convertStringToMonetary(String monetaryAsString) {

		assertValidState();

		Future<Double> resultJob = executorService.submit(new ConvertStringToMonetary(delegate, monetaryAsString));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long createLink(AMHandle srcRecHandle, String linkName, AMHandle srcDstHandle) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new CreateLink(delegate, srcRecHandle, linkName, srcDstHandle));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public AMHandle createRecord(String tblName) {

		assertValidState();

		Future<AMHandle> resultJob = executorService.submit(new CreateRecord(delegate, tblName));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long currentDate() {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new CurrentDate(delegate));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long currentServerDate() {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new CurrentServerDate(delegate));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long dateAdd(long startAsUnixDate, long duration) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new DateAdd(delegate, startAsUnixDate, duration));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long dateAddLogical(long startAsUnixDate, long duration) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new DateAddLogical(delegate, startAsUnixDate, duration));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long dateDiff(long endAsUnixDate, long startAsUnixDate) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new DateDiff(delegate, endAsUnixDate, startAsUnixDate));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long dbExecAql(String aqlQuery) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new DbExecAql(delegate, aqlQuery));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long dbGetDate(String aqlQuery) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new DbGetDate(delegate, aqlQuery));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public double dbGetDouble(String aqlQuery) {

		assertValidState();

		Future<Double> resultJob = executorService.submit(new DbGetDouble(delegate, aqlQuery));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public ReturnWithString dbGetLimitedList(String aqlQuery, AMString result, String colSeperator,
			String lineSeperator, String idSeperator, long maxSize, long errorType) {

		assertValidState();

		Future<ReturnWithString> resultJob = executorService.submit(new DbGetLimitedList(delegate, aqlQuery, result,
				colSeperator, lineSeperator, idSeperator, maxSize, errorType));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public ReturnWithString dbGetList(String aqlQuery, AMString result, String colSeperator, String lineSeperator,
			String idSeperator) {

		assertValidState();

		Future<ReturnWithString> resultJob = executorService
				.submit(new DbGetList(delegate, aqlQuery, result, colSeperator, lineSeperator, idSeperator));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public ReturnWithString dbGetListEx(String aqlQuery, AMString result, String colSeperator, String lineSeperator,
			String idSeperator) {

		assertValidState();

		Future<ReturnWithString> resultJob = executorService
				.submit(new DbGetListEx(delegate, aqlQuery, result, colSeperator, lineSeperator, idSeperator));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long dbGetLong(String aqlQuery) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new DbGetLong(delegate, aqlQuery));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long dbGetPk(String tableName, String whereClause) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new DbGetPk(delegate, tableName, whereClause));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public ReturnWithString dbGetString(String query, AMString result, String colSeperator, String lineSeperator) {

		assertValidState();

		Future<ReturnWithString> resultJob = executorService
				.submit(new DbGetString(delegate, query, result, colSeperator, lineSeperator));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public ReturnWithString dbGetStringEx(String query, AMString result, String colSeperator, String lineSeperator) {

		assertValidState();

		Future<ReturnWithString> resultJob = executorService
				.submit(new DbGetStringEx(delegate, query, result, colSeperator, lineSeperator));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long deleteLink(AMHandle srcRecHandle, String linkName, AMHandle srcDstHandle) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new DeleteLink(delegate, srcRecHandle, linkName, srcDstHandle));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long deleteRecord(AMHandle recordHandle) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new DeleteRecord(delegate, recordHandle));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long duplicateRecord(AMHandle recordHandle, long insert) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new DuplicateRecord(delegate, recordHandle, insert));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public ReturnWithString enumValList(String enumName, AMString value, long caseSensitive, String lineSeperator) {

		assertValidState();

		Future<ReturnWithString> resultJob = executorService
				.submit(new EnumValList(delegate, enumName, value, caseSensitive, lineSeperator));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public AMHandle executeActionById(long actionId, String tableName, long recordId) {

		assertValidState();

		Future<AMHandle> resultJob = executorService
				.submit(new ExecuteActionById(delegate, actionId, tableName, recordId));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public AMHandle executeActionByName(String sqlName, String tableName, long recordId) {

		assertValidState();

		Future<AMHandle> resultJob = executorService
				.submit(new ExecuteActionByName(delegate, sqlName, tableName, recordId));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long exportDocument(long documentId, String fileName) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new ExportDocument(delegate, documentId, fileName));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long flushTransaction() {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new FlushTransaction(delegate));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public ReturnWithString formatCurrency(double amount, String currency, AMString result) {

		assertValidState();

		Future<ReturnWithString> resultJob = executorService
				.submit(new FormatCurrency(delegate, amount, currency, result));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}
	}

	@Override
	public ReturnWithString formatLong(long number, String format, AMString result) {

		assertValidState();

		Future<ReturnWithString> resultJob = executorService.submit(new FormatLong(delegate, number, format, result));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	private long getCallTimeOutInMs() {
		return callTimeOutInMs;
	}

	@Override
	public ReturnWithString getComputeString(String tableName, long recordId, String template, AMString result) {

		assertValidState();

		Future<ReturnWithString> resultJob = executorService
				.submit(new GetComputeString(delegate, tableName, recordId, template, result));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public AMHandle getField(AMHandle objHandle, long position) {

		assertValidState();

		Future<AMHandle> resultJob = executorService.submit(new GetField(delegate, objHandle, position));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long getFieldCount(AMHandle objHandle) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new GetFieldCount(delegate, objHandle));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long getFieldDateOnlyValue(AMHandle recHandle, long fieldPos) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new GetFieldDateOnlyValue(delegate, recHandle, fieldPos));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long getFieldDateValue(AMHandle recHandle, long fieldPos) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new GetFieldDateValue(delegate, recHandle, fieldPos));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public ReturnWithString getFieldDescription(AMHandle fieldHandle, AMString target) {

		assertValidState();

		Future<ReturnWithString> resultJob = executorService
				.submit(new GetFieldDescription(delegate, fieldHandle, target));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public double getFieldDoubleValue(AMHandle objHandle, long fieldPos) {

		assertValidState();

		Future<Double> resultJob = executorService.submit(new GetFieldDoubleValue(delegate, objHandle, fieldPos));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public ReturnWithString getFieldFormat(AMHandle fldHandle, AMString target) {

		assertValidState();

		Future<ReturnWithString> resultJob = executorService.submit(new GetFieldFormat(delegate, fldHandle, target));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}
	}

	@Override
	public ReturnWithString getFieldFormatFromName(String tblName, String fldName, AMString result) {

		assertValidState();

		Future<ReturnWithString> resultJob = executorService
				.submit(new GetFieldFormatFromName(delegate, tblName, fldName, result));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}
	}

	@Override
	public AMHandle getFieldFromName(AMHandle objHandle, String fldName) {

		assertValidState();

		Future<AMHandle> resultJob = executorService.submit(new GetFieldFromName(delegate, objHandle, fldName));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public ReturnWithString getFieldLabel(AMHandle fldHandle, AMString result) {

		assertValidState();

		Future<ReturnWithString> resultJob = executorService.submit(new GetFieldLabel(delegate, fldHandle, result));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public ReturnWithString getFieldLabelFromName(String tableName, String fieldName, AMString fieldLabel) {

		assertValidState();

		Future<ReturnWithString> resultJob = executorService
				.submit(new GetFieldLabelFromName(delegate, tableName, fieldName, fieldLabel));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long getFieldLongValue(AMHandle objHandle, long fieldPosition) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new GetFieldLongValue(delegate, objHandle, fieldPosition));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public ReturnWithString getFieldName(AMHandle objHandle, long fldPosition, AMString fieldName) {

		assertValidState();

		Future<ReturnWithString> resultJob = executorService
				.submit(new GetFieldName(delegate, objHandle, fldPosition, fieldName));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long getFieldSize(AMHandle fldHandle) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new GetFieldSize(delegate, fldHandle));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public ReturnWithString getFieldSqlName(AMHandle fldHandle, AMString fieldSQLName) {

		assertValidState();

		Future<ReturnWithString> resultJob = executorService
				.submit(new GetFieldSqlName(delegate, fldHandle, fieldSQLName));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}
	}

	@Override
	public ReturnWithString getFieldStrValue(AMHandle queryHandle, long position, AMString target) {

		assertValidState();

		Future<ReturnWithString> resultJob = executorService
				.submit(new GetFieldStrValue(delegate, queryHandle, position, target));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long getFieldType(AMHandle fldHandle) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new GetFieldType(delegate, fldHandle));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long getFieldUserType(AMHandle fldHandle) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new GetFieldUserType(delegate, fldHandle));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public AMHandle getRecordFromMainId(String tableName, long id) {

		assertValidState();

		Future<AMHandle> resultJob = executorService.submit(new GetRecordFromMainId(delegate, tableName, id));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}
	}

	@Override
	public AMHandle getRecordHandle(AMHandle qryHandle) {

		assertValidState();

		Future<AMHandle> resultJob = executorService.submit(new GetRecordHandle(delegate, qryHandle));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long getRecordId(AMHandle recHandle) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new GetRecordId(delegate, recHandle));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public AMHandle getRelDstField(AMHandle fldHandle) {

		assertValidState();

		Future<AMHandle> resultJob = executorService.submit(new GetRelDstField(delegate, fldHandle));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}
	}

	@Override
	public AMHandle getRelSrcField(AMHandle fldHandle) {

		assertValidState();

		Future<AMHandle> resultJob = executorService.submit(new GetRelSrcField(delegate, fldHandle));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public AMHandle getRelTable(AMHandle fldHandle) {

		assertValidState();

		Future<AMHandle> resultJob = executorService.submit(new GetRelTable(delegate, fldHandle));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public AMHandle getReverseLink(AMHandle fldHandle) {

		assertValidState();

		Future<AMHandle> resultJob = executorService.submit(new GetReverseLink(delegate, fldHandle));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public ReturnWithString getSelfFromMainId(String tableName, long recordId, AMString recordDescription) {

		assertValidState();

		Future<ReturnWithString> resultJob = executorService
				.submit(new GetSelfFromMainId(delegate, tableName, recordId, recordDescription));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}
	}

	public int getServerPort() {
		return serverPort;
	}

	@Override
	public ReturnWithString getVersion(AMString amVersion) {

		assertValidState();

		Future<ReturnWithString> resultJob = executorService.submit(new GetVersion(delegate, amVersion));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}
	}

	private synchronized CallTimeOutException handleTimeOut(TimeoutException e) {

		lastException = new CallTimeOutException(e);

		return lastException;

	}

	@Override
	public long importDocument(long docId, String tableName, String fileName, String category, String designation) {

		assertValidState();

		Future<Long> resultJob = executorService
				.submit(new ImportDocument(delegate, docId, tableName, fileName, category, designation));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long insertRecord(AMHandle recHandle) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new InsertRecord(delegate, recHandle));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long isConnected() {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new IsConnected(delegate));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long lastError() {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new LastError(delegate));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public ReturnWithString lastErrorMsg(AMString errorMessage) {

		assertValidState();

		Future<ReturnWithString> resultJob = executorService.submit(new LastErrorMsg(delegate, errorMessage));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}
	}

	@Override
	public ReturnWithString listToString(AMString target, String source, String colSep, String lineSep, String idSep) {

		assertValidState();

		Future<ReturnWithString> resultJob = executorService
				.submit(new ListToString(delegate, target, source, colSep, lineSep, idSep));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long loginId() {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new LoginId(delegate));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public ReturnWithString loginName(AMString loginName) {

		assertValidState();

		Future<ReturnWithString> resultJob = executorService.submit(new LoginName(delegate, loginName));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}
	}

	@Override
	public AMHandle openConnection(String database, String username, String password) throws RemoteException {

		assertValidState();

		AMHandle connection = AMHandle.NULL;

		LOGGER.log(Level.FINE,
				String.format("Connection being requested, database=[%1$s], username=[%2$s]", database, username));

		Future<AMHandle> resultJob = executorService.submit(new OpenConnection(delegate, database, username, password));

		try {
			connection = resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);

			LOGGER.log(Level.FINE, String.format("Connection being returned has handle = [%1$s]", connection));

			return connection;
		} catch (TimeoutException e) {
			throw new AMConnectionException(new CallTimeOutException(e));
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long purgeRecord(AMHandle recHandle) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new PurgeRecord(delegate, recHandle));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public AMHandle queryCreate() {

		assertValidState();

		AMHandle handle = AMHandle.NULL;

		Future<AMHandle> resultJob = executorService.submit(new QueryCreate(delegate));

		try {
			handle = resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);

			LOGGER.log(Level.FINE, String.format("returning handle [%1$s]", handle));

			return handle;
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long queryExec(AMHandle queryHandle, String aqlQuery) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new QueryExec(delegate, queryHandle, aqlQuery));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long queryGet(AMHandle qryHandle, String aqlQuery) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new QueryGet(delegate, qryHandle, aqlQuery));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long queryNext(AMHandle qryHandle) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new QueryNext(delegate, qryHandle));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long querySetAddMainField(AMHandle qryHandle, long addMainField) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new QuerySetAddMainField(delegate, qryHandle, addMainField));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long querySetFullMemo(AMHandle qryHandle, long fullMemo) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new QuerySetFullMemo(delegate, qryHandle, fullMemo));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public AMHandle queryStartTable(AMHandle qryHandle) {

		assertValidState();

		Future<AMHandle> resultJob = executorService.submit(new QueryStartTable(delegate, qryHandle));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long queryStop(AMHandle qryHandle) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new QueryStop(delegate, qryHandle));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long refreshAllCaches() {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new RefreshAllCaches(delegate));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long releaseHandle(AMHandle handle) {

		assertValidState();

		LOGGER.log(Level.FINE, String.format("Trying to release handle [%1$s]", handle));

		Future<Long> resultJob = executorService.submit(new ReleaseHandle(delegate, handle));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	private void releaseRegisteredHandles() {

		assertValidState();

		Future<List<AMHandle>> resultJob = executorService.submit(new GetHandleKeys(delegate));

		try {
			List<AMHandle> keys = resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);

			if (!keys.isEmpty()) {

				LOGGER.log(Level.WARNING, "Registered Handles still exist, might have a leak.");

				for (AMHandle handle : keys) {
					releaseHandle(handle);
				}
			}

		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long rollBack() {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new RollBack(delegate));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public void setCallTimeOutInMs(long callTimeOutInMs) {
		this.callTimeOutInMs = callTimeOutInMs;
	}

	@Override
	public long setFieldDateOnlyValue(AMHandle recHandle, String fieldName, AMDate dateOnlyValue) {

		assertValidState();

		Future<Long> resultJob = executorService
				.submit(new SetFieldDateOnlyValue(delegate, recHandle, fieldName, dateOnlyValue));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long setFieldDateValue(AMHandle recHandle, String fieldName, AMDate dateTimeValue) {

		assertValidState();

		Future<Long> resultJob = executorService
				.submit(new SetFieldDateValue(delegate, recHandle, fieldName, dateTimeValue));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long setFieldDoubleValue(AMHandle recHandle, String fieldName, double value) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new SetFieldDoubleValue(delegate, recHandle, fieldName, value));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long setFieldLongValue(AMHandle recHandle, String fieldName, long value) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new SetFieldLongValue(delegate, recHandle, fieldName, value));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long setFieldStrValue(AMHandle recHandle, String fieldName, String value) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new SetFieldStrValue(delegate, recHandle, fieldName, value));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public void shutdown() {

		LOGGER.log(Level.INFO, "Shutdown requested.");

		try {

			Future<Void> resultJob = executorService.submit(new ShutDown(delegate));

			try {
				resultJob.get(15L, TimeUnit.SECONDS);
			} catch (ExecutionException | InterruptedException | TimeoutException e) {
				LOGGER.warning(String.format("Ignoring exception = [%1$s],  during shutdown request", e));
			}
		} finally {
			AMAPIProcess.shutdownSignal.countDown();
			executorService.shutdownNow();
		}

	}

	@Override
	public ReturnWithString sqlTextConst(String aqlQuery, AMString target) {

		assertValidState();

		Future<ReturnWithString> resultJob = executorService.submit(new SqlTextConst(delegate, aqlQuery, target));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long startTransaction() {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new StartTransaction(delegate));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

	@Override
	public long updateRecord(AMHandle recHandle) {

		assertValidState();

		Future<Long> resultJob = executorService.submit(new UpdateRecord(delegate, recHandle));

		try {
			return resultJob.get(getCallTimeOutInMs(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw handleTimeOut(e);
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}

	}

}
