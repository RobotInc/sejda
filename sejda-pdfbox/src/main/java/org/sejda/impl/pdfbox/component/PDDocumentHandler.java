/*
 * Created on 29/ago/2011
 * Copyright 2011 by Andrea Vacondio (andrea.vacondio@gmail.com).
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.sejda.impl.pdfbox.component;

import static org.sejda.impl.pdfbox.util.ViewerPreferencesUtils.getPageLayout;
import static org.sejda.impl.pdfbox.util.ViewerPreferencesUtils.getPageMode;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.BadSecurityHandlerException;
import org.apache.pdfbox.pdmodel.encryption.DecryptionMaterial;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;
import org.sejda.core.Sejda;
import org.sejda.core.exception.TaskException;
import org.sejda.core.exception.TaskIOException;
import org.sejda.core.exception.TaskPermissionsException;
import org.sejda.core.manipulation.model.pdf.PdfVersion;
import org.sejda.core.manipulation.model.pdf.viewerpreferences.PdfPageLayout;
import org.sejda.core.manipulation.model.pdf.viewerpreferences.PdfPageMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper over a {@link PDDocument}.
 * 
 * @author Andrea Vacondio
 * 
 */
public class PDDocumentHandler {

    private static final Logger LOG = LoggerFactory.getLogger(PDDocumentHandler.class);

    private PDDocument document;

    public PDDocumentHandler(PDDocument document) {
        if (document == null) {
            throw new IllegalArgumentException("PDDocument cannot be null.");
        }
        this.document = document;
    }

    /**
     * Ensures that underlying {@link PDDocument} is opened with Owner permissions
     * 
     * @throws TaskPermissionsException
     */
    public void ensureOwnerPermissions() throws TaskPermissionsException {
        AccessPermission ap = document.getCurrentAccessPermission();
        if (!ap.isOwnerPermission()) {
            throw new TaskPermissionsException("Owner permission is required.");
        }
    }

    /**
     * Sets the creator on the underlying {@link PDDocument}.
     */
    public void setCreatorOnPDDocument() {
        document.getDocumentInformation().setCreator(Sejda.CREATOR);
    }

    /**
     * Sets the given page layout on the underlying {@link PDDocument}.
     * 
     * @param layout
     */
    public void setPageLayoutOnDocument(PdfPageLayout layout) {
        document.getDocumentCatalog().setPageLayout(getPageLayout(layout));
        LOG.trace("Page layout set to '{}'", layout);
    }

    /**
     * Sets the given page mode on the underlying {@link PDDocument}.
     * 
     * @param mode
     */
    public void setPageModeOnDocument(PdfPageMode mode) {
        document.getDocumentCatalog().setPageMode(getPageMode(mode));
        LOG.trace("Page mode set to '{}'", mode);
    }

    /**
     * Sets the version on the underlying {@link PDDocument}.
     * 
     * @param version
     */
    public void setVersionOnPDDocument(PdfVersion version) {
        if (version != null) {
            document.getDocument().setVersion((float) version.getVersionAsDouble());
            document.getDocument().setHeaderString(version.getVersionHeader());
            LOG.trace("Version set to '{}'", version);
        }
    }

    /**
     * Set compression of the XRef table on underlying {@link PDDocument}.
     * 
     * @param compress
     */
    public void compressXrefStream(boolean compress) {
        if (compress) {
            LOG.warn("Xref Compression not yet supported by PDFBox");
        }
    }

    /**
     * Decrypts the underlying {@link PDDocument} if encrypted using the provided password if not blank.
     * 
     * @param password
     * @throws TaskIOException
     */
    public void decryptPDDocumentIfNeeded(String password) throws TaskIOException {
        if (document.isEncrypted() && StringUtils.isNotBlank(password)) {
            DecryptionMaterial decryptionMaterial = new StandardDecryptionMaterial(password);
            LOG.trace("Decrypting input document");
            try {
                document.openProtection(decryptionMaterial);
            } catch (IOException e) {
                throw new TaskIOException("An error occurred reading cryptographic information.", e);
            } catch (BadSecurityHandlerException e) {
                throw new TaskIOException("Unable to decrypt the document.", e);
            } catch (CryptographyException e) {
                throw new TaskIOException("Unable to decrypt the document.", e);
            }
        }
    }

    /**
     * @return the view preferences for the underlying {@link PDDocument}.
     */
    public PDViewerPreferences getViewerPreferences() {
        PDViewerPreferences retVal = document.getDocumentCatalog().getViewerPreferences();
        if (retVal == null) {
            retVal = new PDViewerPreferences(new COSDictionary());
        }
        return retVal;
    }

    public void setViewerPreferences(PDViewerPreferences preferences) {
        document.getDocumentCatalog().setViewerPreferences(preferences);
    }

    private void close() throws IOException {
        document.close();
    }

    /**
     * Saves the underlying {@link PDDocument} removing security from it.
     * 
     * @param file
     * @throws TaskException
     */
    public void saveDecryptedPDDocument(File file) throws TaskException {
        savePDDocument(file, true);
    }

    /**
     * Saves the underlying {@link PDDocument} to the given file.
     * 
     * @param file
     * @throws TaskException
     */
    public void savePDDocument(File file) throws TaskException {
        savePDDocument(file, false);
    }

    private void savePDDocument(File file, boolean decrypted) throws TaskException {
        try {
            if (decrypted) {
                document.setAllSecurityToBeRemoved(decrypted);
            }
            document.save(file.getAbsolutePath());
        } catch (COSVisitorException e) {
            throw new TaskException("An error occured saving to temporary file.", e);
        } catch (IOException e) {
            throw new TaskIOException("Unable to save to temporary file.", e);
        }
    }

    public PDDocument getUnderlyingPDDocument() {
        return document;
    }

    /**
     * closes the underlying {@link PDDocument} if the handler is not null.
     * 
     * @param handler
     */
    public static void nullSafeClose(PDDocumentHandler handler) {
        if (handler != null) {
            try {
                handler.close();
            } catch (IOException e) {
                LOG.warn("An error occurred closing the document handler.", e);
            }
        }
    }
}