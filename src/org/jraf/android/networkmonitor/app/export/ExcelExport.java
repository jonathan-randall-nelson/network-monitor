/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2013 Carmen Alvarez (c@rmen.ca)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jraf.android.networkmonitor.app.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import jxl.JXLException;
import jxl.Workbook;
import jxl.format.CellFormat;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import org.jraf.android.networkmonitor.Constants;
import org.jraf.android.networkmonitor.R;

import android.content.Context;
import android.util.Log;

/**
 * Export the Network Monitor data to an Excel file.
 */
public class ExcelExport extends FileExport {

	private static final String TAG = Constants.TAG
			+ ExcelExport.class.getSimpleName();
	private static final String EXCEL_FILE = "networkmonitor.xls";

	private WritableWorkbook mWorkbook;
	private WritableSheet mSheet;
	private WritableCellFormat mBoldFormat;
	private WritableCellFormat mRedFormat;
	private WritableCellFormat mGreenFormat;

	public ExcelExport(Context context) throws FileNotFoundException {
		super(context, new File(context.getExternalFilesDir(null), EXCEL_FILE));
	}

	@Override
	void writeHeader(String[] columnNames) throws IOException {
		// Create the workbook, sheet, custom cell formats, and freeze
		// row/column.
		mWorkbook = Workbook.createWorkbook(mFile);
		mSheet = mWorkbook
				.createSheet(mContext.getString(R.string.app_name), 0);
		mSheet.insertRow(0);
		mSheet.getSettings().setHorizontalFreeze(2);
		mSheet.getSettings().setVerticalFreeze(1);
		createCellFormats();
		for (int i = 0; i < columnNames.length; i++) {
			mSheet.insertColumn(i);
			insertCell(columnNames[i], 0, i, mBoldFormat);
		}
	}

	@Override
	void writeRow(int rowNumber, String[] cellValues) throws IOException {
		mSheet.insertRow(rowNumber);
		for (int i = 0; i < cellValues.length; i++) {
			CellFormat cellFormat = null;
			if (Constants.CONNECTION_TEST_PASS.equals(cellValues[i]))
				cellFormat = mGreenFormat;
			else if (Constants.CONNECTION_TEST_FAIL.equals(cellValues[i]))
				cellFormat = mRedFormat;
			insertCell(cellValues[i], rowNumber, i, cellFormat);
		}
	}

	@Override
	void writeFooter() throws IOException {
		try {
			mWorkbook.write();
			mWorkbook.close();
		} catch (JXLException e) {
			Log.e(TAG, "writeHeader Could not close file", e);
		}
	}

	private void insertCell(String text, int row, int column, CellFormat format) {
		Label label = format == null ? new Label(column, row, text)
				: new Label(column, row, text, format);
		try {
			mSheet.addCell(label);
		} catch (JXLException e) {
			Log.e(TAG, "writeHeader Could not insert cell " + text + " at row="
					+ row + ", col=" + column, e);
		}
	}

	/**
	 * In order to set text to bold, red, or green, we need to create cell
	 * formats for each style.
	 */
	private void createCellFormats() {

		// Insert a dummy empty cell, so we can obtain its cell. This allows to
		// start with a default cell format.
		Label cell = new Label(0, 0, " ");
		CellFormat cellFormat = cell.getCellFormat();

		try {
			// Create the bold format
			final WritableFont boldFont = new WritableFont(cellFormat.getFont());
			mBoldFormat = new WritableCellFormat(cellFormat);
			boldFont.setBoldStyle(WritableFont.BOLD);
			mBoldFormat.setFont(boldFont);

			// Create the red format
			mRedFormat = new WritableCellFormat(cellFormat);
			final WritableFont redFont = new WritableFont(cellFormat.getFont());
			redFont.setColour(Colour.RED);
			mRedFormat.setFont(redFont);

			// Create the green format
			mGreenFormat = new WritableCellFormat(cellFormat);
			final WritableFont greenFont = new WritableFont(
					cellFormat.getFont());
			greenFont.setColour(Colour.GREEN);
			mGreenFormat.setFont(greenFont);
		} catch (WriteException e) {
			Log.e(TAG, "createCellFormats Could not create cell formats", e);
		}
	}
}