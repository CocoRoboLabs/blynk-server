package cc.blynk.server.db;

import cc.blynk.server.common.handlers.logic.timeline.TimelineDTO;
import cc.blynk.server.core.BlockingIOProcessor;
import cc.blynk.server.core.model.web.product.EventType;
import cc.blynk.server.db.dao.descriptor.LogEventDTO;
import cc.blynk.server.db.model.LogEvent;
import cc.blynk.server.db.model.LogEventCountKey;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 19.02.16.
 */
public class EventLogDBTest {

    private static ReportingDBManager reportingDBManager;
    private static BlockingIOProcessor blockingIOProcessor;

    @BeforeClass
    public static void init() throws Exception {
        blockingIOProcessor = new BlockingIOProcessor(4, 10000);
        reportingDBManager = new ReportingDBManager("db-test.properties", blockingIOProcessor);
        assertNotNull(reportingDBManager.getConnection());
    }

    @AfterClass
    public static void close() {
        reportingDBManager.close();
    }

    @Before
    public void cleanAll() throws Exception {
        //clean everything just in case
        reportingDBManager.executeSQL("DELETE FROM reporting_events");
        reportingDBManager.executeSQL("DELETE FROM reporting_events_last_seen");
    }

    @Test
    public void upsertLastSeen() throws Exception {
        reportingDBManager.eventDBDao.insertLastSeen(1, "test@blynk.cc");

        long ts = 0;
        try (Connection connection = reportingDBManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("select * from reporting_events_last_seen")) {

            while (rs.next()) {
                assertEquals(1, rs.getInt("device_id"));
                assertEquals("test@blynk.cc", rs.getString("email"));
                ts = rs.getTimestamp("ts").getTime();
                assertEquals(System.currentTimeMillis(), ts, 10000);
            }

            connection.commit();
        }

        reportingDBManager.eventDBDao.insertLastSeen(1, "test@blynk.cc");
        try (Connection connection = reportingDBManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("select * from reporting_events_last_seen")) {

            while (rs.next()) {
                assertEquals(1, rs.getInt("device_id"));
                assertEquals("test@blynk.cc", rs.getString("email"));
                long newTs = rs.getTimestamp("ts").getTime();
                assertEquals(System.currentTimeMillis(), newTs, 10000);
                assertNotEquals(ts, newTs);
            }

            connection.commit();
        }
    }

    @Test
    public void insert100RowEvents() throws Exception {
        long now = System.currentTimeMillis();
        String eventCode = "something";

        for (int i = 0; i < 100; i++) {
            LogEvent logEvent = new LogEvent(1, EventType.INFORMATION, now, eventCode.hashCode(), null);
            reportingDBManager.eventDBDao.insert(logEvent);
        }

        try (Connection connection = reportingDBManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("select count(*) from reporting_events")) {

            while (rs.next()) {
                assertEquals(100, rs.getInt(1));
            }
        }
    }

    @Test
    public void selectBasicQueryForSingleEntryThatIsResolvedWithAnotherMethod() throws Exception {
        long now = System.currentTimeMillis();
        String eventCode = "something";

        LogEvent logEvent = new LogEvent(1, 1, EventType.INFORMATION, now, eventCode.hashCode(), null, true, "Pupkin Vasya", 0, null);
        reportingDBManager.eventDBDao.insert(logEvent);

        TimelineDTO timelineDTO = new TimelineDTO(1, EventType.INFORMATION, true, now, now, 0, 1);
        List<LogEventDTO> logEvents = reportingDBManager.eventDBDao.getEvents(timelineDTO);
        assertNotNull(logEvents);
        assertEquals(1, logEvents.size());

        timelineDTO = new TimelineDTO(1, EventType.INFORMATION, false, now, now, 0, 1);

        logEvents = reportingDBManager.eventDBDao.getEvents(timelineDTO);
        assertNotNull(logEvents);
        assertEquals(0, logEvents.size());
    }

