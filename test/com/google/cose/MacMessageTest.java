/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cose;

import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.UnsignedInteger;
import com.google.cose.exceptions.CoseException;
import com.google.cose.utils.Algorithm;
import com.google.cose.utils.CborUtils;
import com.google.cose.utils.Headers;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MacMessageTest {
  @Test
  public void testDeserialize() throws CoseException, CborException {
    MacMessage message = MacMessage.deserialize(TestUtilities.hexStringToByteArray(
      "8543A10105A054546869732069732074686520636F6E74656E742E58202BDCC89F058216B8A208DDC6D8B54AA91"
          + "F48BD63484986565105C9AD5A6682F6818340A20125044A6F75722D73656372657440"
    ));
    Assert.assertEquals(TestUtilities.CONTENT, new String(message.getMessage()));
    Map headers = message.getProtectedHeaders();
    Assert.assertEquals(Algorithm.MAC_ALGORITHM_HMAC_SHA_256_256.getCoseAlgorithmId(),
        headers.get(new UnsignedInteger(Headers.MESSAGE_HEADER_ALGORITHM)));
    Assert.assertEquals(1, headers.getKeys().size());
    Assert.assertEquals("A10105",
        TestUtilities.bytesToHexString(message.getProtectedHeaderBytes()));
    Assert.assertEquals(0, message.getUnprotectedHeaders().getKeys().size());
    Assert.assertEquals("2BDCC89F058216B8A208DDC6D8B54AA91F48BD63484986565105C9AD5A6682F6",
        TestUtilities.bytesToHexString(message.getTag()));
    Assert.assertEquals(1, message.recipients.size());

    Recipient r = message.recipients.get(0);
    Assert.assertEquals("", TestUtilities.bytesToHexString(r.getProtectedHeaderBytes()));
    Assert.assertEquals(Algorithm.DIRECT_CEK_USAGE.getCoseAlgorithmId(),
        r.getUnprotectedHeaders().get(new UnsignedInteger(Headers.MESSAGE_HEADER_ALGORITHM)));
    Assert.assertEquals(new ByteString(TestUtilities.SHARED_KEY_ID.getBytes()),
        r.getUnprotectedHeaders().get(new UnsignedInteger(Headers.MESSAGE_HEADER_KEY_ID)));
    Assert.assertEquals("", TestUtilities.bytesToHexString(r.getCiphertext()));
  }

  @Test
  public void testSerializeWithProtectedHeaderBytes() throws CoseException, CborException {
    Map protectedHeaders = new Map();
    protectedHeaders.put(new UnsignedInteger(Headers.MESSAGE_HEADER_ALGORITHM),
        Algorithm.MAC_ALGORITHM_HMAC_SHA_256_256.getCoseAlgorithmId());

    Map unprotectedHeaders = new Map();
    unprotectedHeaders.put(new UnsignedInteger(Headers.MESSAGE_HEADER_ALGORITHM),
        Algorithm.DIRECT_CEK_USAGE.getCoseAlgorithmId());
    unprotectedHeaders.put(new UnsignedInteger(Headers.MESSAGE_HEADER_KEY_ID),
        new ByteString(TestUtilities.SHARED_KEY_ID.getBytes()));

    Recipient r = Recipient.builder()
        .withCiphertext(new byte[0])
        .withUnprotectedHeaders(unprotectedHeaders)
        .withProtectedHeaderBytes(new byte[0])
        .build();

    MacMessage message = MacMessage.builder()
        .withProtectedHeaderBytes(CborUtils.encode(protectedHeaders))
        .withUnprotectedHeaders(new Map())
        .withMessage(TestUtilities.CONTENT.getBytes())
        .withTag(TestUtilities.hexStringToByteArray(
            "2BDCC89F058216B8A208DDC6D8B54AA91F48BD63484986565105C9AD5A6682F6"))
        .withRecipients(Collections.singletonList(r))
        .build();

    Assert.assertEquals("8340A20125044A6F75722D73656372657440",
        TestUtilities.bytesToHexString(r.serialize()));

    Assert.assertEquals("8543A10105A054546869732069732074686520636F6E74656E742E58202BDCC89F058216B"
        + "8A208DDC6D8B54AA91F48BD63484986565105C9AD5A6682F6818340A20125044A6F75722D73656372657440",
        TestUtilities.bytesToHexString(message.serialize()));
  }

  @Test
  public void testSerializeWithProtectedHeaders() throws CoseException, CborException {
    Map protectedHeaders = new Map();
    protectedHeaders.put(new UnsignedInteger(Headers.MESSAGE_HEADER_ALGORITHM),
        Algorithm.MAC_ALGORITHM_HMAC_SHA_256_256.getCoseAlgorithmId());

    Map unprotectedHeaders = new Map();
    unprotectedHeaders.put(new UnsignedInteger(Headers.MESSAGE_HEADER_ALGORITHM),
        Algorithm.DIRECT_CEK_USAGE.getCoseAlgorithmId());
    unprotectedHeaders.put(new UnsignedInteger(Headers.MESSAGE_HEADER_KEY_ID),
        new ByteString(TestUtilities.SHARED_KEY_ID.getBytes()));

    Recipient r = Recipient.builder()
        .withCiphertext(new byte[0])
        .withUnprotectedHeaders(unprotectedHeaders)
        .withProtectedHeaders(new Map())
        .build();

    MacMessage message = MacMessage.builder()
        .withProtectedHeaders(protectedHeaders)
        .withUnprotectedHeaders(new Map())
        .withMessage(TestUtilities.CONTENT.getBytes())
        .withTag(TestUtilities.hexStringToByteArray(
            "2BDCC89F058216B8A208DDC6D8B54AA91F48BD63484986565105C9AD5A6682F6"))
        .withRecipients(Collections.singletonList(r))
        .build();

    Assert.assertEquals("8340A20125044A6F75722D73656372657440",
        TestUtilities.bytesToHexString(r.serialize()));

    Assert.assertEquals("8543A10105A054546869732069732074686520636F6E74656E742E58202BDCC89F058216B"
        + "8A208DDC6D8B54AA91F48BD63484986565105C9AD5A6682F6818340A20125044A6F75722D73656372657440",
        TestUtilities.bytesToHexString(message.serialize()));
  }
}
