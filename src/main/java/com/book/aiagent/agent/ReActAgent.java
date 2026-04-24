package com.book.aiagent.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class ReActAgent extends BaseAgent {
    public abstract boolean think();

    public abstract String act();

    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
                return "Complete thinking, no action needed.";
            }
            return act();
        } catch (Exception e) {
            e.printStackTrace();
            return "Step execution failed：" + e.getMessage();
        }
    }

}
