package com.youtubeagent.agent.tools;

import com.youtubeagent.agent.AgentTool;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CalculatorTool implements AgentTool {

    @Override
    public String getName() {
        return "calculate";
    }

    @Override
    public String getDescription() {
        return "Performs basic arithmetic calculations. " 
                + "Supports addition, subtraction, multiplication, and division. " 
                + "And arguments should contain expression as String. Example expressions: '4 * 5', '16 + 4', '20 / 4', '90 - 1'";
    }

    @Override
    public Object execute(Map<String, Object> arguments) {

        Object expression = arguments.get("expression");

        if (expression == null) {
            throw new IllegalArgumentException(
                    "Missing required argument: expression");
        }

        return calculate(expression.toString());
    }

    private double calculate(String expression) {

        String normalized = expression.replaceAll("\\s+", "");

        if (normalized.contains("+")) {
            String[] parts = normalized.split("\\+", 2);
            return Double.parseDouble(parts[0])
                    + Double.parseDouble(parts[1]);
        }

        if (normalized.contains("-")) {
            String[] parts = normalized.split("-", 2);
            return Double.parseDouble(parts[0])
                    - Double.parseDouble(parts[1]);
        }

        if (normalized.contains("*")) {
            String[] parts = normalized.split("\\*", 2);
            return Double.parseDouble(parts[0])
                    * Double.parseDouble(parts[1]);
        }

        if (normalized.contains("/")) {
            String[] parts = normalized.split("/", 2);

            double divisor = Double.parseDouble(parts[1]);

            if (divisor == 0) {
                throw new IllegalArgumentException(
                        "Cannot divide by zero");
            }

            return Double.parseDouble(parts[0]) / divisor;
        }

        throw new IllegalArgumentException(
                "Unsupported expression: " + expression);
    }
}