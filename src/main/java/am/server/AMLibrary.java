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

import java.nio.CharBuffer;

import com.sun.jna.Library;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

public interface AMLibrary extends Library {

	void AmCleanup();

	NativeLong AmClearLastErrorW(Pointer connHandle);

	NativeLong AmCloseConnectionW(Pointer connHandle);

	NativeLong AmCommitW(Pointer connHandle);

	NativeLong AmConnectionNameW(Pointer connHandle, CharBuffer buffer, NativeLong bufferLen);

	NativeLong AmConvertDateBasicToUnixW(Pointer hApiCnxBase, NativeLong tmTime);

	NativeLong AmConvertDateIntlToUnixW(Pointer hApiCnxBase, WString strDate);

	NativeLong AmConvertDateStringToUnixW(Pointer hApiCnxBase, WString strDate);

	NativeLong AmConvertDateUnixToBasicW(Pointer hApiCnxBase, NativeLong lTime);

	NativeLong AmConvertDateUnixToIntlW(Pointer hApiCnxBase, NativeLong lUnixDate, CharBuffer strDate,
			NativeLong lDate);

	NativeLong AmConvertDateUnixToStringW(Pointer hApiCnxBase, NativeLong lUnixDate, CharBuffer strDate,
			NativeLong lDate);

	NativeLong AmConvertDoubleToStringW(double dSrc, CharBuffer strDst, NativeLong lDst);

	NativeLong AmConvertMonetaryToStringW(double dSrc, CharBuffer strDst, NativeLong lDst);

	double AmConvertStringToDoubleW(WString strSrc);

	double AmConvertStringToMonetaryW(WString strSrc);

	NativeLong AmCreateLinkW(Pointer hApiRecord, WString strLinkName, Pointer hApiRecDest);

	Pointer AmCreateRecordW(Pointer hApiCnxBase, WString strTable);

	NativeLong AmCurrentDateW();

	NativeLong AmCurrentServerDateW(Pointer hApiCnxBase);

	NativeLong AmDateAddLogicalW(NativeLong tmStart, NativeLong tsDuration);

	NativeLong AmDateAddW(NativeLong tmStart, NativeLong tsDuration);

	NativeLong AmDateDiffW(NativeLong tmEnd, NativeLong tmStart);

	NativeLong AmDbExecAqlW(Pointer hApiCnxBase, WString strAqlStatement);

	NativeLong AmDbGetDateW(Pointer hApiCnxBase, WString strQuery);

	double AmDbGetDoubleW(Pointer hApiCnxBase, WString strQuery);

	NativeLong AmDbGetLimitedListW(Pointer hApiCnxBase, WString strQuery, CharBuffer pstrResult, NativeLong lResult,
			WString strColSep, WString strLineSep, WString strIdSep, NativeLong lMaxSize, NativeLong lErrorType);

	NativeLong AmDbGetListExW(Pointer hApiCnxBase, WString strQuery, CharBuffer strResult, NativeLong lResult,
			WString strColSep, WString strLineSep, WString strIdSep);

	NativeLong AmDbGetListW(Pointer hApiCnxBase, WString strQuery, CharBuffer strResult, NativeLong lResult,
			WString strColSep, WString strLineSep, WString strIdSep);

	NativeLong AmDbGetLongW(Pointer hApiCnxBase, WString strQuery);

	NativeLong AmDbGetPkW(Pointer hApiCnxBase, WString strTableName, WString strWhere);

	NativeLong AmDbGetStringExW(Pointer hApiCnxBase, WString strQuery, CharBuffer pstrResult, NativeLong lResult,
			WString strColSep, WString strLineSep);

	NativeLong AmDbGetStringW(Pointer hApiCnxBase, WString strQuery, CharBuffer strResult, NativeLong lResult,
			WString strColSep, WString strLineSep);

	NativeLong AmDeleteLinkW(Pointer hApiRecord, WString strLinkName, Pointer hApiRecDest);

	NativeLong AmDeleteRecordW(Pointer hApiRecord);

	NativeLong AmDuplicateRecordW(Pointer hApiRecord, NativeLong bInsert);

	void AmEnableThrowException();

	NativeLong AmEnumValListW(Pointer hApiCnxBase, WString strEnumName, CharBuffer pstrValList, NativeLong lValList,
			NativeLong bNoCase, WString strLineSep);

	Pointer AmExecuteActionByIdW(Pointer hApiCnxBase, NativeLong lActionId, WString strTableName, NativeLong lRecordId);

	Pointer AmExecuteActionByNameW(Pointer hApiCnxBase, WString strSqlName, WString strTableName, NativeLong lRecordId);

	NativeLong AmExportDocumentW(Pointer hApiCnxBase, NativeLong lDocId, WString strFileName);

	NativeLong AmFlushTransactionW(Pointer hApiCnxBase);

	NativeLong AmFormatCurrencyW(double dAmount, WString strCurrency, CharBuffer strDisplay, NativeLong lDisplay);

	NativeLong AmFormatLongW(Pointer hApiCnxBase, NativeLong lNumber, WString strFormat, CharBuffer strResult,
			NativeLong lResult);

	NativeLong AmGetComputeStringW(Pointer hApiCnxBase, WString strTableName, NativeLong lRecordId, String strTemplate,
			CharBuffer strComputeString, NativeLong lComputeString);

	NativeLong AmGetFieldCountW(Pointer hApiObject);

	NativeLong AmGetFieldDateOnlyValueW(Pointer hApiObject, NativeLong lFieldPos);

