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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class Sign1MessageTest {
  @Test
  public void testDeserialize() throws CoseException, CborException {
    Sign1Message message = Sign1Message.deserialize(TestUtilities.hexStringToByteArray("8441A0A20126044"
      + "2313154546869732069732074686520636F6E74656E742E584087DB0D2E5571843B78AC33ECB2830DF7B6E0A4"
      + "D5B7376DE336B23C591C90C425317E56127FBE04370097CE347087B233BF722B64072BEB4486BDA4031D27244F"
    ));
    Assert.assertEquals(TestUtilities.CONTENT, new String(message.getMessage()));
    Assert.assertEquals("87DB0D2E5571843B78AC33ECB2830DF7B6E0A4D5B7376DE336B23C591C90C425317E56127"
            + "FBE04370097CE347087B233BF722B64072BEB4486BDA4031D27244F",
        TestUtilities.bytesToHexString(message.getSignature()));
    Assert.assertEquals("A0", TestUtilities.bytesToHexString(message.getProtectedHeaderBytes()));
    Assert.assertEquals(0, message.getProtectedHeaders().getKeys().size());

    Map headers = message.getUnprotectedHeaders();
    Assert.assertEquals(2, headers.getKeys().size());
    Assert.assertEquals(Algorithm.SIGNING_ALGORITHM_ECDSA_SHA_256.getCoseAlgorithmId(),
        headers.get(new UnsignedInteger(Headers.MESSAGE_HEADER_ALGORITHM)));
    Assert.assertEquals(new ByteString(TestUtilities.hexStringToByteArray("3131")),
        headers.get(new UnsignedInteger(Headers.MESSAGE_HEADER_KEY_ID)));
  }

  @Test
  public void testSerializeWithProtectedHeaderBytes() throws CoseException, CborException {
    Map map = new Map();
    map.put(new UnsignedInteger(Headers.MESSAGE_HEADER_ALGORITHM),
        Algorithm.SIGNING_ALGORITHM_ECDSA_SHA_256.getCoseAlgorithmId());
    map.put(new UnsignedInteger(Headers.MESSAGE_HEADER_KEY_ID),
        new ByteString(TestUtilities.hexStringToByteArray("3131")));

    Sign1Message message = Sign1Message.builder()
        .withProtectedHeaderBytes(CborUtils.encode(new Map()))
        .withUnprotectedHeaders(map)
        .withMessage(TestUtilities.CONTENT.getBytes())
        .withSignature(TestUtilities.hexStringToByteArray("87DB0D2E5571843B78AC33ECB2830DF7B6E0A4D"
            + "5B7376DE336B23C591C90C425317E56127FBE04370097CE347087B233BF722B64072BEB4486BDA4031D"
            + "27244F"))
        .build();
    Assert.assertEquals("8441A0A201260442313154546869732069732074686520636F6E74656E742E584087DB0D2"
      + "E5571843B78AC33ECB2830DF7B6E0A4D5B7376DE336B23C591C90C425317E56127FBE04370097CE347087B2"
      + "33BF722B64072BEB4486BDA4031D27244F", TestUtilities.bytesToHexString(message.serialize()));
  }

  @Test
  public void testSerializeWithProtectedHeaders() throws CoseException, CborException {
    Map map = new Map();
    map.put(new UnsignedInteger(Headers.MESSAGE_HEADER_ALGORITHM),
        Algorithm.SIGNING_ALGORITHM_ECDSA_SHA_256.getCoseAlgorithmId());
    map.put(new UnsignedInteger(Headers.MESSAGE_HEADER_KEY_ID),
        new ByteString(TestUtilities.hexStringToByteArray("3131")));

    Sign1Message message = Sign1Message.builder()
        .withProtectedHeaders(new Map())
        .withUnprotectedHeaders(map)
        .withMessage(TestUtilities.CONTENT.getBytes())
        .withSignature(TestUtilities.hexStringToByteArray("87DB0D2E5571843B78AC33ECB2830DF7B6E0A4D"
            + "5B7376DE336B23C591C90C425317E56127FBE04370097CE347087B233BF722B64072BEB4486BDA4031D"
            + "27244F"))
        .build();
    Assert.assertEquals("8440A201260442313154546869732069732074686520636F6E74656E742E584087DB0D2"
      + "E5571843B78AC33ECB2830DF7B6E0A4D5B7376DE336B23C591C90C425317E56127FBE04370097CE347087B2"
      + "33BF722B64072BEB4486BDA4031D27244F", TestUtilities.bytesToHexString(message.serialize()));
  }
}
