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
package am.api.wrapper.local;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Logger;

import am.api.AMHandle;
import am.api.model.AMCredential;
import am.api.model.AMDate;
import am.api.model.AMString;
import am.api.wrapper.AMBaseConnection;
import am.api.wrapper.AMConnectionDelegate;
import am.api.wrapper.ConnectionPool;

public final class AMConnectionLocal extends AMBaseConnection {

	private final static Logger logger = Logger.getLogger(AMConnectionLocal.class);

	private final AMCredential credential;

	private final AMConnectionDelegate delegate;

	private final ConnectionPool pool;

	public AMConnectionLocal(ConnectionPool pool, AMCredential credential) {
		super();
		this.pool = pool;
		this.credential = credential;
		this.delegate = new AMConnectionDelegate();
	}

	@Override
	public void cleanup() {

		clearLastError();

		releaseRegisteredHandles();

	}

	@Override
	public long clearLastError() {

		return delegate.clearLastError();
	}

	@Override
	public void close() {

		if (logger.isDebugEnabled()) {
			logger.debug("Current Thread = {}", Thread.currentThread().toString());
		}

		releaseRegisteredHandles();

		if (pool != null) {

			logger.debug(String.format("Returning connection [%s] to [%s]", this, pool));

			pool.returnObject(credential, this);

		} else {

			delegate.close();

		}

	}

	@Override
	public long commit() {

		return delegate.commit();

	}

	@Override
	public long connectionName(AMString connectionName) {

		return delegate.connectionName(connectionName);

	}

	@Override
	public long convertDateBasicToUnix(long tmTime) {

		return delegate.convertDateBasicToUnix(tmTime);

	}

	@Override
	public long convertDateIntlToUnix(String dateAsString) {

		return delegate.convertDateIntlToUnix(dateAsString);

	}

	@Override
	public long convertDateStringToUnix(String dateAsString) {

		return delegate.convertDateStringToUnix(dateAsString);

	}

	@Override
	public long convertDateUnixToBasic(long dateAsUnix) {

		return delegate.convertDateUnixToBasic(dateAsUnix);

	}

	@Override
	public long convertDateUnixToIntl(long unixDate, AMString dateAsIntlStr) {

		return delegate.convertDateUnixToIntl(unixDate, dateAsIntlStr);

	}

	@Override
	public long convertDateUnixToString(long dateAsUnix, AMString dateAsStr) {

		return delegate.convertDateUnixToString(dateAsUnix, dateAsStr);

	}

	@Override
	public long convertDoubleToString(double dSrc, AMString dblAsString) {

		return delegate.convertDoubleToString(dSrc, dblAsString);

	}

	@Override
	public long convertMonetaryToString(double dMonetarySrc, AMString dblAsString) {

		return delegate.convertMonetaryToString(dMonetarySrc, dblAsString);

	}

	@Override
	public double convertStringToDouble(String dblAsString) {

		return delegate.convertStringToDouble(dblAsString);

	}

	@Override
	public double convertStringToMonetary(String monetaryAsString) {

		return delegate.convertStringToMonetary(monetaryAsString);

	}

	@Override
	public long createLink(AMHandle srcRecHandle, String linkName, AMHandle srcDstHandle) {

		return delegate.createLink(srcRecHandle, linkName, srcDstHandle);

	}

	@Override
	public AMHandle createRecord(String tblName) {

		AMHandle handle = delegate.createRecord(tblName);

		logger.debug("Returning handle [{}]", handle);

		return handle;

	}

	@Override
	public long currentDate() {

		return delegate.currentDate();

	}

	@Override
	public long currentServerDate() {

		return delegate.currentServerDate();

	}

	@Override
	public long dateAdd(long startAsUnixDate, long duration) {

		return delegate.dateAdd(startAsUnixDate, duration);

	}

	@Override
	public long dateAddLogical(long startAsUnixDate, long duration) {

		return delegate.dateAddLogical(startAsUnixDate, duration);

	}

	@Override
	public long dateDiff(long endAsUnixDate, long startAsUnixDate) {

		return delegate.dateDiff(endAsUnixDate, startAsUnixDate);

	}

	@Override
	public long dbExecAql(String aqlQuery) {

		return delegate.dbExecAql(aqlQuery);

	}

	@Override
	public long dbGetDate(String aqlQuery) {

		return delegate.dbGetDate(aqlQuery);

	}

	@Override
	public double dbGetDouble(String aqlQuery) {

		return delegate.dbGetDouble(aqlQuery);

	}

