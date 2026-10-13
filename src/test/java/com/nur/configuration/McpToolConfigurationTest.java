package com.nur.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import com.nur.tools.EmployeeTools;
import org.junit.jupiter.api.Test;

class McpToolConfigurationTest {

    @Test
    void registersAllEmployeeToolsAsCallbacks() {
        var provider = new McpToolConfiguration()
                .employeeToolCallbackProvider(new EmployeeTools());

        assertThat(provider.getToolCallbacks()).hasSize(4);
    }
}
