/**
 * BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
 * 
 * Copyright (c) 2012 BigBlueButton Inc. and by respective authors (see below).
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 * 
 * BigBlueButton is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with BigBlueButton; if not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.bigbluebutton.presentation.imp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultDocument;
import org.apache.commons.io.FilenameUtils;
import org.bigbluebutton.presentation.UploadedPresentation;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.job.AbstractConverter;
import org.jodconverter.local.LocalConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public abstract class Office2PdfPageConverter {
  private static Logger log = LoggerFactory.getLogger(Office2PdfPageConverter.class);

  public static boolean convert(File presentationFile, File output, int page, UploadedPresentation pres,
                         LocalConverter converter){

    FileInputStream inputStream = null;
    FileOutputStream outputStream = null;

    try {
      Map<String, Object> logData = new HashMap<>();
      logData.put("meetingId", pres.getMeetingId());
      logData.put("presId", pres.getId());
      logData.put("filename", pres.getName());
      logData.put("logCode", "office_doc_to_pdf");
      logData.put("message", "Converting Office doc to PDF.");
      Gson gson = new Gson();
      String logStr = gson.toJson(logData);
      log.info(" --analytics-- data={}", logStr);

      final DocumentFormat sourceFormat = DefaultDocumentFormatRegistry.getFormatByExtension(
              FilenameUtils.getExtension(presentationFile.getName()));

      inputStream = new FileInputStream(presentationFile);
      outputStream = new FileOutputStream(output);

      converter.convert(inputStream).as(sourceFormat).to(outputStream).as(DefaultDocumentFormatRegistry.PDF).execute();
      outputStream.flush();

      if (output.exists()) {
        return true;
      } else {
        logData = new HashMap<>();
        logData.put("meetingId", pres.getMeetingId());
        logData.put("presId", pres.getId());
        logData.put("filename", pres.getName());
        logData.put("logCode", "office_doc_to_pdf_failed");
        logData.put("message", "Failed to convert Office doc to PDF.");
        gson = new Gson();
        logStr = gson.toJson(logData);
        log.warn(" --analytics-- data={}", logStr);

        return false;
      }
    } catch (Exception e) {
      Map<String, Object> logData = new HashMap<>();
      logData.put("meetingId", pres.getMeetingId());
      logData.put("presId", pres.getId());
      logData.put("filename", pres.getName());
      logData.put("logCode", "office_doc_to_pdf_failed_with_exception");
      logData.put("message", "Failed to convert Office doc to PDF.");
      logData.put("exception", e);
      Gson gson = new Gson();
      String logStr = gson.toJson(logData);
      log.error(" --analytics-- data={}", logStr, e);
      return false;
    } finally {
       if(inputStream!=null) {
         try {
           inputStream.close();
         } catch(Exception e) {

         }
       }

      if(outputStream!=null) {
        try {
          outputStream.close();
        } catch(Exception e) {

        }
      }
    }
  }

}