	@Override
	public long dbGetLimitedList(String aqlQuery, AMString result, String colSeperator, String lineSeperator,
			String idSeperator, long maxSize, long errorType) {

		return delegate.dbGetLimitedList(aqlQuery, result, colSeperator, lineSeperator, idSeperator, maxSize,
				errorType);

	}

	@Override
	public long dbGetList(String aqlQuery, AMString result, String colSeperator, String lineSeperator,
			String idSeperator) {

		return delegate.dbGetList(aqlQuery, result, colSeperator, lineSeperator, idSeperator);

	}

	@Override
	public long dbGetListEx(String aqlQuery, AMString result, String colSeperator, String lineSeperator,
			String idSeperator) {

		return delegate.dbGetListEx(aqlQuery, result, colSeperator, lineSeperator, idSeperator);

	}

	@Override
	public long dbGetLong(String aqlQuery) {

		return delegate.dbGetLong(aqlQuery);
	}

	@Override
	public long dbGetPk(String tableName, String whereClause) {

		return delegate.dbGetPk(tableName, whereClause);

	}

	@Override
	public long dbGetString(String query, AMString result, String colSeperator, String lineSeperator) {

		return delegate.dbGetString(query, result, colSeperator, lineSeperator);

	}

	@Override
	public long dbGetStringEx(String query, AMString result, String colSeperator, String lineSeperator) {

		return delegate.dbGetStringEx(query, result, colSeperator, lineSeperator);

	}

	@Override
	public long deleteLink(AMHandle srcRecHandle, String linkName, AMHandle srcDstHandle) {

		return delegate.deleteLink(srcRecHandle, linkName, srcDstHandle);

	}

	@Override
	public long deleteRecord(AMHandle recordHandle) {

		return delegate.deleteRecord(recordHandle);

	}

	@Override
	public long duplicateRecord(AMHandle recordHandle, long insert) {

		return delegate.duplicateRecord(recordHandle, insert);

	}

