package io.github.smagical.bot.bot.util;

import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import java.util.Collection;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ClientUtils {
    public final static int LIMIT = 50;
    public final static int RETRY = 5;

    public static Collection<TdApi.Message> getChatHistory(
            Client client,Long chatId,long lastMessageId,int limit
    ) throws InterruptedException {
        PriorityQueue<TdApi.Message> res =
                new PriorityQueue<TdApi.Message>(
                        (a,b)->Integer.compare(a.date,b.date)
                );
        Set<Long> dist = new HashSet<Long>();
        dist.add(lastMessageId);
        final AtomicBoolean flag = new AtomicBoolean(true);
        final AtomicInteger count = new AtomicInteger(RETRY);

        while (flag.get() && count.get() > 0) {
            int lastCount = res.size();
            CountDownLatch latch = new CountDownLatch(1);
            TdApi.GetChatHistory getChatHistory = new TdApi.GetChatHistory(
                    chatId,lastMessageId,0,limit - res.size() +1 > LIMIT?LIMIT:limit - res.size() +1,false
            );
            client.send(
                    getChatHistory,
                    new Client.ResultHandler() {
                        @Override
                        public void onResult(TdApi.Object object) {
                            switch (object.getConstructor()){
                                case TdApi.Error.CONSTRUCTOR -> {
                                    flag.set(false);
                                    latch.countDown();
                                    log.error("{}",object);
                                }
                                case TdApi.Messages.CONSTRUCTOR -> {
                                    TdApi.Messages messages = (TdApi.Messages)object;
                                    TdApi.Message[] messagesArray = messages.messages;
                                    for (int i = 0; i < messagesArray.length; i++) {
                                        if (dist.contains(messagesArray[i].id)) {
                                            continue;
                                        }
                                        res.add(messagesArray[i]);
                                        dist.add(messagesArray[i].id);
                                    }
                                    if (res.size() >= limit) {
                                        flag.set(false);
                                    }
                                    latch.countDown();
                                }
                            }
                        }
                    }
            );
            if (!res.isEmpty())
                lastMessageId = res.peek().id;
            latch.await();
            if (res.size() == lastCount){
                count.decrementAndGet();
            }else {
                count.set(RETRY);
            }
        }
        while (res.size() > limit) res.poll();
        return res;
    }

    public static String getMessageLink(Client client,Long chatId,Long messageId) throws InterruptedException {
        final StringBuilder result = new StringBuilder();
        TdApi.GetMessageLink getMessageLink = new TdApi.GetMessageLink(
                chatId,messageId,0,true,false
        );
        CountDownLatch latch = new CountDownLatch(1);
        client.send(getMessageLink, new Client.ResultHandler() {

            @Override
            public void onResult(TdApi.Object object) {
                if (object.getConstructor() == TdApi.MessageLink.CONSTRUCTOR) {
                    result.append(((TdApi.MessageLink)object).link) ;
                }
                latch.countDown();
            }
        });
        latch.await();
        return result.length() == 0 ? null : result.toString();
    }

}
