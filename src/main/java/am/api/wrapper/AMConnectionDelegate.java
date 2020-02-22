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
package am.api.wrapper;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

import am.api.AMHandle;
import am.api.AMHandleType;
import am.api.exception.AMConnectionException;
import am.api.model.AMDate;
import am.api.model.AMHandleImpl;
import am.api.model.AMString;
import am.api.util.NLS;
import am.server.AMLibrary;
import am.server.AMLibraryFactory;

public class AMConnectionDelegate {

	private volatile AMLibrary amLibrary;

	private volatile Pointer connection;

	private final Map<AMHandle, HandleMetaData> handleMap;

	public AMConnectionDelegate() {
		super();
		amLibrary = AMLibraryFactory.getInstance();
		this.handleMap = new ConcurrentHashMap<AMHandle, HandleMetaData>();
	}

	private void checkInternalState() {
		if (connection == Pointer.NULL) {
			throw new IllegalStateException(NLS.ERRORS.getString("connection.invalid"));
		}
	}

	private void checkQueryHandle(Pointer pointer) {
		if (pointer == Pointer.NULL) {
			throw new IllegalStateException(NLS.ERRORS.getString("queryhandle.invalid"));
		}
	}

	public long clearLastError() {
		checkInternalState();
		return amLibrary.AmClearLastErrorW(connection).longValue();
	}

	public void close() {

		if (Pointer.NULL != connection) {
			amLibrary.AmCloseConnectionW(connection);
			connection = null;
		}

	}

	public long commit() {
		checkInternalState();

		return amLibrary.AmCommitW(connection).longValue();

	}

	public long connectionName(AMString connectionName) {
		checkInternalState();

		CharBuffer buffer = CharBuffer.allocate(connectionName.getBufferLength());

		long status = 0L;

		status = amLibrary.AmConnectionNameW(connection, buffer, new NativeLong(connectionName.getBufferLength()))
				.longValue();

		connectionName.fromString(Native.toString(buffer.array()));

		return status;

	}

	public long convertDateBasicToUnix(long tmTime) {
		checkInternalState();
		return amLibrary.AmConvertDateBasicToUnixW(connection, new NativeLong(tmTime)).longValue();
	}

	public long convertDateIntlToUnix(String dateAsString) {
		checkInternalState();
		return amLibrary.AmConvertDateIntlToUnixW(connection, new WString(dateAsString)).longValue();
	}

	public long convertDateStringToUnix(String dateAsString) {
		checkInternalState();
		return amLibrary.AmConvertDateStringToUnixW(connection, new WString(dateAsString)).longValue();
	}

	public long convertDateUnixToBasic(long dateAsUnix) {
		checkInternalState();
		return amLibrary.AmConvertDateUnixToBasicW(connection, new NativeLong(dateAsUnix)).longValue();
	}

	public long convertDateUnixToIntl(long unixDate, AMString dateAsIntlStr) {
		checkInternalState();

		long status = 0;

		CharBuffer buffer = CharBuffer.allocate(dateAsIntlStr.getBufferLength());

		status = amLibrary.AmConvertDateUnixToIntlW(connection, new NativeLong(unixDate), buffer,
				new NativeLong(dateAsIntlStr.getBufferLength())).longValue();

		dateAsIntlStr.fromString(Native.toString(buffer.array()));

		return status;
	}

	public long convertDateUnixToString(long dateAsUnix, AMString dateAsStr) {
		checkInternalState();

		long status = 0;

		CharBuffer buffer = CharBuffer.allocate(dateAsStr.getBufferLength());

		status = amLibrary.AmConvertDateUnixToStringW(connection, new NativeLong(dateAsUnix), buffer,
				new NativeLong(dateAsStr.getBufferLength())).longValue();

		dateAsStr.fromString(Native.toString(buffer.array()));

		return status;
	}

