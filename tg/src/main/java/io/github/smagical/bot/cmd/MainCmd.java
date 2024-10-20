package io.github.smagical.bot.cmd;

import io.github.smagical.bot.bot.Bot;
import io.github.smagical.bot.bot.handler.user.authorization.state.ClosedHandler;
import io.github.smagical.bot.bot.handler.user.authorization.state.ClosingHandler;
import io.github.smagical.bot.bot.model.UserMap;
import io.github.smagical.bot.bot.util.ClientUtils;
import io.github.smagical.bot.event.Event;
import io.github.smagical.bot.event.user.LoginEvent;
import io.github.smagical.bot.event.user.LogoutEvent;
import io.github.smagical.bot.listener.Listener;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import java.util.*;

@Slf4j
public class MainCmd implements Cmd {
    private static List<Bot> bots = new ArrayList<Bot>();
    private static List<Cmd> cmds = new ArrayList<>();

    public static void addCmd(Cmd cmd) {
        synchronized (cmds) {
            cmds.add(cmd);
        }
    }
    public static void removeCmd(Cmd cmd) {
        synchronized (cmds) {
            cmds.remove(cmd);
        }
    }

    private static void addBot(Bot bot) {
        synchronized (bots) {
            bots.add(bot);
        }
    }
    private static void removeBot(Bot bot) {
        synchronized (bots) {
            bots.remove(bot);
        }
    }

    public static void printMainMenu(){
        while (true){
            printSplit();
            log.info("###################  Main Menu  #########################");
            synchronized (bots) {
                for (int i = 0; i < bots.size(); i++) {
                    Bot bot = bots.get(i);
                    log.info("{}:{}:{}",
                            i,
                            bot.getMe()!=null?bot.getMe().phoneNumber:"",
                            bot.getMe()!=null?(bot.getMe().firstName+" "+bot.getMe().lastName):"");
                }
            }
            printSplit();
            String select = select("请选择:\n");
            try {
                int choice = Integer.parseInt(select);
                if (choice >= 0 && choice < bots.size()) {
                    printBotMenu(bots.get(choice));
                }

                }catch (Exception e) {}

        }
    }

    private static   void printBotMenu(Bot bot){
       while (true){
           printSplit();
           log.info("###################  Bot Menu  #########################");
           log.info("cl get chat list");
           log.info("ul get user list");
           log.info("me get me information");
           log.info("se chatId message");
           log.info("rse chatId replyMessageId message");
           log.info("gh chatId offset limit");
           log.info("ext extend cmds");
           log.info("exit back MainMenu");
           printSplit();
           String select = select("请输入命令:\n");
           if (select.startsWith("cl")){
               for (TdApi.Chat chat : bot.getAllChats().getAllChat()) {
                   log.info("{}:{}",chat.title,chat.id);
               }
           }else if (select.startsWith("ul")){
               for (UserMap.Entity entity : bot.getAllUser().getEntities()) {
                   log.info("{}",entity);
               }
           }else if (select.startsWith("me")){
               log.info("{}:{}:{}",
                       bot.getMe().id,
                       bot.getMe()!=null?bot.getMe().phoneNumber:"",
                       bot.getMe()!=null?(bot.getMe().firstName+" "+bot.getMe().lastName):"");

           }else if (select.startsWith("se")){
               String[] args = select.split(" ");
               if (args.length != 3){
                   continue;
               }
               TdApi.InputMessageContent content = new TdApi.InputMessageText(
                       new TdApi.FormattedText(args[2],null),null,true
               );
               bot.getClient().send(
                       new TdApi.SendMessage(
                               Long.parseLong(args[1]), 0, null, null, null, content
                       ),
                       new Client.ResultHandler() {
                           @Override
                           public void onResult(TdApi.Object object) {
                               log.info(object.toString());
                           }
                       }
               );
           }else if (select.startsWith("rse")){
               String[] args = select.split(" ");
               if (args.length != 4){
                   continue;
               }
               TdApi.InputMessageContent content = new TdApi.InputMessageText(
                       new TdApi.FormattedText(args[3],null),null,true
               );
               TdApi.InputMessageReplyTo replyTo = new TdApi.InputMessageReplyToMessage(
                       Long.parseLong(args[2]),null
               );
               bot.getClient().send(
                       new TdApi.SendMessage(
                               Long.parseLong(args[1]), 0, replyTo, null, null, content
                       ),
                       new Client.ResultHandler() {
                           @Override
                           public void onResult(TdApi.Object object) {
                               log.info(object.toString());
                           }
                       }
               );
           }else if (select.startsWith("gh")){
               String[] args = select.split(" ");
               if (args.length != 4){
                   continue;
               }
               try {
                  Collection<TdApi.Message> messages =  ClientUtils.getChatHistory(
                           bot.getClient(),Long.parseLong(args[1]),Long.parseLong(args[2]),Integer.parseInt(args[3])
                   );
                   for (TdApi.Message message : messages) {
                       log.info("{}",message);
                   }
               } catch (InterruptedException e) {
                   throw new RuntimeException(e);
               }
           }else if (select.startsWith("ext")){
               printExtendCmds(bot);
           }else if (select.startsWith("exit")){
               break;
           }

       }
    }

    static void printExtendCmds(Bot bot){
        while (true){
            printSplit();
            log.info("###################  Extend Menu  #########################");
            synchronized (cmds){
                for (int i = 0; i < cmds.size(); i++) {
                    log.info("{}\t{}",i,cmds.get(i).getName());
                }
                log.info("{}\t{}",cmds.size(),"go back bot menu");
                printSplit();
                String select = select("请选择:\n");
                try {
                    int choice = Integer.parseInt(select);
                    if (choice == cmds.size()){
                        break;
                    }
                    if (choice >= 0 && choice < cmds.size()) {
                        cmds.get(choice).printMainMenu(bot);
                    }

                }catch (Exception e) {}
            }
        }
    }

//gh -4548136454 0 5
     static void printMenu(String content){
        printSplit();
        log.info(content);
        printSplit();

    }

     static void printSplit(){
        log.info("#######################################################");
    }
     static String select(String content){
        log.info(content);
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        return input;
    }

    @Override
    public void printMainMenu(Bot bot) {
        printMainMenu();
    }

    public static class CmdListener implements Listener{
        private final static List<Class> sup =
                Arrays.asList(
                        LoginEvent.LoginSuccessEvent.class,
                        LogoutEvent.class,
                        ClosedHandler.ClosedEvent.class);
        private Bot bot;
        public CmdListener(Bot bot) {
            this.bot = bot;
        }
        @Override
        public boolean support(Event event) {
            return sup
                    .stream()
                    .filter(e->e.isAssignableFrom(event.getClass()))
                    .findFirst()
                    .isPresent();
        }

        @Override
        public void onListener(Event event) {
            if (event instanceof LoginEvent.LoginSuccessEvent){
                MainCmd.addBot(bot);
            }else if (event instanceof LogoutEvent){
                MainCmd.removeBot(bot);
            }else if (event instanceof ClosingHandler.ClosingEvent){
                MainCmd.removeBot(bot);
            }
        }
    }
}
