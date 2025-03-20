package com.yitianyigexiangfa;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransport;
import io.modelcontextprotocol.spec.McpSchema;

import java.util.ArrayList;
import java.util.List;

public class HelloWorldMcpServer {
    public static void main(String[] args) {
        StdioServerTransport transport = new StdioServerTransport(new ObjectMapper());

        var capabilities = McpSchema.ServerCapabilities.builder()
                .tools(true)            // Tool support with list changes notifications
                .logging()              // Enable logging support (enabled by default with loging level INFO)
                .build();

// Create a server with custom configuration
        McpSyncServer syncServer = McpServer.sync(transport)
                .serverInfo("my-server", "1.0.0")
                .capabilities(capabilities)
                .build();

// Register a tool
        // Sync tool registration
        var schema = """
                {
                  "type" : "object",
                  "id" : "urn:jsonschema:Operation",
                  "properties" : {
                    "operation" : {
                      "type" : "string"
                    },
                    "a" : {
                      "type" : "number"
                    },
                    "b" : {
                      "type" : "number"
                    }
                  }
                }
                """;
        var syncToolRegistration = new McpServerFeatures.SyncToolRegistration(
                new McpSchema.Tool("calculator", "Basic calculator", schema),
                arguments -> {
                    List<McpSchema.Content> result = new ArrayList<>();
                    var textContent = new McpSchema.TextContent("Hello world");
                    result.add(textContent);
                    // Tool implementation
                    return new McpSchema.CallToolResult(result, false);
                }
        );


// Register tools, resources, and prompts
        syncServer.addTool(syncToolRegistration);

// Send logging notifications
        syncServer.loggingNotification(McpSchema.LoggingMessageNotification.builder()
                .level(McpSchema.LoggingLevel.INFO)
                .logger("custom-logger")
                .data("Server initialized")
                .build());
    }
}