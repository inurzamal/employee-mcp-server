package com.nur.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.nur.service.EmployeeService;
import com.nur.tools.EmployeeTools;
import org.junit.jupiter.api.Test;

class McpToolConfigurationTest {

    @Test
    void registersAllEmployeeToolsAsCallbacks() {
        EmployeeService employeeService = mock(EmployeeService.class);
        var provider = new McpToolConfiguration()
                .employeeToolCallbackProvider(new EmployeeTools(employeeService));

        assertThat(provider.getToolCallbacks()).hasSize(6);
    }
}
