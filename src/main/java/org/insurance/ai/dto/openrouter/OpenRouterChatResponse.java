package org.insurance.ai.dto.openrouter;

import java.util.List;

public class OpenRouterChatResponse {
    
    private String id;
    private String object;
    private Long created;
    private String model;
    private List<OpenRouterChoice> choices;
    private OpenRouterUsage usage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<OpenRouterChoice> getChoices() {
        return choices;
    }

    public void setChoices(List<OpenRouterChoice> choices) {
        this.choices = choices;
    }

    public OpenRouterUsage getUsage() {
        return usage;
    }

    public void setUsage(OpenRouterUsage usage) {
        this.usage = usage;
    }
}
