package com.youtubeagent.ai.routing;
import com.youtubeagent.ai.catalog.ModelCatalog;
import com.youtubeagent.ai.model.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class DefaultModelSelectorTest {
 @Test void filtersRoleFreeModelsAndOrdersByPriority(){
  ModelDefinition slow=new ModelDefinition("slow","p",true,Set.of(ModelCapability.TOOL_CALLING,ModelCapability.JSON,ModelCapability.CHAT),20);
  ModelDefinition fast=new ModelDefinition("fast","p",true,Set.of(ModelCapability.TOOL_CALLING,ModelCapability.JSON),1);
  ModelDefinition paid=new ModelDefinition("paid","p",false,Set.of(ModelCapability.TOOL_CALLING,ModelCapability.JSON),0);
  ModelDefinition response=new ModelDefinition("response","p",true,Set.of(ModelCapability.CHAT),0);
  ModelCatalog catalog=mock(ModelCatalog.class);when(catalog.getModels()).thenReturn(List.of(slow,paid,response,fast));
  assertEquals(List.of(fast,slow),new DefaultModelSelector(catalog).select(ModelRole.DECISION,new ModelRequirements(true,Set.of())));
 }
 @Test void filtersRequiredCapabilitiesForResponseRole(){
  ModelDefinition chat=new ModelDefinition("chat","p",true,Set.of(ModelCapability.CHAT,ModelCapability.VISION),1);
  ModelDefinition chatJson=new ModelDefinition("chat-json","p",true,Set.of(ModelCapability.CHAT,ModelCapability.JSON),2);
  ModelCatalog catalog=mock(ModelCatalog.class);when(catalog.getModels()).thenReturn(List.of(chat,chatJson));
  assertEquals(List.of(chatJson),new DefaultModelSelector(catalog).select(ModelRole.RESPONSE,new ModelRequirements(true,Set.of(ModelCapability.JSON))));
 }
}