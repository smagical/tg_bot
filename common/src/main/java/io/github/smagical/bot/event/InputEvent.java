package io.github.smagical.bot.event;

public interface InputEvent<T,V> extends Event<V> {
    public T getMessage();
}
