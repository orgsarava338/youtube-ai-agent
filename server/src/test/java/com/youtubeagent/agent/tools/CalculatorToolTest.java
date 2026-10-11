package com.youtubeagent.agent.tools;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
class CalculatorToolTest {
 private final CalculatorTool tool=new CalculatorTool();
 @Test void supportsBasicArithmetic(){
  assertEquals(9.0,(double)tool.execute(Map.of("expression","4 + 5")));
  assertEquals(15.0,(double)tool.execute(Map.of("expression","20 - 5")));
  assertEquals(20.0,(double)tool.execute(Map.of("expression","4 * 5")));
  assertEquals(4.0,(double)tool.execute(Map.of("expression","20 / 5")));
 }
 @Test void ignoresWhitespace(){assertEquals(20.0,(double)tool.execute(Map.of("expression"," 4 * 5 ")));}
 @Test void rejectsMissingExpression(){assertEquals("Missing required argument: expression",assertThrows(IllegalArgumentException.class,()->tool.execute(Map.of())).getMessage());}
 @Test void rejectsDivisionByZero(){assertEquals("Cannot divide by zero",assertThrows(IllegalArgumentException.class,()->tool.execute(Map.of("expression","12 / 0"))).getMessage());}
 @Test void rejectsUnsupportedExpression(){assertThrows(IllegalArgumentException.class,()->tool.execute(Map.of("expression","2 ^ 3")));}
}