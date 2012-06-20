/*
 * eID Digital Signature Service Project.
 * Copyright (C) 2009 FedICT.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version
 * 3.0 as published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, see 
 * http://www.gnu.org/licenses/.
 */

package be.fedict.eid.dss.webapp;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.fedict.eid.dss.control.View;
import be.fedict.eid.dss.model.DocumentRepository;
import be.fedict.eid.dss.spi.DSSDocumentService;
import be.fedict.eid.dss.spi.DocumentVisualization;

/**
 * A servlet for visualizing a document.
 * 
 * @author Frank Cornelis.
 */
public class DocumentViewerServlet extends AbstractProtocolServiceServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory
			.getLog(DocumentViewerServlet.class);

	public DocumentViewerServlet() {
		super(false, true);
	}

	@Override
	protected void handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {

		LOG.debug("doGet");

		// get browser plugins
		List<String> plugins = BrowserInfoServlet.getPlugins(request
				.getSession());
		for (String plugin : plugins) {
			LOG.debug("Plugin: " + plugin);
		}

		// get browser mimeTypes
		List<MimeType> mimeTypes = BrowserInfoServlet.getMimeTypes(request
				.getSession());
		for (MimeType mimeType : mimeTypes) {
			LOG.debug("MimeTypes: type=" + mimeType.getType() + " plugin="
					+ mimeType.getPlugin());
		}

		HttpSession httpSession = request.getSession();
		DocumentRepository documentRepository = new DocumentRepository(
				httpSession);
		String contentType = documentRepository.getDocumentContentType();
		byte[] documentData = documentRepository.getDocument();

		DSSDocumentService documentService = super
				.findDocumentService(contentType);
		if (null != documentService) {
			LOG.debug("document visualization transformation");
			String language = (String) httpSession
					.getAttribute(View.LANGUAGE_SESSION_ATTRIBUTE);
			DocumentVisualization documentVisualization;
			try {
				documentVisualization = documentService.visualizeDocument(
						documentData, language);
			} catch (Exception e) {
				throw new ServletException("error visualizing the document: "
						+ e.getMessage(), e);
			}
			if (null != documentVisualization) {
				contentType = documentVisualization.getBrowserContentType();
				documentData = documentVisualization.getBrowserData();
			}
		}

		response.setHeader("Cache-Control",
				"no-cache, no-store, must-revalidate, max-age=-1"); // http
		// 1.1
		if (false == request.getScheme().equals("https")) {
			// else the download fails in IE
			response.setHeader("Pragma", "no-cache"); // http 1.0
		} else {
			response.setHeader("Pragma", "public");
		}
		response.setDateHeader("Expires", -1);
		response.setContentLength(documentData.length);

		response.setContentType(contentType);
		response.setContentLength(documentData.length);
		ServletOutputStream out = response.getOutputStream();
		out.write(documentData);
		out.flush();
	}
}
