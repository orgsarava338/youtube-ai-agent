package com.youtubeagent.youtube.token;
import com.youtubeagent.youtube.oauth.YouTubeToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class YouTubeTokenPersistenceServiceTest {
 @Mock private YouTubeTokenRepository repository; @Mock private YouTubeTokenCrypto crypto; private YouTubeTokenPersistenceService service;
 @BeforeEach void setUp(){service=new YouTubeTokenPersistenceService(repository,crypto);}
 @Test void savesEncryptedTokenFields(){
  var token=new YouTubeToken("access","refresh",1800000000L,"scope","Bearer");when(repository.findByUserId("user")).thenReturn(Optional.empty());when(crypto.encrypt("access")).thenReturn("enc-access");when(crypto.encrypt("refresh")).thenReturn("enc-refresh");
  service.save("user",token);var captor=org.mockito.ArgumentCaptor.forClass(YouTubeTokenEntity.class);verify(repository).save(captor.capture());var e=captor.getValue();
  assertEquals("user",e.getUserId());assertEquals("enc-access",e.getAccessTokenEncrypted());assertEquals("enc-refresh",e.getRefreshTokenEncrypted());assertEquals(Instant.ofEpochSecond(1800000000L),e.getExpiresAt());assertEquals("scope",e.getScopes());assertEquals("Bearer",e.getTokenType());
 }
 @Test void preservesExistingRefreshTokenWhenProviderOmitsIt(){
  var e=new YouTubeTokenEntity();e.setUserId("user");e.setRefreshTokenEncrypted("existing-refresh");when(repository.findByUserId("user")).thenReturn(Optional.of(e));when(crypto.encrypt("new-access")).thenReturn("enc-access");
  service.save("user",new YouTubeToken("new-access",null,1800000000L,"scope","Bearer"));assertEquals("existing-refresh",e.getRefreshTokenEncrypted());verify(crypto,never()).encrypt(null);verify(repository).save(e);
 }
 @Test void loadsAndDecryptsStoredToken(){
  var e=new YouTubeTokenEntity();e.setAccessTokenEncrypted("enc-access");e.setRefreshTokenEncrypted("enc-refresh");e.setExpiresAt(Instant.ofEpochSecond(1800000000L));e.setScopes("scope");e.setTokenType("Bearer");when(repository.findByUserId("user")).thenReturn(Optional.of(e));when(crypto.decrypt("enc-access")).thenReturn("access");when(crypto.decrypt("enc-refresh")).thenReturn("refresh");
  assertEquals(new YouTubeToken("access","refresh",1800000000L,"scope","Bearer"),service.load("user").orElseThrow());
 }
 @Test void returnsEmptyForMissingTokenAndDeletesByUser(){when(repository.findByUserId("missing")).thenReturn(Optional.empty());assertTrue(service.load("missing").isEmpty());service.delete("user");verify(repository).deleteByUserId("user");}
}