	public long convertDoubleToString(double dSrc, AMString dblAsString) {

		long status = 0;

		CharBuffer buffer = CharBuffer.allocate(dblAsString.getBufferLength());

		status = amLibrary.AmConvertDoubleToStringW(dSrc, buffer, new NativeLong(dblAsString.getBufferLength()))
				.longValue();

		dblAsString.fromString(Native.toString(buffer.array()));

		return status;
	}

	public long convertMonetaryToString(double dMonetarySrc, AMString dblAsString) {

		long status = 0;

		CharBuffer buffer = CharBuffer.allocate(dblAsString.getBufferLength());

		status = amLibrary
				.AmConvertMonetaryToStringW(dMonetarySrc, buffer, new NativeLong(dblAsString.getBufferLength()))
				.longValue();

		dblAsString.fromString(Native.toString(buffer.array()));

		return status;
	}

	public double convertStringToDouble(String dblAsString) {
		return amLibrary.AmConvertStringToDoubleW(new WString(dblAsString));
	}

	public double convertStringToMonetary(String monetaryAsString) {
		return amLibrary.AmConvertStringToMonetaryW(new WString(monetaryAsString));
	}

	public long createLink(AMHandle srcRecHandle, String linkName, AMHandle srcDstHandle) {

		checkInternalState();

		return amLibrary
				.AmCreateLinkW(handleAsPointer(srcRecHandle), new WString(linkName), handleAsPointer(srcDstHandle))
				.longValue();
	}

	public AMHandle createRecord(String tblName) {
		checkInternalState();

		Pointer pointer = amLibrary.AmCreateRecordW(connection, new WString(tblName));

		return registerHandle(pointer, AMHandleType.RECORD, "createRecord");
	}

	public long currentDate() {
		return amLibrary.AmCurrentDateW().longValue();
	}

	public long currentServerDate() {
		checkInternalState();
		return amLibrary.AmCurrentServerDateW(connection).longValue();
	}

	public long dateAdd(long startAsUnixDate, long duration) {
		return amLibrary.AmDateAddW(new NativeLong(startAsUnixDate), new NativeLong(duration)).longValue();
	}

	public long dateAddLogical(long startAsUnixDate, long duration) {
		return amLibrary.AmDateAddLogicalW(new NativeLong(startAsUnixDate), new NativeLong(duration)).longValue();
	}

	public long dateDiff(long endAsUnixDate, long startAsUnixDate) {
		return amLibrary.AmDateDiffW(new NativeLong(endAsUnixDate), new NativeLong(startAsUnixDate)).longValue();
	}

	public long dbExecAql(String aqlQuery) {
		checkInternalState();
		return amLibrary.AmDbExecAqlW(connection, new WString(aqlQuery)).longValue();
	}

	public long dbGetDate(String aqlQuery) {
		checkInternalState();
		return amLibrary.AmDbGetDateW(connection, new WString(aqlQuery)).longValue();
	}

	public double dbGetDouble(String aqlQuery) {
		checkInternalState();
		return amLibrary.AmDbGetDoubleW(connection, new WString(aqlQuery));
	}

	public long dbGetLimitedList(String aqlQuery, AMString result, String colSeperator, String lineSeperator,
			String idSeperator, long maxSize, long errorType) {
		checkInternalState();

		CharBuffer buffer = CharBuffer.allocate(result.getBufferLength());

		long status = 0;

		status = amLibrary.AmDbGetLimitedListW(connection, new WString(aqlQuery), buffer,
				new NativeLong(result.getBufferLength()), new WString(colSeperator), new WString(lineSeperator),
				new WString(idSeperator), new NativeLong(maxSize), new NativeLong(errorType)).longValue();

		result.fromString(Native.toString(buffer.array()));

		return status;
	}

	public long dbGetList(String aqlQuery, AMString result, String colSeperator, String lineSeperator,
			String idSeperator) {
		checkInternalState();

		CharBuffer buffer = CharBuffer.allocate(result.getBufferLength());

		long status = 0;

		status = amLibrary
				.AmDbGetListW(connection, new WString(aqlQuery), buffer, new NativeLong(result.getBufferLength()),
						new WString(colSeperator), new WString(lineSeperator), new WString(idSeperator))
				.longValue();

		result.fromString(Native.toString(buffer.array()));

		return status;
	}

