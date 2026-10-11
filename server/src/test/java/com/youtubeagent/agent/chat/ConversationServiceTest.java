package com.youtubeagent.agent.chat;
import com.youtubeagent.agent.AgentResponse;
import com.youtubeagent.agent.AgentService;
import com.youtubeagent.agent.AgentTurnResult;
import com.youtubeagent.ai.core.LLMMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {
 @Mock private ConversationStore store; @Mock private AgentService agent; private ConversationService service;
 @BeforeEach void setUp(){service=new ConversationService(store,agent);}
 @Test void createsConversationAndPersistsUserAndAssistantMessages(){
  String user="user-1",prompt="Hello";Conversation c=Conversation.create(user,prompt);when(store.create(user,prompt)).thenReturn(c);when(store.getMessages(c.getId(),user)).thenReturn(List.of());
  AgentResponse response=new AgentResponse("final_answer",List.of(),"Hi");when(agent.runTurn(eq(user),eq(prompt),anyList())).thenReturn(new AgentTurnResult(response,List.of(new LLMMessage("assistant","Hi"))));
  var result=service.chat(user,null,prompt);assertEquals(c.getId(),result.conversationId());assertEquals(response,result.response());
  verify(store).append(eq(c.getId()),eq(user),argThat(ms->ms.size()==1&&"user".equals(ms.getFirst().role())));
  verify(store).append(eq(c.getId()),eq(user),argThat(ms->ms.size()==1&&"assistant".equals(ms.getFirst().role())));
 }
 @Test void verifiesOwnershipBeforeUsingExistingConversation(){
  UUID id=UUID.randomUUID();when(store.getMessages(id,"user-1")).thenReturn(List.of());when(store.getOwnedConversation(id,"user-1")).thenReturn(Conversation.create("user-1","Title"));
  when(agent.runTurn(eq("user-1"),eq("Hello"),anyList())).thenReturn(new AgentTurnResult(new AgentResponse("final_answer",List.of(),"Hi"),List.of()));
  service.chat("user-1",id,"Hello");verify(store).getOwnedConversation(id,"user-1");verify(store,never()).create(anyString(),anyString());
 }
 @Test void delegatesConversationReadAndDeleteOperations(){UUID id=UUID.randomUUID();service.getConversations("user-1");service.getConversation(id,"user-1");service.deleteConversation(id,"user-1");verify(store).getConversations("user-1");verify(store).getOwnedConversation(id,"user-1");verify(store).getVisibleMessages(id,"user-1");verify(store).deleteConversation(id,"user-1");}
}