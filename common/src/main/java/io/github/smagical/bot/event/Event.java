package io.github.smagical.bot.event;

public interface Event<T> {
    EventData<T> getData();
    String getOriginalData();
}