	public long dbGetListEx(String aqlQuery, AMString result, String colSeperator, String lineSeperator,
			String idSeperator) {
		checkInternalState();

		CharBuffer buffer = CharBuffer.allocate(result.getBufferLength());

		long status = 0;

		status = amLibrary
				.AmDbGetListExW(connection, new WString(aqlQuery), buffer, new NativeLong(result.getBufferLength()),
						new WString(colSeperator), new WString(lineSeperator), new WString(idSeperator))
				.longValue();

		result.fromString(Native.toString(buffer.array()));

		return status;
	}

	public long dbGetLong(String aqlQuery) {
		checkInternalState();
		return amLibrary.AmDbGetLongW(connection, new WString(aqlQuery)).longValue();
	}

	public long dbGetPk(String tableName, String whereClause) {
		checkInternalState();
		return amLibrary.AmDbGetPkW(connection, new WString(tableName), new WString(whereClause)).longValue();
	}

	public long dbGetString(String query, AMString result, String colSeperator, String lineSeperator) {
		checkInternalState();

		CharBuffer buffer = CharBuffer.allocate(result.getBufferLength());

		long status = 0;

		status = amLibrary.AmDbGetStringW(connection, new WString(query), buffer,
				new NativeLong(result.getBufferLength()), new WString(colSeperator), new WString(lineSeperator))
				.longValue();

		result.fromString(Native.toString(buffer.array()));

		return status;
	}

	public long dbGetStringEx(String query, AMString result, String colSeperator, String lineSeperator) {
		checkInternalState();

		CharBuffer buffer = CharBuffer.allocate(result.getBufferLength());

		long status = 0;

		status = amLibrary.AmDbGetStringExW(connection, new WString(query), buffer,
				new NativeLong(result.getBufferLength()), new WString(colSeperator), new WString(lineSeperator))
				.longValue();

		result.fromString(Native.toString(buffer.array()));

		return status;
	}

	public long deleteLink(AMHandle srcRecHandle, String linkName, AMHandle srcDstHandle) {
		checkInternalState();

		return amLibrary
				.AmDeleteLinkW(handleAsPointer(srcRecHandle), new WString(linkName), handleAsPointer(srcDstHandle))
				.longValue();
	}

	public long deleteRecord(AMHandle recordHandle) {
		checkInternalState();

		return amLibrary.AmDeleteRecordW(handleAsPointer(recordHandle)).longValue();

	}

	public long duplicateRecord(AMHandle recordHandle, long insert) {
		checkInternalState();

		return amLibrary.AmDuplicateRecordW(handleAsPointer(recordHandle), new NativeLong(insert)).longValue();
	}

	public long enumValList(String enumName, AMString value, long caseSensitive, String lineSeperator) {
		checkInternalState();

		CharBuffer buffer = CharBuffer.allocate(value.getBufferLength());

		long status = 0;

		status = amLibrary.AmEnumValListW(connection, new WString(enumName), buffer,
				new NativeLong(value.getBufferLength()), new NativeLong(caseSensitive), new WString(lineSeperator))
				.longValue();

		value.fromString(Native.toString(buffer.array()));

		return status;
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
		AMConnectionDelegate rhs = (AMConnectionDelegate) obj;
		return new EqualsBuilder().appendSuper(super.equals(rhs)).append(connection, rhs.connection).isEquals();
	}

	public AMHandle executeActionById(long actionId, String tableName, long recordId) {
		checkInternalState();

		Pointer pointer = amLibrary.AmExecuteActionByIdW(connection, new NativeLong(actionId), new WString(tableName),
				new NativeLong(recordId));

		return registerHandle(pointer, AMHandleType.ACTION, "executeActionById");
	}

	public AMHandle executeActionByName(String sqlName, String tableName, long recordId) {
		checkInternalState();

		Pointer pointer = amLibrary.AmExecuteActionByNameW(connection, new WString(sqlName), new WString(tableName),
				new NativeLong(recordId));

		return registerHandle(pointer, AMHandleType.ACTION, "executeActionByName");
	}

