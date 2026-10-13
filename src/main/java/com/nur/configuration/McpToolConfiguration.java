package com.nur.configuration;

import com.nur.tools.EmployeeTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolConfiguration {

    @Bean
    public ToolCallbackProvider employeeToolCallbackProvider(EmployeeTools employeeTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(employeeTools)
                .build();
    }
}
