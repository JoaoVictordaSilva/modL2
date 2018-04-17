package com.it.br.gameserver.model.entity.event.championship.schedule;

import com.it.br.gameserver.Announcements;
import com.it.br.gameserver.model.entity.event.championship.game.ChampionshipEvent;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static com.it.br.gameserver.model.entity.event.championship.util.ChampionshipConstants.*;

public class ChampionshipEventSchedule implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ChampionshipEventSchedule.class.getName());
    private static ChampionshipEventSchedule INSTANCE;

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private ChampionshipEventSchedule() {
        LOGGER.info("Starting engine([ChampionshipEvent])");
        scheduler.schedule(this, delay(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        Calendar calendar = Calendar.getInstance(TIME_ZONE);
        long delay;

        if (DAY_OF_WEEK == calendar.get(Calendar.DAY_OF_WEEK)) {

            calendar.set(Calendar.DAY_OF_WEEK, DAY_OF_WEEK);
            calendar.set(Calendar.HOUR_OF_DAY, HOUR_OF_DAY);
            calendar.set(Calendar.MINUTE, MINUTE);
            calendar.set(Calendar.SECOND, SECOND);

            if (initialDelay(calendar) > 0)
                announce(initialDelay(calendar));

            if (initialDelay(calendar) > PER_HOUR) {
                delay = PER_HOUR;
            } else if (initialDelay(calendar) > PER_HALF_HOUR) {
                delay = PER_HALF_HOUR;
            } else if (initialDelay(calendar) > PER_TWENTY_MINUTE) {
                delay = PER_TWENTY_MINUTE;
            } else if (initialDelay(calendar) > PER_FIVE_MINUTE) {
                delay = PER_FIVE_MINUTE;
            } else if (initialDelay(calendar) > PER_MINUTE) {
                delay = PER_MINUTE;
            } else if (initialDelay(calendar) > THIRTY_SECONDS) {
                delay = THIRTY_SECONDS;
            } else if (initialDelay(calendar) > TWENTY_SECONDS) {
                delay = TWENTY_SECONDS;
            } else if (initialDelay(calendar) > 0) {
                delay = PER_SECOND;
            } else {
                Announcements.getInstance().gameAnnounceToAll(THAT_THE_BEST_WIN);
                new Thread(ChampionshipEvent.LAZY_HOLDER.getInstance()).start();

                calendar = Calendar.getInstance(TIME_ZONE);
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                calendar.set(Calendar.HOUR, 0);

                delay = initialDelay(calendar);
            }
        } else {
            calendar.add(Calendar.HOUR, 1);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            Announcements.getInstance().gameAnnounceToAll("Register to Championship Event .info");
            delay = initialDelay(calendar);
        }

        if (delay < 0)
            delay = -delay;
        scheduler.schedule(this, delay, TimeUnit.MILLISECONDS);

    }

    private static long initialDelay(Calendar calendar) {
        return calendar.getTimeInMillis() - Calendar.getInstance(TIME_ZONE).getTimeInMillis();
    }

    private long delay() {
        Calendar calendar = Calendar.getInstance(TIME_ZONE);
        calendar.add(Calendar.HOUR, 1);
        calendar.set(Calendar.MINUTE, MINUTE);
        calendar.set(Calendar.SECOND, 0);
        return initialDelay(calendar);
    }

    public static class LAZY_HOLDER {

        public static ChampionshipEventSchedule schedule() {
            if (INSTANCE == null)
                INSTANCE = new ChampionshipEventSchedule();
            return INSTANCE;
        }
    }

    private static void announce(long time) {
        time = time / 1000;
        if (time >= 3600) {
            Announcements.getInstance().gameAnnounceToAll("Championship Event: " + (time / 60 / 60) + " hour(s) until event start!");
        } else if (time >= 60) {
            Announcements.getInstance().gameAnnounceToAll("Championship Event: " + (time / 60) + " minute(s) until event start!");
        } else {
            Announcements.getInstance().gameAnnounceToAll("Championship Event: " + time + " second(s) until event start!");
        }
    }

    public static void main(String[] args) {
        Calendar instance = Calendar.getInstance(TIME_ZONE);
        instance.set(Calendar.HOUR_OF_DAY, 22);
        instance.set(Calendar.MINUTE, mockMinute);
        instance.set(Calendar.SECOND, 0);
        scheduler.schedule(new Teste(), initialDelay(instance), TimeUnit.MILLISECONDS);
    }

    static final int mockMinute = 17;
    static final int mockPosMinute = mockMinute;

    static class Teste implements Runnable {

        @Override
        public void run() {

            Calendar calendar = Calendar.getInstance(TIME_ZONE);
            long delay;

            if (Calendar.FRIDAY == calendar.get(Calendar.DAY_OF_WEEK)) {

                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, mockPosMinute);
                calendar.set(Calendar.SECOND, SECOND);

                if (initialDelay(calendar) > 0)
                    announce(initialDelay(calendar));

                if (initialDelay(calendar) > PER_HOUR) {
                    delay = PER_HOUR;
                } else if (initialDelay(calendar) > PER_HALF_HOUR) {
                    delay = PER_HALF_HOUR;
                } else if (initialDelay(calendar) > PER_TWENTY_MINUTE) {
                    delay = PER_TWENTY_MINUTE;
                } else if (initialDelay(calendar) > PER_FIVE_MINUTE) {
                    delay = PER_FIVE_MINUTE;
                } else if (initialDelay(calendar) > PER_MINUTE) {
                    delay = PER_MINUTE;
                } else if (initialDelay(calendar) > THIRTY_SECONDS) {
                    delay = THIRTY_SECONDS;
                } else if (initialDelay(calendar) > TWENTY_SECONDS) {
                    delay = TWENTY_SECONDS;
                } else if (initialDelay(calendar) > 0) {
                    delay = PER_SECOND;
                } else {

                    new Thread(ChampionshipEvent.LAZY_HOLDER.getInstance()).start();

                    calendar = Calendar.getInstance(TIME_ZONE);
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                    calendar.set(Calendar.HOUR, 0);

                    delay = initialDelay(calendar);
                }

            } else {
                calendar.add(Calendar.HOUR, 1);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                Announcements.getInstance().gameAnnounceToAll("Register to Championship Event .info");
                delay = initialDelay(calendar);
            }
            if (delay < 0)
                delay = -delay;
            scheduler.schedule(this, delay, TimeUnit.MILLISECONDS);

        }
    }
}