	@Override
	public long enumValList(String enumName, AMString value, long caseSensitive, String lineSeperator) {

		return delegate.enumValList(enumName, value, caseSensitive, lineSeperator);

	}

	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		AMConnectionLocal rhs = (AMConnectionLocal) obj;
		return new EqualsBuilder().appendSuper(super.equals(rhs)).append(delegate, rhs.delegate)
				.append(credential, rhs.credential).isEquals();
	}

	@Override
	public AMHandle executeActionById(long actionId, String tableName, long recordId) {

		AMHandle handle = delegate.executeActionById(actionId, tableName, recordId);

		logger.debug("Returning handle [{}]", handle);

		return handle;

	}

	@Override
	public AMHandle executeActionByName(String sqlName, String tableName, long recordId) {

		AMHandle handle = delegate.executeActionByName(sqlName, tableName, recordId);

		logger.debug("Returning handle [{}]", handle);

		return handle;

	}

	@Override
	public long exportDocument(long documentId, String fileName) {

		return delegate.exportDocument(documentId, fileName);

	}

	@Override
	public long flushTransaction() {

		return delegate.flushTransaction();

	}

	@Override
	public long formatCurrency(double amount, String currency, AMString result) {

		return delegate.formatCurrency(amount, currency, result);

	}

	@Override
	public long formatLong(long number, String format, AMString result) {

		return delegate.formatLong(number, format, result);
	}

	@Override
	public long getComputeString(String tableName, long recordId, String template, AMString result) {

		return delegate.getComputeString(tableName, recordId, template, result);

	}

	@Override
	public AMHandle getField(AMHandle objHandle, long position) {

		AMHandle handle = delegate.getField(objHandle, position);

		logger.debug("Returning handle [{}]", handle);

		return handle;

	}

	@Override
	public long getFieldCount(AMHandle objHandle) {

		return delegate.getFieldCount(objHandle);

	}

	@Override
	public long getFieldDateOnlyValue(AMHandle recHandle, long fieldPos) {

		return delegate.getFieldDateOnlyValue(recHandle, fieldPos);

	}

	@Override
	public long getFieldDateValue(AMHandle recHandle, long fieldPos) {

		return delegate.getFieldDateValue(recHandle, fieldPos);

	}

	@Override
	public long getFieldDescription(AMHandle fieldHandle, AMString target) {

		return delegate.getFieldDescription(fieldHandle, target);

	}

	@Override
	public double getFieldDoubleValue(AMHandle objHandle, long fieldPos) {

		return delegate.getFieldDoubleValue(objHandle, fieldPos);
	}

	@Override
	public long getFieldFormat(AMHandle fldHandle, AMString target) {

		return delegate.getFieldFormat(fldHandle, target);

	}

	@Override
	public long getFieldFormatFromName(String tblName, String fldName, AMString result) {

		return delegate.getFieldFormatFromName(tblName, fldName, result);

	}

	@Override
	public AMHandle getFieldFromName(AMHandle objHandle, String fldName) {

		AMHandle handle = delegate.getFieldFromName(objHandle, fldName);

		logger.debug("Returning handle [{}]", handle);

		return handle;

	}

	@Override
	public long getFieldLabel(AMHandle fldHandle, AMString result) {

		return delegate.getFieldLabel(fldHandle, result);

	}

	@Override
	public long getFieldLabelFromName(String tableName, String fieldName, AMString fieldLabel) {

		return delegate.getFieldLabelFromName(tableName, fieldName, fieldLabel);

	}

	@Override
	public long getFieldLongValue(AMHandle objHandle, long fieldPosition) {

		return delegate.getFieldLongValue(objHandle, fieldPosition);

	}

	@Override
	public long getFieldName(AMHandle objHandle, long fldPosition, AMString fieldName) {

		return delegate.getFieldName(objHandle, fldPosition, fieldName);

	}

	@Override
	public long getFieldSize(AMHandle fldHandle) {

		return delegate.getFieldSize(fldHandle);

	}

	@Override
	public long getFieldSqlName(AMHandle fldHandle, AMString fieldSQLName) {

		return delegate.getFieldSqlName(fldHandle, fieldSQLName);

	}

	@Override
	public long getFieldStrValue(AMHandle queryHandle, long position, AMString target) {

		return delegate.getFieldStrValue(queryHandle, position, target);

	}

	@Override
	public long getFieldType(AMHandle fldHandle) {

		return delegate.getFieldType(fldHandle);

	}

	@Override
	public long getFieldUserType(AMHandle fldHandle) {

		return delegate.getFieldUserType(fldHandle);

	}

	@Override
	public AMHandle getRecordFromMainId(String tableName, long id) {

		AMHandle handle = delegate.getRecordFromMainId(tableName, id);

		logger.debug("Returning handle [{}]", handle);

		return handle;

	}

	@Override
	public AMHandle getRecordHandle(AMHandle qryHandle) {

		AMHandle handle = delegate.getRecordHandle(qryHandle);

		logger.debug("Returning handle [{}]", handle);

		return handle;

	}

	@Override
	public long getRecordId(AMHandle recHandle) {

		return delegate.getRecordId(recHandle);
	}

	@Override
	public AMHandle getRelDstField(AMHandle fldHandle) {

		AMHandle handle = delegate.getRelDstField(fldHandle);

		logger.debug("Returning handle [{}]", handle);

		return handle;

	}

	@Override
	public AMHandle getRelSrcField(AMHandle fldHandle) {

		AMHandle handle = delegate.getRelSrcField(fldHandle);

		logger.debug("Returning handle [{}]", handle);

		return handle;

	}

	@Override
	public AMHandle getRelTable(AMHandle fldHandle) {

		AMHandle handle = delegate.getRelTable(fldHandle);

		logger.debug("Returning handle [{}]", handle);

		return handle;

	}

	@Override
	public AMHandle getReverseLink(AMHandle fldHandle) {

		AMHandle handle = delegate.getReverseLink(fldHandle);

		logger.debug("Returning handle [{}]", handle);

		return handle;

	}

	@Override
	public long getSelfFromMainId(String tableName, long recordId, AMString recordDescription) {

		return delegate.getSelfFromMainId(tableName, recordId, recordDescription);

	}

	@Override
	public long getVersion(AMString amVersion) {

		return delegate.getVersion(amVersion);

	}

	public int hashCode() {
		return new HashCodeBuilder(99, 97).appendSuper(super.hashCode()).append(delegate).append(credential)
				.toHashCode();
	}

	@Override
	public long importDocument(long docId, String tableName, String fileName, String category, String designation) {

		return delegate.importDocument(docId, tableName, fileName, category, designation);

	}

	@Override
	public long insertRecord(AMHandle recHandle) {

		return delegate.insertRecord(recHandle);

	}

	@Override
	public long isConnected() {

		return delegate.isConnected();

	}

	@Override
	public long lastError() {

		return delegate.lastError();

	}

	@Override
	public long lastErrorMsg(AMString errorMessage) {

		return delegate.lastErrorMsg(errorMessage);

	}

	@Override
	public long listToString(AMString target, String source, String colSep, String lineSep, String idSep) {

		return delegate.listToString(target, source, colSep, lineSep, idSep);

	}

	@Override
	public long loginId() {

		return delegate.loginId();

	}

	@Override
	public long loginName(AMString loginName) {

		return delegate.loginName(loginName);

	}

	@Override
	public AMHandle openConnection(String database, String username, String password) {

		AMHandle handle = delegate.openConnection(database, username, password);

		logger.debug("Returning handle [{}]", handle);

		return handle;

	}

	@Override
	public long purgeRecord(AMHandle recHandle) {

		return delegate.purgeRecord(recHandle);

	}

	@Override
	public AMHandle queryCreate() {

		AMHandle handle = delegate.queryCreate();

		logger.debug("Returning handle [{}]", handle);

		return handle;

	}

	@Override
	public long queryExec(AMHandle queryHandle, String aqlQuery) {

		return delegate.queryExec(queryHandle, aqlQuery);

	}

	@Override
	public long queryGet(AMHandle qryHandle, String aqlQuery) {

		return delegate.queryGet(qryHandle, aqlQuery);

	}

	@Override
	public long queryNext(AMHandle qryHandle) {

		return delegate.queryNext(qryHandle);

	}

	@Override
	public long querySetAddMainField(AMHandle qryHandle, long addMainField) {

		return delegate.querySetAddMainField(qryHandle, addMainField);

	}

	@Override
	public long querySetFullMemo(AMHandle qryHandle, long fullMemo) {

		return delegate.querySetFullMemo(qryHandle, fullMemo);

	}

	@Override
	public AMHandle queryStartTable(AMHandle qryHandle) {

		AMHandle handle = delegate.queryStartTable(qryHandle);

		logger.debug("Returning handle [{}]", handle);

		return handle;
	}

	@Override
	public long queryStop(AMHandle qryHandle) {

		return delegate.queryStop(qryHandle);

	}

	@Override
	public long refreshAllCaches() {

		return delegate.refreshAllCaches();

	}

	@Override
	public long releaseHandle(AMHandle handle) {

		logger.debug("Releasing handle [{}]", handle);

		return delegate.releaseHandle(handle);

	}

	private void releaseRegisteredHandles() {

		List<AMHandle> keys = delegate.getHandleKeys();

		if (!keys.isEmpty()) {

			logger.warn("Registered Handles still exist, might have a leak.");

			for (AMHandle handle : keys) {
				logger.warn("Trying to release amHandle=[{}].", handle);
				delegate.releaseHandle(handle);
			}
		}

	}

	@Override
	public void resetCallTimeOut() {

		// does nothing

	}

	@Override
	public long rollBack() {

		return delegate.rollBack();

	}

	@Override
	public void setCallTimeOutInMs(long timeOutInMs) {

		// does nothing in this implementation

	}

	@Override
	public long setFieldDateOnlyValue(AMHandle recHandle, String fieldName, AMDate dateOnlyValue) {

		return delegate.setFieldDateOnlyValue(recHandle, fieldName, dateOnlyValue);

	}

	@Override
	public long setFieldDateValue(AMHandle recHandle, String fieldName, AMDate dateTimeValue) {

		return delegate.setFieldDateValue(recHandle, fieldName, dateTimeValue);

	}

	@Override
	public long setFieldDoubleValue(AMHandle recHandle, String fieldName, double value) {

		return delegate.setFieldDoubleValue(recHandle, fieldName, value);

	}

	@Override
	public long setFieldLongValue(AMHandle recHandle, String fieldName, long value) {

		return delegate.setFieldLongValue(recHandle, fieldName, value);

	}

	@Override
	public long setFieldStrValue(AMHandle recHandle, String fieldName, String value) {

		return delegate.setFieldStrValue(recHandle, fieldName, value);

	}

	public void shutdown() {

		logger.debug("shutdown called");

		releaseRegisteredHandles();

		delegate.close();

	}

	@Override
	public long sqlTextConst(String aqlQuery, AMString target) {

		return delegate.sqlTextConst(aqlQuery, target);

	}

	@Override
	public long startTransaction() {

		return delegate.startTransaction();

	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).append("delegate", delegate)
				.append("credential", credential).toString();
	}

	@Override
	public long updateRecord(AMHandle recHandle) {

		return delegate.updateRecord(recHandle);

	}

}
