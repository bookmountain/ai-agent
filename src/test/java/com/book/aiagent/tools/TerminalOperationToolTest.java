package com.book.aiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TerminalOperationToolTest {

    @Test
    public void executeTerminalCommand() {
        TerminalOperationTool tool = new TerminalOperationTool();
        String command = "ls -l";
        String result = tool.executeTerminalCommand(command);
        Assertions.assertNotNull(result);
    }
}