	public long exportDocument(long documentId, String fileName) {
		checkInternalState();
		return amLibrary.AmExportDocumentW(connection, new NativeLong(documentId), new WString(fileName)).longValue();
	}

	public long flushTransaction() {
		checkInternalState();
		return amLibrary.AmFlushTransactionW(connection).longValue();
	}

	public long formatCurrency(double amount, String currency, AMString result) {
		checkInternalState();

		CharBuffer buffer = CharBuffer.allocate(result.getBufferLength());

		long status = 0;

		status = amLibrary
				.AmFormatCurrencyW(amount, new WString(currency), buffer, new NativeLong(result.getBufferLength()))
				.longValue();

		result.fromString(Native.toString(buffer.array()));

		return status;
	}

	public long formatLong(long number, String format, AMString result) {
		checkInternalState();

		CharBuffer buffer = CharBuffer.allocate(result.getBufferLength());

		long status = 0;

		status = amLibrary.AmFormatLongW(connection, new NativeLong(number), new WString(format), buffer,
				new NativeLong(result.getBufferLength())).longValue();

		result.fromString(Native.toString(buffer.array()));

		return status;
	}

	public long getComputeString(String tableName, long recordId, String template, AMString result) {
		checkInternalState();

		CharBuffer buffer = CharBuffer.allocate(result.getBufferLength());

		long status = 0;

		status = amLibrary.AmGetComputeStringW(connection, new WString(tableName), new NativeLong(recordId), template,
				buffer, new NativeLong(result.getBufferLength())).longValue();

		result.fromString(Native.toString(buffer.array()));

		return status;
	}

	public AMHandle getField(AMHandle objHandle, long position) {
		checkInternalState();

		return registerHandle(amLibrary.AmGetFieldW(handleAsPointer(objHandle), new NativeLong(position)),
				AMHandleType.FIELD, "getField");
	}

	public long getFieldCount(AMHandle objHandle) {
		checkInternalState();

		return amLibrary.AmGetFieldCountW(handleAsPointer(objHandle)).longValue();
	}

	public long getFieldDateOnlyValue(AMHandle recHandle, long fieldPos) {
		checkInternalState();

		return amLibrary.AmGetFieldDateOnlyValueW(handleAsPointer(recHandle), new NativeLong(fieldPos)).longValue();
	}

	public long getFieldDateValue(AMHandle recHandle, long fieldPos) {
		checkInternalState();

		return amLibrary.AmGetFieldDateValueW(handleAsPointer(recHandle), new NativeLong(fieldPos)).longValue();

	}

	public long getFieldDescription(AMHandle fieldHandle, AMString target) {
		checkInternalState();

		CharBuffer buffer = CharBuffer.allocate(target.getBufferLength());

		long status = 0;

		status = amLibrary
				.AmGetFieldDescriptionW(handleAsPointer(fieldHandle), buffer, new NativeLong(target.getBufferLength()))
				.longValue();

		target.fromString(Native.toString(buffer.array()));

		return status;

	}

	public double getFieldDoubleValue(AMHandle objHandle, long fieldPos) {
		checkInternalState();

		return amLibrary.AmGetFieldDoubleValueW(handleAsPointer(objHandle), new NativeLong(fieldPos));
	}

	public long getFieldFormat(AMHandle fldHandle, AMString target) {
		checkInternalState();

		CharBuffer buffer = CharBuffer.allocate(target.getBufferLength());

		long status = 0;

		status = amLibrary
				.AmGetFieldFormatW(handleAsPointer(fldHandle), buffer, new NativeLong(target.getBufferLength()))
				.longValue();

		target.fromString(Native.toString(buffer.array()));

		return status;
	}

	public long getFieldFormatFromName(String tblName, String fldName, AMString result) {
		checkInternalState();

		CharBuffer buffer = CharBuffer.allocate(result.getBufferLength());

		long status = 0;

		status = amLibrary.AmGetFieldFormatFromNameW(connection, new WString(tblName), new WString(fldName), buffer,
				new NativeLong(result.getBufferLength())).longValue();

		result.fromString(Native.toString(buffer.array()));

		return status;
	}

