package net.es.maddash.notifications;

import javax.json.JsonObject;

import java.util.List;

public interface Notification {
    
    public void init(String name, JsonObject params);
    public void send(int notificationId, List<NotifyProblem> problems, List<String> resolvedData);
}
