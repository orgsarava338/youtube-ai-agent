package com.youtubeagent.youtube;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;
class YouTubeJsonUtilsTest {
 private final ObjectMapper mapper=new ObjectMapper();
 private JsonNode json(String s){return mapper.readTree(s);}
 @Test void readsPrimitiveFieldsAndSafeDefaults(){
  JsonNode n=json("{\"title\":\"Demo\",\"views\":42,\"enabled\":true}");
  assertEquals("Demo",YouTubeJsonUtils.textValue(n,"title"));assertEquals(42L,YouTubeJsonUtils.longValue(n,"views"));assertTrue(YouTubeJsonUtils.booleanValue(n,"enabled"));
  assertNull(YouTubeJsonUtils.textValue(n,"missing"));assertEquals(0L,YouTubeJsonUtils.longValue(n,"missing"));assertFalse(YouTubeJsonUtils.booleanValue(n,"missing"));assertNull(YouTubeJsonUtils.textValue(null,"title"));assertEquals(0L,YouTubeJsonUtils.longValue((JsonNode)null,"views"));
 }
 @Test void readsArrayValuesByIndex(){JsonNode row=json("[\"x\",7]");assertEquals(7L,YouTubeJsonUtils.longValue(row,1));assertEquals(0L,YouTubeJsonUtils.longValue(row,9));assertEquals(0L,YouTubeJsonUtils.longValue((JsonNode)null,0));}
 @Test void parsesInstants(){JsonNode n=json("{\"publishedAt\":\"2026-01-02T03:04:05Z\"}");assertEquals(Instant.parse("2026-01-02T03:04:05Z"),YouTubeJsonUtils.instantValue(n,"publishedAt"));assertNull(YouTubeJsonUtils.instantValue(n,"missing"));}
 @Test void choosesBestAvailableThumbnail(){
  assertEquals("high.jpg",YouTubeJsonUtils.thumbnailUrl(json("{\"thumbnails\":{\"high\":{\"url\":\"high.jpg\"},\"medium\":{\"url\":\"medium.jpg\"}}}")));
  assertEquals("medium.jpg",YouTubeJsonUtils.thumbnailUrl(json("{\"thumbnails\":{\"medium\":{\"url\":\"medium.jpg\"},\"default\":{\"url\":\"default.jpg\"}}}")));
  assertEquals("default.jpg",YouTubeJsonUtils.thumbnailUrl(json("{\"thumbnails\":{\"default\":{\"url\":\"default.jpg\"}}}")));
  assertNull(YouTubeJsonUtils.thumbnailUrl(json("{}")));assertNull(YouTubeJsonUtils.thumbnailUrl(null));
 }
}