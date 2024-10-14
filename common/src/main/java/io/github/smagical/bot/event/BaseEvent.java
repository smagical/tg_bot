package io.github.smagical.bot.event;

public  class BaseEvent<T> implements Event<T> {

    private  final EventData<T> code;

    public BaseEvent( EventData<T> code) {
        this.code = code;
    }
    public BaseEvent(T code) {
        this.code = EventData.warp(code);
    }

    @Override
    public EventData<T> getData() {
        return this.code;
    }

    @Override
    public String getOriginalData() {
        return this.code.toString();
    }

}
