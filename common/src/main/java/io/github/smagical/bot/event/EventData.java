package io.github.smagical.bot.event;

import java.util.concurrent.atomic.AtomicLong;

public class EventData<T> {
    private String eventId = Long.toHexString(counter.incrementAndGet());
    private T data ;


    public EventData(T data) {
        this.data = data;
    }


    public String getEventId() {
        return eventId;
    }
    public T getData() {
        return data;
    }
    public static AtomicLong counter = new AtomicLong();
    public static <T>  EventData<T>  warp(T data){
        return new EventData<>(data);
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
