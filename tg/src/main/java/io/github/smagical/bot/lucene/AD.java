package io.github.smagical.bot.lucene;

import io.github.smagical.bot.util.DbUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class AD {
    private static List<Predicate<TgLucene.TgMessage>> predicates = new ArrayList<>();

    static {
        synchronized (predicates) {
            predicates.add(new AblumPredicate());
            predicates.add(new TextPredicate());
        }
    }

    public static boolean adFilter(TgLucene.TgMessage message) {
        synchronized (predicates) {
            for (Predicate predicate : predicates) {
                if (predicate.test(message)) {
                    return true;
                }
            }
        }
        return false;
    }


    private static class TextPredicate implements Predicate<TgLucene.TgMessage> {
        private final static Set<String> list = new HashSet<>();
        static {
            synchronized (list) {
                    try (
                            InputStreamReader reader = new InputStreamReader(
                                    DbUtil.class.getClassLoader()
                                            .getResourceAsStream("AD.txt")
                            );
                            BufferedReader br = new BufferedReader(reader)
                    ){
                        StringBuilder builder = new StringBuilder();
                        br.lines().forEach(list::add);
                    } catch (FileNotFoundException e) {
                    } catch (IOException e) {
                    }

            }
        }
        @Override
        public boolean test(TgLucene.TgMessage tgMessage) {
            if (tgMessage.content == null) {
                return true;
            }
            for (String string : list) {
                if (tgMessage.content.contains(string)) {
                    return true;
                }
            }
            return false;
        }
    }
    private static class AblumPredicate implements Predicate<TgLucene.TgMessage> {
        @Override
        public boolean test(TgLucene.TgMessage tgMessage) {
            return tgMessage.album == 0;
        }
    }

}
