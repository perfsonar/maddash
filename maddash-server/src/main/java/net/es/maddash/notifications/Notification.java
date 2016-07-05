package net.es.maddash.notifications;

import javax.json.JsonObject;

import java.util.List;

public interface Notification {
    
    public void init(String name, JsonObject params);
    public void send(List<NotifyProblem> problems);
}
