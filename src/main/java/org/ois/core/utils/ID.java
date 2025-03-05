package org.ois.core.utils;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ID {

    private static final ConcurrentHashMap<String, AtomicLong> topicCounters = new ConcurrentHashMap<>();
    private final String topic;
    private final long id;

    private ID(String topic) {
        this.topic = topic;
        this.id = topicCounters.computeIfAbsent(topic, k -> new AtomicLong()).incrementAndGet();
    }

    public static ID generate() {
        return generate("");
    }

    public static ID generate(String topic) {
        if (topic == null || topic.isBlank()) {
            topic = "";
        }
        return new ID(topic);
    }

    public String getTopic() {
        return topic;
    }

    public long getId() {
        return id;
    }

    public boolean isSameTopic(ID other) {
        return this.topic.equals(other.topic);
    }

    public boolean isSameId(ID other) {
        return this.id == other.id && this.topic.equals(other.topic);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ID uniqueID = (ID) obj;
        return id == uniqueID.id && Objects.equals(topic, uniqueID.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic, id);
    }

    @Override
    public String toString() {
        return topic.isBlank() ? "" + id : topic + "-" + id;
    }
}
