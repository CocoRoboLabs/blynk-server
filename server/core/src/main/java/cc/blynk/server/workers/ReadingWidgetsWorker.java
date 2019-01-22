package cc.blynk.server.workers;

import cc.blynk.server.core.dao.DeviceDao;
import cc.blynk.server.core.dao.SessionDao;
import cc.blynk.server.core.dao.UserDao;
import cc.blynk.server.core.model.DashBoard;
import cc.blynk.server.core.model.auth.Session;
import cc.blynk.server.core.model.auth.User;
import cc.blynk.server.core.model.device.Device;
import cc.blynk.server.core.model.profile.Profile;
import cc.blynk.server.core.model.widgets.FrequencyWidget;
import cc.blynk.server.core.model.widgets.Widget;
import cc.blynk.server.core.model.widgets.ui.tiles.DeviceTiles;
import cc.blynk.server.core.model.widgets.ui.tiles.Tile;
import cc.blynk.server.core.model.widgets.ui.tiles.TileTemplate;
import cc.blynk.server.core.session.HardwareStateHolder;
import cc.blynk.server.internal.StateHolderUtil;
import io.netty.channel.Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 02.02.17.
 */
public class ReadingWidgetsWorker implements Runnable {

    private static final Logger log = LogManager.getLogger(ReadingWidgetsWorker.class);

    private final SessionDao sessionDao;
    private final UserDao userDao;
    private final DeviceDao deviceDao;
    private final boolean allowRunWithoutApp;

    private int tickedWidgets = 0;
    private int counter = 0;
    private long totalTime = 0;

    public ReadingWidgetsWorker(SessionDao sessionDao,
                                UserDao userDao, DeviceDao deviceDao, boolean allowRunWithoutApp) {
        this.sessionDao = sessionDao;
        this.userDao = userDao;
        this.deviceDao = deviceDao;
        this.allowRunWithoutApp = allowRunWithoutApp;
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        try {
            process(now);
            totalTime += System.currentTimeMillis() - now;
        } catch (Exception e) {
            log.error("Error processing reading widgets. ", e);
        }

        counter++;
        if (counter == 60) {
            log.info("Ticked widgets for 1 minute : {}. Per second : {}, total time : {} ms",
                    tickedWidgets, tickedWidgets / 60, totalTime);
            tickedWidgets = 0;
            counter = 0;
            totalTime = 0;
        }
    }

    private void process(long now) {
        for (Map.Entry<Integer, Session> entry : sessionDao.orgSession.entrySet()) {
            Session session = entry.getValue();
            //for now checking widgets for active app only
            if ((allowRunWithoutApp || session.isAppConnected()) && session.isHardwareConnected()) {
                for (User user : userDao.users.values()) {
                    int orgId = entry.getKey();
                    if (user.orgId == orgId) {
                        Profile profile = user.profile;
                        for (DashBoard dashBoard : profile.dashBoards) {
                            if (dashBoard.isActive) {
                                for (Channel channel : session.hardwareChannels) {
                                    HardwareStateHolder stateHolder = StateHolderUtil.getHardState(channel);
                                    if (stateHolder != null) {
                                        int deviceId = stateHolder.device.id;
                                        for (Widget widget : dashBoard.widgets) {
                                            if (widget instanceof FrequencyWidget) {
                                                process(channel, (FrequencyWidget) widget, deviceId, now);
                                            } else if (widget instanceof DeviceTiles) {
                                                processDeviceTile(channel, (DeviceTiles) widget, deviceId, now);
                                            }
                                        }
                                        channel.flush();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void processDeviceTile(Channel channel,  DeviceTiles deviceTiles, int deviceId, long now) {
        for (Tile tile : deviceTiles.tiles) {
            if (tile.deviceId == deviceId && tile.isTicked(now)) {
                TileTemplate tileTemplate = deviceTiles.getTileTemplateById(tile.templateId);
                if (tileTemplate != null) {
                    for (Widget tileWidget : tileTemplate.widgets) {
                        if (tileWidget instanceof FrequencyWidget) {
                            FrequencyWidget frequencyWidget = (FrequencyWidget) tileWidget;
                            if (frequencyWidget.hasReadingInterval() && channel.isWritable()) {
                                frequencyWidget.writeReadingCommand(channel);
                                tickedWidgets++;
                            }
                        }
                    }
                }
            }
        }
    }

    private void process(Channel channel, FrequencyWidget frequencyWidget,
                         int deviceId, long now) {
        if (channel.isWritable()
                && sameDeviceId(frequencyWidget.getDeviceId(), deviceId)
                && frequencyWidget.isTicked(now)) {
            frequencyWidget.writeReadingCommand(channel);
            tickedWidgets++;
        }
    }

    private boolean sameDeviceId(int targetId, int channelDeviceId) {
        Device device = deviceDao.getById(targetId);
        return device != null && device.id == channelDeviceId;
    }

}
