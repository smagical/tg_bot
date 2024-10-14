package io.github.smagical.bot.listener;

import io.github.smagical.bot.event.Event;

import java.lang.reflect.ParameterizedType;

public interface Listener<T extends Event> {
    default boolean support(Event event){
        Class<?> tClass = null;
        try {
            tClass = (Class<?>) (((ParameterizedType)getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0]);
        }catch(Exception e){
            return false;
        }
        return tClass.isAssignableFrom(event.getClass());
    };

    void onListener(T event);
    default boolean next() {
        return true;
    }
    default int getOrder(){
        return Integer.MAX_VALUE;
    };
}
