/*
 * eID Digital Signature Service Project.
 * Copyright (C) 2010 FedICT.
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

package be.fedict.eid.dss.portal.control.bean;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.bouncycastle.util.encoders.Base64;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.international.LocaleSelector;
import org.jboss.seam.log.Log;

import be.fedict.eid.dss.model.SignatureInfo;
import be.fedict.eid.dss.model.SignatureVerificationService;
import be.fedict.eid.dss.model.exception.DocumentFormatException;
import be.fedict.eid.dss.model.exception.InvalidSignatureException;
import be.fedict.eid.dss.portal.control.View;

@Stateful
@Name("dssView")
@LocalBinding(jndiBinding = "fedict/eid/dss/portal/ViewBean")
public class ViewBean implements View {

	@Logger
	private Log log;

	@EJB
	private SignatureVerificationService signatureVerificationService;

	@In(value = "document", scope = ScopeType.SESSION, required = true)
	private byte[] document;

	@Out(value = "target", scope = ScopeType.SESSION, required = false)
	private String target;

	@Out(value = "SignatureRequest", scope = ScopeType.SESSION, required = false)
	private String signatureRequest;

	@Out(value = "language", scope = ScopeType.SESSION, required = false)
	private String language;

	@In(value = "filesize", scope = ScopeType.SESSION, required = false)
	@Out(value = "filesize", scope = ScopeType.SESSION, required = false)
	private Integer filesize;

	@DataModel
	private List<SignatureInfo> signatureInfos;

	@In
	private LocaleSelector localeSelector;

	@Remove
	@Destroy
	@Override
	public void destroy() {
		this.log.debug("destroy");
	}

	@Override
	public void verifySignatures() {
		this.filesize = this.document.length;
		try {
			this.signatureInfos = this.signatureVerificationService
					.verify(this.document);
		} catch (DocumentFormatException e) {
			this.log.error("document format error: #0", e.getMessage());
			return;
		} catch (InvalidSignatureException e) {
			this.log.error("invalid signature: #0", e.getMessage());
			return;
		}
		this.log.debug("number of signatures: #0", this.signatureInfos.size());
		for (SignatureInfo signatureInfo : this.signatureInfos) {
			this.log.debug("signer: #0", signatureInfo.getSigner()
					.getSubjectX500Principal());
			this.log.debug("signing time: #0", signatureInfo.getSigningTime());
		}
	}

	@Override
	public String sign() {
		this.log.debug("sign");
		this.signatureRequest = new String(Base64.encode(this.document));

		FacesContext facesContext = FacesContext.getCurrentInstance();
		ExternalContext externalContext = facesContext.getExternalContext();
		String requestContextPath = externalContext.getRequestContextPath();
		this.target = requestContextPath + "/dss-response";

		this.language = this.localeSelector.getLanguage();

		return "submit";
	}
}