	NativeLong AmGetFieldDateValueW(Pointer hApiObject, NativeLong lFieldPos);

	NativeLong AmGetFieldDescriptionW(Pointer hApiField, CharBuffer strBuffer, NativeLong lBuffer);

	double AmGetFieldDoubleValueW(Pointer hApiObject, NativeLong lFieldPos);

	NativeLong AmGetFieldFormatFromNameW(Pointer hApiCnxBase, WString strTableName, WString strFieldName,
			CharBuffer fieldFormat, NativeLong pFieldFormat);

	NativeLong AmGetFieldFormatW(Pointer hApiField, CharBuffer strBuffer, NativeLong lBuffer);

	Pointer AmGetFieldFromName(Pointer hApiObject, String strName);

	NativeLong AmGetFieldLabelFromNameW(Pointer hApiCnxBase, WString strTableName, WString strFieldName,
			CharBuffer fieldLabel, NativeLong lFieldLabel);

	NativeLong AmGetFieldLabelW(Pointer hApiField, CharBuffer strBuffer, NativeLong lBuffer);

	NativeLong AmGetFieldLongValue(Pointer hApiObject, NativeLong lFieldPos);

	NativeLong AmGetFieldNameW(Pointer hApiObject, NativeLong lFieldPos, CharBuffer strBuffer, NativeLong lBuffer);

	NativeLong AmGetFieldSize(Pointer hApiField);

	NativeLong AmGetFieldSqlNameW(Pointer hApiField, CharBuffer strBuffer, NativeLong lBuffer);

	NativeLong AmGetFieldStrValueW(Pointer queryHandle, NativeLong position, CharBuffer target,
			NativeLong targetLength);

	NativeLong AmGetFieldTypeW(Pointer hApiField);

	NativeLong AmGetFieldUserTypeW(Pointer hApiField);

	Pointer AmGetFieldW(Pointer hApiObject, NativeLong lPos);

	Pointer AmGetRecordFromMainIdW(Pointer hApiCnxBase, WString strTableName, NativeLong lId);

	Pointer AmGetRecordHandleW(Pointer qryHandle);

	NativeLong AmGetRecordIdW(Pointer hApiRecord);

	Pointer AmGetRelDstFieldW(Pointer hApiField);

	Pointer AmGetRelSrcFieldW(Pointer hApiField);

	Pointer AmGetRelTableW(Pointer hApiField);

	Pointer AmGetReverseLinkW(Pointer hApiField);

	NativeLong AmGetSelfFromMainIdW(Pointer hApiCnxBase, WString strTableName, NativeLong lId, CharBuffer strRecordDesc,
			NativeLong lRecordDesc);

	NativeLong AmGetVersionW(CharBuffer strBuf, NativeLong lBuf);

	NativeLong AmImportDocumentW(Pointer hApiCnxBase, NativeLong lDocObjId, WString strTableName, WString strFileName,
			WString strCategory, WString strDesignation);

	NativeLong AmInsertRecordW(Pointer hApiRecord);

	NativeLong AmIsConnectedW(Pointer hApiCnxBase);

	NativeLong AmLastErrorMsgW(Pointer hApiCnxBase, CharBuffer strBuffer, NativeLong lBuffer);

	NativeLong AmLastErrorW(Pointer hApiCnxBase);

	NativeLong AmListToStringW(WString strSource, WString strColSep, WString strLineSep, WString strIdSep,
			CharBuffer output, NativeLong outputSize);

	NativeLong AmLoginIdW(Pointer hApiCnxBase);

	NativeLong AmLoginNameW(Pointer hApiCnxBase, CharBuffer returnStr, NativeLong lreturn);

	Pointer AmOpenConnectionW(WString src, WString user, WString password);

	NativeLong AmPurgeRecordW(Pointer hApiRecord);

	Pointer AmQueryCreateW(Pointer connHandle);

	NativeLong AmQueryExecW(Pointer qryHandle, WString aqlQuery);

	NativeLong AmQueryGetW(Pointer qryHandle, WString aqlQuery);

	NativeLong AmQueryNextW(Pointer qryHandle);

	NativeLong AmQuerySetAddMainFieldW(Pointer hApiQuery, NativeLong bAddMainField);

	NativeLong AmQuerySetFullMemoW(Pointer hApiQuery, NativeLong bFullMemo);

	Pointer AmQueryStartTableW(Pointer hApiQuery);

	NativeLong AmQueryStopW(Pointer qryHandle);

	NativeLong AmRefreshAllCachesW(Pointer hApiCnxBase);

	NativeLong AmReleaseHandleW(Pointer qryHandle);

	NativeLong AmRollbackW(Pointer hApiCnxBase);

	NativeLong AmSetFieldDateOnlyValueW(Pointer hApiRecord, WString strFieldName, NativeLong dtptmValue);

	NativeLong AmSetFieldDateValueW(Pointer hApiRecord, WString strFieldName, NativeLong tmValue);

	NativeLong AmSetFieldDoubleValueW(Pointer hApiRecord, WString strFieldName, double dValue);

	NativeLong AmSetFieldLongValueW(Pointer hApiRecord, WString strFieldName, NativeLong lValue);

	NativeLong AmSetFieldStrValueW(Pointer hApiRecord, WString strFieldName, WString strValue);

	NativeLong AmSqlTextConstW(WString str, CharBuffer returnStr, NativeLong lreturn);

	NativeLong AmStartTransactionW(Pointer connHandle);

	NativeLong AmStartup();

	NativeLong AmUpdateRecordW(Pointer recHandle);

}