	public AMHandle getFieldFromName(AMHandle objHandle, String fldName) {
		checkInternalState();

		return registerHandle(amLibrary.AmGetFieldFromName(handleAsPointer(objHandle), fldName), AMHandleType.FIELD,
				"getFieldFromName");
	}

	public long getFieldLabel(AMHandle fldHandle, AMString result) {
		checkInternalState();

		CharBuffer buffer = CharBuffer.allocate(result.getBufferLength());

		long status = 0;

		status = amLibrary
				.AmGetFieldLabelW(handleAsPointer(fldHandle), buffer, new NativeLong(result.getBufferLength()))
				.longValue();

		result.fromString(Native.toString(buffer.array()));

		return status;

	}

	public long getFieldLabelFromName(String tableName, String fieldName, AMString fieldLabel) {
		checkInternalState();

		CharBuffer buffer = CharBuffer.allocate(fieldLabel.getBufferLength());

		long status = 0;

		status = amLibrary.AmGetFieldLabelFromNameW(connection, new WString(tableName), new WString(fieldName), buffer,
				new NativeLong(fieldLabel.getBufferLength())).longValue();

		fieldLabel.fromString(Native.toString(buffer.array()));

		return status;
	}

	public long getFieldLongValue(AMHandle objHandle, long fieldPosition) {
		checkInternalState();

		return amLibrary.AmGetFieldLongValue(handleAsPointer(objHandle), new NativeLong(fieldPosition)).longValue();

	}

	public long getFieldName(AMHandle objHandle, long fldPosition, AMString fieldName) {
		checkInternalState();

		CharBuffer buffer = CharBuffer.allocate(fieldName.getBufferLength());

		long status = 0;

		status = amLibrary.AmGetFieldNameW(handleAsPointer(objHandle), new NativeLong(fldPosition), buffer,
				new NativeLong(fieldName.getBufferLength())).longValue();

		fieldName.fromString(Native.toString(buffer.array()));

		return status;

	}

	public long getFieldSize(AMHandle fldHandle) {
		checkInternalState();

		return amLibrary.AmGetFieldSize(handleAsPointer(fldHandle)).longValue();
	}

	public long getFieldSqlName(AMHandle fldHandle, AMString fieldSQLName) {
		checkInternalState();

		CharBuffer buffer = CharBuffer.allocate(fieldSQLName.getBufferLength());

		long status = 0;

		status = amLibrary
				.AmGetFieldSqlNameW(handleAsPointer(fldHandle), buffer, new NativeLong(fieldSQLName.getBufferLength()))
				.longValue();

		fieldSQLName.fromString(Native.toString(buffer.array()));

		return status;
	}

	public long getFieldStrValue(AMHandle queryHandle, long position, AMString target) {
		checkInternalState();

		checkQueryHandle(handleAsPointer(queryHandle));

		long status = 0;

		CharBuffer buffer = CharBuffer.allocate(target.getBufferLength());

		status = amLibrary.AmGetFieldStrValueW(handleAsPointer(queryHandle), new NativeLong(position), buffer,
				new NativeLong(target.getBufferLength())).longValue();

		target.fromString(Native.toString(buffer.array()));

		return status;

	}

	public long getFieldType(AMHandle fldHandle) {
		checkInternalState();

		return amLibrary.AmGetFieldTypeW(handleAsPointer(fldHandle)).longValue();
	}

	public long getFieldUserType(AMHandle fldHandle) {
		checkInternalState();

		return amLibrary.AmGetFieldUserTypeW(handleAsPointer(fldHandle)).longValue();
	}

	public AMHandle getRecordFromMainId(String tableName, long id) {

		checkInternalState();

		return registerHandle(amLibrary.AmGetRecordFromMainIdW(connection, new WString(tableName), new NativeLong(id)),
				AMHandleType.RECORD, "getRecordFromMainId");

	}

