package io.github.smagical.bot.bot.model;


import org.drinkless.tdlib.TdApi;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

public class UserMap  {
    public static class Entity{
        private TdApi.User user;
        private TdApi.UserFullInfo fullInfo;

        public Entity(TdApi.User user) {
            this.user = user;
        }

        public Entity(TdApi.UserFullInfo fullInfo) {
            this.fullInfo = fullInfo;
        }

        public Entity(TdApi.User user, TdApi.UserFullInfo fullInfo) {
            this.user = user;
            this.fullInfo = fullInfo;
        }

        public TdApi.User getUser() {
            return user;
        }
        public void setUser(TdApi.User user) {
            this.user = user;
        }
        public TdApi.UserFullInfo getFullInfo() {
            return fullInfo;
        }
        public void setFullInfo(TdApi.UserFullInfo fullInfo) {
            this.fullInfo = fullInfo;
        }

        @Override
        public String toString() {
            return "User [user=" + user.firstName + user.lastName+ ":" + user.id + "]";
        }
    }

    private ConcurrentHashMap<Long, Entity> entitys = new ConcurrentHashMap<Long, Entity>();

    public void put(TdApi.User user) {
        entitys.merge(user.id, new Entity(user),
                (a,b)->{
                    a.user = user;
                    return a;
                });
    }

    public void put(Long userId,TdApi.UserFullInfo fullInfo) {
        entitys.merge(userId, new Entity(fullInfo),
                (a,b)->{
                    a.fullInfo = fullInfo;
                    return a;
                });
    }

    public void put(TdApi.User user, TdApi.UserFullInfo fullInfo) {
        Entity entity = new Entity(user,fullInfo);
        entitys.put(entity.user.id, entity);
    }

    public TdApi.User getUser(Long userId) {
        if (entitys.containsKey(userId)) {
            return entitys.get(userId).user;
        }
        return null;
    }
    public TdApi.UserFullInfo getFullInfo(Long userId) {
        if (entitys.containsKey(userId)) {
            return entitys.get(userId).fullInfo;
        }
        return null;
    }
    public Entity getEntity(Long userId) {
        return entitys.get(userId);
    }
    public boolean remove(Long userId) {
        entitys.remove(userId);
        return true;
    }

    public Collection<Entity> getEntities() {
        return Collections.unmodifiableMap(entitys).values();
    }

}