    @Test
    public void selectBasicQueryFor100EntriesWithAnotherMethod() throws Exception {
        long now = System.currentTimeMillis();
        String eventCode = "something";


        for (int i = 0; i < 100; i++) {
            LogEvent logEvent = new LogEvent(1, EventType.INFORMATION, now++, eventCode.hashCode(), null);
            reportingDBManager.eventDBDao.insert(logEvent);
        }

        for (int i = 0; i < 10; i += 10) {
            List<LogEventDTO> logEvents = reportingDBManager.eventDBDao.getEvents(
                    new TimelineDTO(1, EventType.INFORMATION, null, now - 100, now, i * 10, 10));
            assertNotNull(logEvents);
            assertEquals(10, logEvents.size());
            for (LogEventDTO logEventDTO : logEvents) {
                assertEquals(--now, logEventDTO.ts);

                assertEquals(1, logEventDTO.deviceId);
                assertEquals(EventType.INFORMATION, logEventDTO.eventType);
                assertEquals(eventCode.hashCode(), logEventDTO.eventHashcode);
                assertNull(logEventDTO.description);
                assertFalse(logEventDTO.isResolved);
            }
        }
    }

    @Test
    public void selectBasicQueryWithNullIsResolved() throws Exception {
        long now = System.currentTimeMillis();
        String eventCode = "something";

        LogEvent logEvent = new LogEvent(1, 1, EventType.INFORMATION, now, eventCode.hashCode(), null, true, "Pupkin Vasya", 0, null);
        reportingDBManager.eventDBDao.insert(logEvent);

        TimelineDTO timelineDTO = new TimelineDTO(1, EventType.INFORMATION, null, now, now, 0, 1);
        List<LogEventDTO> logEvents = reportingDBManager.eventDBDao.getEvents(timelineDTO);
        assertNotNull(logEvents);
        assertEquals(1, logEvents.size());

        timelineDTO = new TimelineDTO(1, EventType.INFORMATION, true, now, now, 0, 1);

        logEvents = reportingDBManager.eventDBDao.getEvents(timelineDTO);
        assertNotNull(logEvents);
        assertEquals(1, logEvents.size());

        timelineDTO = new TimelineDTO(1, EventType.INFORMATION, false, now, now, 0, 1);

        logEvents = reportingDBManager.eventDBDao.getEvents(timelineDTO);
        assertNotNull(logEvents);
        assertEquals(0, logEvents.size());
    }

    @Test
    public void selectBasicQueryWithNullEventType() throws Exception {
        long now = System.currentTimeMillis();
        String eventCode = "something";

        LogEvent logEvent = new LogEvent(1, 1, EventType.INFORMATION, now, eventCode.hashCode(), null, true, "Pupkin Vasya", 0, null);
        reportingDBManager.eventDBDao.insert(logEvent);

        TimelineDTO timelineDTO = new TimelineDTO(1, null, true, now, now, 0, 1);
        List<LogEventDTO> logEvents = reportingDBManager.eventDBDao.getEvents(timelineDTO);
        assertNotNull(logEvents);
        assertEquals(1, logEvents.size());

        timelineDTO = new TimelineDTO(1, EventType.INFORMATION, true, now, now, 0, 1);

        logEvents = reportingDBManager.eventDBDao.getEvents(timelineDTO);
        assertNotNull(logEvents);
        assertEquals(1, logEvents.size());

        timelineDTO = new TimelineDTO(1, EventType.WARNING, true, now, now, 0, 1);

        logEvents = reportingDBManager.eventDBDao.getEvents(timelineDTO);
        assertNotNull(logEvents);
        assertEquals(0, logEvents.size());
    }