	public AMHandle getRecordHandle(AMHandle qryHandle) {

		checkInternalState();

		return registerHandle(amLibrary.AmGetRecordHandleW(handleAsPointer(qryHandle)), AMHandleType.RECORD,
				"getRecordHandle");

	}

	public long getRecordId(AMHandle recHandle) {
		checkInternalState();

		return amLibrary.AmGetRecordIdW(handleAsPointer(recHandle)).longValue();
	}

	public AMHandle getRelDstField(AMHandle fldHandle) {
		checkInternalState();

		return registerHandle(amLibrary.AmGetRelDstFieldW(handleAsPointer(fldHandle)), AMHandleType.FIELD,
				"getRelDstField");
	}

	public AMHandle getRelSrcField(AMHandle fldHandle) {
		checkInternalState();

		return registerHandle(amLibrary.AmGetRelSrcFieldW(handleAsPointer(fldHandle)), AMHandleType.FIELD,
				"getRelSrcField");

	}

	public AMHandle getRelTable(AMHandle fldHandle) {
		checkInternalState();

		return registerHandle(amLibrary.AmGetRelTableW(handleAsPointer(fldHandle)), AMHandleType.FIELD, "getRelTable");
	}

	public AMHandle getReverseLink(AMHandle fldHandle) {
		checkInternalState();

		return registerHandle(amLibrary.AmGetReverseLinkW(handleAsPointer(fldHandle)), AMHandleType.FIELD,
				"getReverseLink");
	}

	public long getSelfFromMainId(String tableName, long recordId, AMString recordDescription) {
		checkInternalState();

		CharBuffer buffer = CharBuffer.allocate(recordDescription.getBufferLength());

		long status = 0;

		status = amLibrary.AmGetSelfFromMainIdW(connection, new WString(tableName), new NativeLong(recordId), buffer,
				new NativeLong(recordDescription.getBufferLength())).longValue();

		recordDescription.fromString(Native.toString(buffer.array()));

		return status;
	}

	public long getVersion(AMString amVersion) {
		checkInternalState();

		CharBuffer buffer = CharBuffer.allocate(amVersion.getBufferLength());

		long status = 0;

		status = amLibrary.AmGetVersionW(buffer, new NativeLong(amVersion.getBufferLength())).longValue();

		amVersion.fromString(Native.toString(buffer.array()));

		return status;
	}

	private Pointer handleAsPointer(AMHandle handle) {

		Pointer pointer = Pointer.NULL;

		HandleMetaData mHandle = handleMap.get(handle);

		if (mHandle != null) {
			pointer = mHandle.getPointer();
		}

		return pointer;
	}

	public int hashCode() {
		return new HashCodeBuilder(99, 97).appendSuper(super.hashCode()).append(connection).toHashCode();

	}

	public long importDocument(long docId, String tableName, String fileName, String category, String designation) {
		checkInternalState();
		return amLibrary.AmImportDocumentW(connection, new NativeLong(docId), new WString(tableName),
				new WString(fileName), new WString(category), new WString(designation)).longValue();
	}

	public long insertRecord(AMHandle recHandle) {
		checkInternalState();

		long id = 0L;

		id = amLibrary.AmInsertRecordW(handleAsPointer(recHandle)).longValue();

		return id;
	}

	public long isConnected() {
		return amLibrary.AmIsConnectedW(connection).longValue();
	}

	public long lastError() {
		return amLibrary.AmLastErrorW(connection).longValue();
	}

	public long lastErrorMsg(AMString errorMessage) {

		CharBuffer buffer = CharBuffer.allocate(errorMessage.getBufferLength());

		long status = 0;

		status = amLibrary.AmLastErrorMsgW(connection, buffer, new NativeLong(errorMessage.getBufferLength()))
				.longValue();

		errorMessage.fromString(Native.toString(buffer.array()));

		return status;
	}

