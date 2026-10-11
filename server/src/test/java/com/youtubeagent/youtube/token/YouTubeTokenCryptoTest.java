package com.youtubeagent.youtube.token;
import com.youtubeagent.youtube.YouTubeProperties;
import org.junit.jupiter.api.Test;
import java.util.Base64;
import static org.junit.jupiter.api.Assertions.*;
class YouTubeTokenCryptoTest {
 private static final String KEY=Base64.getEncoder().encodeToString(new byte[32]);
 private YouTubeTokenCrypto crypto(String key){return new YouTubeTokenCrypto(new YouTubeProperties(null,null,key==null?null:new YouTubeProperties.TokenEncryption(key)));}
 @Test void encryptsAndDecryptsToken(){var c=crypto(KEY);String encrypted=c.encrypt("secret-token");assertNotEquals("secret-token",encrypted);assertEquals("secret-token",c.decrypt(encrypted));}
 @Test void usesDifferentCiphertextForRepeatedEncryption(){var c=crypto(KEY);assertNotEquals(c.encrypt("same"),c.encrypt("same"));}
 @Test void supportsNullValues(){var c=crypto(KEY);assertNull(c.encrypt(null));assertNull(c.decrypt(null));}
 @Test void rejectsMissingInvalidAndWrongLengthKeys(){
  assertThrows(IllegalStateException.class,()->crypto(null));assertThrows(IllegalStateException.class,()->crypto("not-base64!"));
  assertThrows(IllegalStateException.class,()->crypto(Base64.getEncoder().encodeToString(new byte[16])));
 }
 @Test void rejectsMalformedOrTamperedCiphertext(){
  var c=crypto(KEY);assertThrows(IllegalStateException.class,()->c.decrypt("YQ=="));byte[] bytes=Base64.getDecoder().decode(c.encrypt("sensitive"));bytes[bytes.length-1]^=1;
  assertThrows(IllegalStateException.class,()->c.decrypt(Base64.getEncoder().encodeToString(bytes)));
 }
}