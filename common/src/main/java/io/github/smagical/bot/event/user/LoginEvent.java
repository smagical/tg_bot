package io.github.smagical.bot.event.user;

import io.github.smagical.bot.event.BaseEvent;

public abstract class LoginEvent<T> extends BaseEvent<T> {
    public static class LoginSuccessEvent<T> extends LoginEvent<T> {

        public LoginSuccessEvent(T data) {
            super(data);
        }
    }

    public static class LoginFailureEvent<T> extends LoginEvent<T> {

        public LoginFailureEvent(T data) {
            super(data);
        }
    }


    public LoginEvent(T data) {
        super(data);
    }


}