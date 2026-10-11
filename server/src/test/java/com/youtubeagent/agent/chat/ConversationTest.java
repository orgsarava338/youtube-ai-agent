package com.youtubeagent.agent.chat;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ConversationTest {
 @Test void requiresUserId(){assertThrows(IllegalArgumentException.class,()->Conversation.create(null,"Title"));assertThrows(IllegalArgumentException.class,()->Conversation.create(" ","Title"));}
 @Test void normalizesBlankAndWhitespaceTitles(){assertEquals("New conversation",Conversation.create("user",null).getTitle());assertEquals("New conversation",Conversation.create("user","  ").getTitle());assertEquals("A title",Conversation.create("user","  A title  ").getTitle());}
 @Test void truncatesTitleToDatabaseLimit(){assertEquals(200,Conversation.create("user","x".repeat(250)).getTitle().length());}
 @Test void touchUpdatesUpdatedTimestamp() throws InterruptedException {Conversation c=Conversation.create("user","Title");c.onCreate();var before=c.getUpdatedAt();Thread.sleep(2);c.touch();assertFalse(c.getUpdatedAt().isBefore(before));}
}