    @Test
    public void selectBasicQueryWithNullEventTypeAndIsResolved() throws Exception {
        long now = System.currentTimeMillis();
        String eventCode = "something";

        LogEvent logEvent = new LogEvent(1, 1, EventType.INFORMATION, now, eventCode.hashCode(), null, true, "Pupkin Vasya", 0, null);
        reportingDBManager.eventDBDao.insert(logEvent);

        TimelineDTO timelineDTO = new TimelineDTO(1, null, null, now, now, 0, 1);
        List<LogEventDTO> logEvents = reportingDBManager.eventDBDao.getEvents(timelineDTO);
        assertNotNull(logEvents);
        assertEquals(1, logEvents.size());

        timelineDTO = new TimelineDTO(1, null, null, now, now, 0, 1);

        logEvents = reportingDBManager.eventDBDao.getEvents(timelineDTO);
        assertNotNull(logEvents);
        assertEquals(1, logEvents.size());

        timelineDTO = new TimelineDTO(1, EventType.WARNING, false, now, now, 0, 1);

        logEvents = reportingDBManager.eventDBDao.getEvents(timelineDTO);
        assertNotNull(logEvents);
        assertEquals(0, logEvents.size());
    }


    @Test
    public void selectEventsSinceLastView() throws Exception {
        long now = System.currentTimeMillis();
        String eventCode = "something";
        LogEvent logEvent;

        logEvent = new LogEvent(1, EventType.INFORMATION, now, eventCode.hashCode(), null);
        reportingDBManager.eventDBDao.insert(logEvent);
        logEvent = new LogEvent(1, EventType.INFORMATION, now + 1, eventCode.hashCode(), null);
        reportingDBManager.eventDBDao.insert(logEvent);
        logEvent = new LogEvent(2, EventType.INFORMATION, now + 2, eventCode.hashCode(), null);
        reportingDBManager.eventDBDao.insert(logEvent);

        Map<LogEventCountKey, Integer> lastViewEvents = reportingDBManager.eventDBDao.getEventsSinceLastView("pupkin@blynk.cc");
        assertEquals(2, lastViewEvents.size());

        Integer lastView = lastViewEvents.get(new LogEventCountKey(1, EventType.INFORMATION, false));
        assertEquals(2, lastView.intValue());

        lastView = lastViewEvents.get(new LogEventCountKey(2, EventType.INFORMATION, false));
        assertEquals(1, lastView.intValue());
    }

    @Test
    public void selectEventsSinceLastViewAndViewRecords() throws Exception {
        long now = System.currentTimeMillis();
        String eventCode = "something";
        LogEvent logEvent;

        logEvent = new LogEvent(1, EventType.INFORMATION, now, eventCode.hashCode(), null);
        reportingDBManager.eventDBDao.insert(logEvent);

        Map<LogEventCountKey, Integer> lastViewEvents = reportingDBManager.eventDBDao.getEventsSinceLastView("pupkin@blynk.cc");
        assertEquals(1, lastViewEvents.size());

        Integer lastView = lastViewEvents.get(new LogEventCountKey(1, EventType.INFORMATION, false));
        assertEquals(1, lastView.intValue());

        reportingDBManager.eventDBDao.insertLastSeen(1, "test@blynk.cc");

        lastViewEvents = reportingDBManager.eventDBDao.getEventsSinceLastView("pupkin@blynk.cc");
        assertEquals(1, lastViewEvents.size());

        reportingDBManager.eventDBDao.insertLastSeen(1, "pupkin@blynk.cc");

        //0 because last view is later than event itself
        lastViewEvents = reportingDBManager.eventDBDao.getEventsSinceLastView("pupkin@blynk.cc");
        assertEquals(0, lastViewEvents.size());

        //new event comes after "last view", so expecting it
        logEvent = new LogEvent(1, EventType.INFORMATION, System.currentTimeMillis(), eventCode.hashCode(), null);
        reportingDBManager.eventDBDao.insert(logEvent);

        lastViewEvents = reportingDBManager.eventDBDao.getEventsSinceLastView("pupkin@blynk.cc");
        assertEquals(1, lastViewEvents.size());
    }


}
