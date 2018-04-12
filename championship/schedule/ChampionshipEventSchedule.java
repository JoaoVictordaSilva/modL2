package com.it.br.gameserver.model.entity.event.championship.schedule;

import com.it.br.gameserver.Announcements;
import com.it.br.gameserver.model.entity.event.championship.game.ChampionshipEvent;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static com.it.br.gameserver.model.entity.event.championship.util.ChampionshipConstants.*;

public class ChampionshipEventSchedule implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ChampionshipEventSchedule.class.getName());
    private static ChampionshipEventSchedule INSTANCE;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

    private ChampionshipEventSchedule() {
        LOGGER.info("Starting engine([ChampionshipEvent])");
        scheduler.schedule(this, delay(), TimeUnit.MILLISECONDS);
    }

    public void start(Calendar calendar) {
        calendar.set(Calendar.DAY_OF_WEEK, DAY_OF_WEEK);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 11);
        calendar.set(Calendar.MINUTE, 4);
        calendar.set(Calendar.SECOND, 50);

        scheduler.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Executor Service");
            }
        }, initialDelay(calendar), 60000, TimeUnit.MILLISECONDS);
    }

    private void scheduleEvent(Calendar calendar) {

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                new Thread(ChampionshipEvent.LAZY_HOLDER.getInstance()).start();
            }
        }, initialDelay(calendar));

    }

    private class StartAnnounceTask implements Runnable {

        private long timeMillis;

        public StartAnnounceTask(long timeMillis) {
            this.timeMillis = timeMillis;
        }

        @Override
        public void run() {
            long delay = timeMillis - Calendar.getInstance(TIME_ZONE).getTimeInMillis();

            if (delay > PER_HOUR) {
                delay = PER_HOUR;
            } else if (delay > PER_HALF_HOUR) {
                delay = PER_HALF_HOUR;
            } else if (delay > PER_TEN_MINUTE) {
                delay = PER_TEN_MINUTE;
            } else if (delay > PER_FIVE_MINUTE) {
                delay = PER_MINUTE;
            } else if (delay > PER_MINUTE) {
                delay = THIRTY_SECONDS;
            } else if (delay > FIFTEENTH) {
                delay = FIFTEENTH;
            } else if (delay > TEN_SECONDS) {
                delay = FIVE_SECONDS;
            } else {
                delay = PER_SECOND;
            }

            scheduler.schedule(this, delay, TimeUnit.MILLISECONDS);

        }
    }

    @Override
    public void run() {
        Calendar calendar = Calendar.getInstance(TIME_ZONE);
        if ((Calendar.FRIDAY == calendar.get(Calendar.DAY_OF_WEEK) &&
                calendar.get(Calendar.HOUR_OF_DAY) > 22 && calendar.get(Calendar.HOUR_OF_DAY) < 24)
                || (DAY_OF_WEEK == calendar.get(Calendar.DAY_OF_WEEK))) {

            calendar.set(Calendar.HOUR_OF_DAY, HOUR_OF_DAY);
            calendar.set(Calendar.MINUTE, MINUTE);
            calendar.set(Calendar.SECOND, SECOND);

            scheduler.execute(new StartAnnounceTask(calendar.getTimeInMillis()));

            calendar = Calendar.getInstance(TIME_ZONE);
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            calendar.set(Calendar.HOUR, 0);
        } else {
            calendar.add(Calendar.HOUR, 1);
        }
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        Announcements.getInstance().gameAnnounceToAll("Register to Championship Event .info");
        scheduler.schedule(this, initialDelay(calendar), TimeUnit.MILLISECONDS);

    }


    private long initialDelay(Calendar calendar) {
        long initialDelay = calendar.getTimeInMillis() - Calendar.getInstance(TIME_ZONE).getTimeInMillis();

        if (initialDelay < 0)
            initialDelay = Math.abs(initialDelay);

        return initialDelay;
    }

    private long delay() {
        Calendar calendar = Calendar.getInstance(TIME_ZONE);
        calendar.add(Calendar.HOUR, 1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return initialDelay(calendar);
    }

    private long mockDelay() {
        Calendar calendar = Calendar.getInstance(TIME_ZONE);
        calendar.set(Calendar.HOUR_OF_DAY, 14);
        calendar.set(Calendar.MINUTE, 39);
        calendar.set(Calendar.SECOND, 30);
        return initialDelay(calendar);
    }

    public static class LAZY_HOLDER {

        public static ChampionshipEventSchedule schedule() {
            if (INSTANCE == null)
                INSTANCE = new ChampionshipEventSchedule();
            return INSTANCE;
        }
    }
}
