/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.configuration;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.plexus.components.cipher.PlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipherException;
import org.sonatype.security.configuration.source.PhraseService;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
@Named
public class PasswordHelper
{

  private static final String ENC = "CMMDwoV";

  private final PlexusCipher plexusCipher;

  private final PhraseService phraseService;

  @Inject
  public PasswordHelper(final PlexusCipher plexusCipher, final PhraseService phraseService) {
    this.plexusCipher = checkNotNull(plexusCipher);
    this.phraseService = checkNotNull(phraseService);
  }

  public String encrypt(String password)
      throws PlexusCipherException
  {
    return phraseService.mark(encrypt(password, phraseService.getPhrase(ENC)));
  }

  public String encrypt(String password, String encoding)
      throws PlexusCipherException
  {
    // check if the password is encrypted
    if (plexusCipher.isEncryptedString(password)) {
      return password;
    }

    if (password != null) {
      synchronized (plexusCipher) {
        return plexusCipher.encryptAndDecorate(password, encoding);
      }
    }

    return null;
  }

  public String decrypt(String encodedPassword)
      throws PlexusCipherException
  {
    if (phraseService.usesLegacyEncoding(encodedPassword)) {
      return decrypt(encodedPassword, ENC);
    }
    return decrypt(encodedPassword, phraseService.getPhrase(ENC));
  }

  public String decrypt(String encodedPassword, String encoding)
      throws PlexusCipherException
  {
    // check if the password is encrypted
    if (!plexusCipher.isEncryptedString(encodedPassword)) {
      return encodedPassword;
    }

    if (encodedPassword != null) {
      synchronized (plexusCipher) {
        return plexusCipher.decryptDecorated(encodedPassword, encoding);
      }
    }
    return null;
  }

  public boolean foundLegacyEncoding() {
    return phraseService.foundLegacyEncoding();
  }
}