	public long listToString(AMString target, String source, String colSep, String lineSep, String idSep) {

		CharBuffer buffer = CharBuffer.allocate(target.getBufferLength());

		long status = 0;

		status = amLibrary.AmListToStringW(new WString(source), new WString(colSep), new WString(lineSep),
				new WString(idSep), buffer, new NativeLong(target.getBufferLength())).longValue();

		target.fromString(Native.toString(buffer.array()));

		return status;
	}

	public long loginId() {
		checkInternalState();
		return amLibrary.AmLoginIdW(connection).longValue();
	}

	public long loginName(AMString loginName) {
		checkInternalState();

		CharBuffer buffer = CharBuffer.allocate(loginName.getBufferLength());

		long status = 0;

		status = amLibrary.AmLoginNameW(connection, buffer, new NativeLong(loginName.getBufferLength())).longValue();

		loginName.fromString(Native.toString(buffer.array()));

		return status;
	}

	public AMHandle openConnection(String database, String username, String password) {

		if (connection != Pointer.NULL) {
			throw new UnsupportedOperationException(NLS.ERRORS.getString("connection.multiple.open"));
		} else {

			Pointer newConnection = amLibrary.AmOpenConnectionW(new WString(database), new WString(username),
					new WString(password));

			if (newConnection == Pointer.NULL) {

				AMString errorMessage = AMString.create(255);

				CharBuffer buffer = CharBuffer.allocate(errorMessage.getBufferLength());

				amLibrary.AmLastErrorMsgW(connection, buffer, new NativeLong(errorMessage.getBufferLength()));

				errorMessage.fromString(Native.toString(buffer.array()));

				throw new AMConnectionException(String.format(NLS.ERRORS.getString("login.failure"), database, username,
						errorMessage.toString()));

			} else {
				connection = newConnection;
			}

		}

		// Assertion

		if (1L != amLibrary.AmIsConnectedW(connection).longValue()) {
			throw new AMConnectionException(NLS.ERRORS.getString("connection.success.notconnected"));
		}

		return new AMHandleImpl(Integer.toString(connection.hashCode()), AMHandleType.CONNECTION, "openConnection");
	}

	public long purgeRecord(AMHandle recHandle) {
		checkInternalState();

		return amLibrary.AmPurgeRecordW(handleAsPointer(recHandle)).longValue();
	}

	public AMHandle queryCreate() {
		checkInternalState();

		return registerHandle(amLibrary.AmQueryCreateW(connection), AMHandleType.QUERY, "queryCreate");
	}

	public long queryExec(AMHandle queryHandle, String aqlQuery) {
		checkInternalState();

		checkQueryHandle(handleAsPointer(queryHandle));

		return amLibrary.AmQueryExecW(handleAsPointer(queryHandle), new WString(aqlQuery)).longValue();
	}

	public long queryGet(AMHandle qryHandle, String aqlQuery) {
		checkInternalState();

		return amLibrary.AmQueryGetW(handleAsPointer(qryHandle), new WString(aqlQuery)).longValue();
	}

	public long queryNext(AMHandle qryHandle) {
		checkInternalState();

		checkQueryHandle(handleAsPointer(qryHandle));

		return amLibrary.AmQueryNextW(handleAsPointer(qryHandle)).longValue();
	}

	public long querySetAddMainField(AMHandle qryHandle, long addMainField) {
		checkInternalState();

		checkQueryHandle(handleAsPointer(qryHandle));

		return amLibrary.AmQuerySetAddMainFieldW(handleAsPointer(qryHandle), new NativeLong(addMainField)).longValue();
	}

	public long querySetFullMemo(AMHandle qryHandle, long fullMemo) {
		checkInternalState();

		checkQueryHandle(handleAsPointer(qryHandle));

		return amLibrary.AmQuerySetFullMemoW(handleAsPointer(qryHandle), new NativeLong(fullMemo)).longValue();
	}

	public AMHandle queryStartTable(AMHandle qryHandle) {
		checkInternalState();

		checkQueryHandle(handleAsPointer(qryHandle));

		return registerHandle(amLibrary.AmQueryStartTableW(handleAsPointer(qryHandle)), AMHandleType.QUERY,
				"queryStartTable");
	}

