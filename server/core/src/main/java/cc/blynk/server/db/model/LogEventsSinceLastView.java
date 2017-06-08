package cc.blynk.server.db.model;

import cc.blynk.server.core.model.web.product.EventType;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 09.06.17.
 */
public class LogEventsSinceLastView {

    public final int deviceId;

    public final EventType eventType;

    public LogEventsSinceLastView(int deviceId, EventType eventType) {
        this.deviceId = deviceId;
        this.eventType = eventType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LogEventsSinceLastView)) return false;

        LogEventsSinceLastView that = (LogEventsSinceLastView) o;

        if (deviceId != that.deviceId) return false;
        return eventType == that.eventType;

    }

    @Override
    public int hashCode() {
        int result = deviceId;
        result = 31 * result + (eventType != null ? eventType.hashCode() : 0);
        return result;
    }
}
