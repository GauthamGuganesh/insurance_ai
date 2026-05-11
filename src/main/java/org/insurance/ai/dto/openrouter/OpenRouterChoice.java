package org.insurance.ai.dto.openrouter;

public class OpenRouterChoice {
    
    private OpenRouterMessage message;
    private String finish_reason;
    private Integer index;

    public OpenRouterMessage getMessage() {
        return message;
    }

    public void setMessage(OpenRouterMessage message) {
        this.message = message;
    }

    public String getFinish_reason() {
        return finish_reason;
    }

    public void setFinish_reason(String finish_reason) {
        this.finish_reason = finish_reason;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }
}