	public long queryStop(AMHandle qryHandle) {
		checkInternalState();

		return amLibrary.AmQueryStopW(handleAsPointer(qryHandle)).longValue();
	}

	public long refreshAllCaches() {

		checkInternalState();

		long returnValue;

		returnValue = amLibrary.AmRefreshAllCachesW(connection).longValue();

		return returnValue;

	}

	private AMHandle registerHandle(Pointer pointer, AMHandleType handleType, String functionSource) {

		AMHandle handle = AMHandle.NULL;

		if (pointer != Pointer.NULL) {

			handle = new AMHandleImpl(Integer.toString(pointer.hashCode()), handleType, functionSource);

			if (!handleMap.containsKey(handle)) {
				handleMap.put(handle, new HandleMetaData(pointer));
			}

		}

		return handle;

	}

	public long releaseHandle(AMHandle handle) {

		checkInternalState();

		long status = 0;

		if (handle != AMHandle.NULL) {

			HandleMetaData mHandle = handleMap.remove(handle);

			if (mHandle != null) {

				AMHandleType handleType = ((AMHandleImpl) handle).getHandleType();

				switch (handleType) {
				/*
				 * I believe only QUERY handles and RECORD handles needs to be released, not
				 * sure about ACTIONS but definitely not FIELD handles.
				 */
				case QUERY:
				case RECORD:
					status = amLibrary.AmReleaseHandleW(mHandle.getPointer()).longValue();
					break;
				default:
					status = 1L;
				}

			}

		}

		return status;

	}

	public long rollBack() {
		return amLibrary.AmRollbackW(connection).longValue();
	}

	public long setFieldDateOnlyValue(AMHandle recHandle, String fieldName, AMDate dateOnlyValue) {
		checkInternalState();

		return amLibrary.AmSetFieldDateOnlyValueW(handleAsPointer(recHandle), new WString(fieldName),
				new NativeLong(dateOnlyValue.getValue())).longValue();
	}

	public long setFieldDateValue(AMHandle recHandle, String fieldName, AMDate dateTimeValue) {
		checkInternalState();

		return amLibrary.AmSetFieldDateValueW(handleAsPointer(recHandle), new WString(fieldName),
				new NativeLong(dateTimeValue.getValue())).longValue();
	}

	public long setFieldDoubleValue(AMHandle recHandle, String fieldName, double value) {
		checkInternalState();

		return amLibrary.AmSetFieldDoubleValueW(handleAsPointer(recHandle), new WString(fieldName), value).longValue();
	}

	public long setFieldLongValue(AMHandle recHandle, String fieldName, long value) {
		checkInternalState();

		return amLibrary.AmSetFieldLongValueW(handleAsPointer(recHandle), new WString(fieldName), new NativeLong(value))
				.longValue();
	}

	public long setFieldStrValue(AMHandle recHandle, String fieldName, String value) {
		checkInternalState();

		return amLibrary.AmSetFieldStrValueW(handleAsPointer(recHandle), new WString(fieldName), new WString(value))
				.longValue();
	}

	public void shutdown() {

		close();

		amLibrary = null;
		connection = null;

		handleMap.clear();

	}

	public long sqlTextConst(String aqlQuery, AMString target) {
		checkInternalState();

		CharBuffer buffer = CharBuffer.allocate(target.getBufferLength());

		long status = 0;

		status = amLibrary.AmSqlTextConstW(new WString(aqlQuery), buffer, new NativeLong(target.getBufferLength()))
				.longValue();

		target.fromString(Native.toString(buffer.array()));

		return status;
	}

	public long startTransaction() {

		checkInternalState();

		return amLibrary.AmStartTransactionW(connection).longValue();

	}

	public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).append("connection", connection).toString();
	}

	public long updateRecord(AMHandle recHandle) {
		checkInternalState();

		return amLibrary.AmUpdateRecordW(handleAsPointer(recHandle)).longValue();
	}

	public List<AMHandle> getHandleKeys() {
		return new ArrayList<AMHandle>(handleMap.keySet());
	}

}
