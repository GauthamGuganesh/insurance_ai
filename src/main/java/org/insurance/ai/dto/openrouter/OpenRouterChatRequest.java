package org.insurance.ai.dto.openrouter;

import java.util.List;

public class OpenRouterChatRequest {
    
    private String model;
    private List<OpenRouterMessage> messages;
    private Integer max_tokens;
    private Double temperature;
    private String site_url;
    private String app_name;

    public OpenRouterChatRequest() {}

    public OpenRouterChatRequest(String model, List<OpenRouterMessage> messages, Integer max_tokens, 
                               Double temperature, String site_url, String app_name) {
        this.model = model;
        this.messages = messages;
        this.max_tokens = max_tokens;
        this.temperature = temperature;
        this.site_url = site_url;
        this.app_name = app_name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<OpenRouterMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<OpenRouterMessage> messages) {
        this.messages = messages;
    }

    public Integer getMax_tokens() {
        return max_tokens;
    }

    public void setMax_tokens(Integer max_tokens) {
        this.max_tokens = max_tokens;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public String getSite_url() {
        return site_url;
    }

    public void setSite_url(String site_url) {
        this.site_url = site_url;
    }

    public String getApp_name() {
        return app_name;
    }

    public void setApp_name(String app_name) {
        this.app_name = app_name;
    }
}
