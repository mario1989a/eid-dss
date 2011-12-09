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

package be.fedict.eid.dss.model;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.ejb.Local;

/**
 * Interface for the identity service. The identity service maintains the
 * identity of the eID DSS service.
 * 
 * @author Frank Cornelis
 * 
 */
@Local
public interface IdentityService {

	void reloadIdentity();

	KeyStore.PrivateKeyEntry getIdentity();

	String getIdentityFingerprint();

	List<X509Certificate> getIdentityCertificateChain();
}