package cc.blynk.server.core.model.widgets.ui.tiles;

import cc.blynk.server.core.model.DataStream;
import cc.blynk.server.core.model.device.Device;
import cc.blynk.server.core.model.enums.PinMode;
import cc.blynk.server.core.model.enums.PinType;
import cc.blynk.server.core.model.enums.WidgetProperty;
import cc.blynk.server.core.model.widgets.DeviceCleaner;
import cc.blynk.server.core.model.widgets.MobileSyncWidget;
import cc.blynk.server.core.model.widgets.Widget;
import cc.blynk.server.core.model.widgets.outputs.TextAlignment;
import cc.blynk.server.core.protocol.exceptions.IllegalCommandException;
import cc.blynk.utils.ArrayUtil;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cc.blynk.server.core.protocol.enums.Command.DEVICE_SYNC;
import static cc.blynk.server.internal.CommonByteBufUtil.makeUTF8StringMessage;
import static cc.blynk.utils.IntArray.EMPTY_INTS;
import static cc.blynk.utils.StringUtils.prependDeviceId;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 02.10.17.
 */
public class DeviceTiles extends Widget implements MobileSyncWidget, DeviceCleaner {

    private static final Tile[] EMPTY_DEVICE_TILES = {};
    private static final TileTemplate[] EMPTY_TEMPLATES = {};

    public volatile TileTemplate[] templates = EMPTY_TEMPLATES;

    public volatile Tile[] tiles = EMPTY_DEVICE_TILES;

    public int rows;

    public int columns;

    public volatile SortType sortType;

    public TextAlignment alignment = TextAlignment.LEFT;

    public boolean disableWhenOffline;

    public boolean stretchToBottom;

    public void deleteDeviceTilesByTemplateId(long deviceTileId) {
        ArrayList<Tile> list = new ArrayList<>();
        for (Tile tile : tiles) {
            if (tile.templateId != deviceTileId) {
                list.add(tile);
            }
        }
        tiles = list.toArray(new Tile[0]);
    }

    public void recreateTilesIfNecessary(TileTemplate newTileTemplate, TileTemplate existingTileTemplate) {
        //no changes. do nothing.
        if (existingTileTemplate != null
                && Arrays.equals(newTileTemplate.deviceIds, existingTileTemplate.deviceIds)
                && newTileTemplate.dataStream != null
                && newTileTemplate.dataStream.equals(existingTileTemplate.dataStream)) {
            return;
        }

        Tile[] existingTiles = this.tiles;

        ArrayList<Tile> list = new ArrayList<>();
        for (TileTemplate tileTemplate : this.templates) {
            //creating new device tiles for updated TileTemplate
            if (tileTemplate.id == newTileTemplate.id) {
                for (int deviceId : newTileTemplate.deviceIds) {
                    Tile newTile = new Tile(deviceId, tileTemplate.id, null,
                            newTileTemplate.dataStream == null
                                    ? null
                                    : new DataStream(newTileTemplate.dataStream)
                    );
                    preserveOldValueIfPossible(existingTiles, newTile);
                    list.add(newTile);
                }
                //leaving untouched device tiles that are not updated
            } else {
                for (Tile tile : existingTiles) {
                    if (tile.templateId == tileTemplate.id) {
                        list.add(tile);
                    }
                }
            }
        }
        this.tiles = list.toArray(new Tile[0]);
    }

    private void preserveOldValueIfPossible(Tile[] existingTiles, Tile newTile) {
        for (Tile existingTile : existingTiles) {
            if (existingTile.templateId == newTile.templateId
                    && newTile.updateIfSame(existingTile.deviceId, existingTile.dataStream)) {
                return;
            }
        }
    }

    public TileTemplate getTileTemplateByIdOrThrow(long id) {
        return templates[getTileTemplateIndexByIdOrThrow(id)];
    }

    public TileTemplate getTileTemplateById(long id) {
        for (TileTemplate tileTemplate : templates) {
            if (tileTemplate.id == id) {
                return tileTemplate;
            }
        }
        return null;
    }

    public int getTileTemplateIndexByIdOrThrow(long id) {
        for (int i = 0; i < templates.length; i++) {
            if (templates[i].id == id) {
                return i;
            }
        }
        throw new IllegalCommandException("Tile template with passed id not found.");
    }

    public Widget getWidgetById(long widgetId) {
        for (TileTemplate tileTemplate : templates) {
            for (Widget widget : tileTemplate.widgets) {
                if (widget.id == widgetId) {
                    return widget;
                }
            }
        }
        return null;
    }

    public TileTemplate getTileTemplateByWidgetIdOrThrow(long widgetId) {
        for (TileTemplate tileTemplate : templates) {
            for (Widget tileTemplateWidget : tileTemplate.widgets) {
                if (tileTemplateWidget.id == widgetId) {
                    return tileTemplate;
                }
            }
        }
        throw new IllegalCommandException("Widget template not found for passed widget id.");
    }

    @Override
    public boolean isSame(int deviceId, short pin, PinType pinType) {
        for (Tile tile : tiles) {
            if (tile.isSame(deviceId, pin, pinType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean updateIfSame(int deviceId, short pin, PinType pinType, String value) {
        for (Tile tile : tiles) {
            if (tile.updateIfSame(deviceId, pin, pinType, value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void sendAppSync(Channel appChannel, int targetId) {
        for (Tile tile : tiles) {
            if ((targetId == ANY_TARGET || tile.deviceId == targetId)
                    && tile.isValidDataStream() && tile.dataStream.isNotEmpty()) {
                String hardBody = tile.dataStream.makeHardwareBody();
                String body = prependDeviceId(tile.deviceId, hardBody);
                appChannel.write(makeUTF8StringMessage(DEVICE_SYNC, SYNC_DEFAULT_MESSAGE_ID, body));
            }
        }
    }

    @Override
    public PinMode getModeType() {
        return null;
    }

    @Override
    public int getPrice() {
        int sum = 1700; //price for DeviceTiles widget itself
        for (TileTemplate template : templates) {
            sum += template.getPrice();
        }
        return sum;
    }

    @Override
    public void updateValue(Widget oldWidget) {
        if (oldWidget instanceof DeviceTiles) {
            DeviceTiles oldDeviceTiles = (DeviceTiles) oldWidget;
            this.tiles = oldDeviceTiles.tiles;
            for (TileTemplate tileTemplate : templates) {
                TileTemplate oldTileTemplate = oldDeviceTiles.getTileTemplateById(tileTemplate.id);
                if (oldTileTemplate != null) {
                    tileTemplate.deviceIds = oldTileTemplate.deviceIds;
                }
            }
        }
    }

    @Override
    public void erase() {
        //for export apps tiles are fully removed
        //tiles will be created during provisioning.
        tiles = EMPTY_DEVICE_TILES;
        if (templates != null) {
            for (TileTemplate tileTemplate : templates) {
                tileTemplate.erase();
            }
        }
    }

    public String getValue(int deviceId, short pin, PinType pinType) {
        for (Tile tile : tiles) {
            if (tile.isSame(deviceId, pin, pinType)) {
                return tile.dataStream.value;
            }
        }
        return null;
    }

    @Override
    public boolean isAssignedToDevice(int deviceId) {
        return false;
    }

    private static int getTileIndexByDeviceId(Tile[] tiles, int deviceId) {
        for (int i = 0; i < tiles.length; i++) {
            if (tiles[i].deviceId == deviceId) {
                return i;
            }
        }
        return -1;
    }

    public TileTemplate getTileTemplateByTemplateId(String templateId) {
        for (TileTemplate tileTemplate : templates) {
            if (templateId.equals(tileTemplate.templateId)) {
                return tileTemplate;
            }
        }
        return null;
    }

    public void createTiles(List<Device> devices) {
        var tiles = new ArrayList<Tile>(devices.size());
        for (Device device : devices) {
            String templateId = device.getTemplateId();
            if (templateId != null) {
                TileTemplate tileTemplate = getTileTemplateByTemplateId(templateId);
                if (tileTemplate != null) {
                    int deviceId = device.id;
                    tiles.add(new Tile(deviceId, tileTemplate));
                    tileTemplate.addDeviceId(deviceId);
                }
            }
        }
        this.tiles = tiles.toArray(new Tile[0]);
    }

    public void recreateTiles(List<Device> devices) {
        tiles = EMPTY_DEVICE_TILES;
        if (templates != null) {
            for (TileTemplate tileTemplate : templates) {
                tileTemplate.deviceIds = EMPTY_INTS;
            }
        }
        createTiles(devices);
    }

    public void replaceTileTemplate(TileTemplate newTileTemplate, int existingTileTemplateIndex) {
        TileTemplate[] updatedTemplates = Arrays.copyOf(templates, templates.length);
        TileTemplate existingTileTemplate = templates[existingTileTemplateIndex];
        updatedTemplates[existingTileTemplateIndex] = newTileTemplate;
        //do not override widgets field, as we have separate commands for it.

        newTileTemplate.widgets = existingTileTemplate.widgets;
        this.templates = updatedTemplates;
    }

    @Override
    public void deleteDevice(int deviceId) {
        Tile[] localTiles = this.tiles;
        int index = getTileIndexByDeviceId(localTiles, deviceId);
        if (index != -1) {
            this.tiles = localTiles.length == 1 ? EMPTY_DEVICE_TILES : ArrayUtil.remove(localTiles, index, Tile.class);
        }

        for (TileTemplate tileTemplate : this.templates) {
            tileTemplate.deviceIds = ArrayUtil.deleteFromArray(tileTemplate.deviceIds, deviceId);
        }
    }

    @Override
    public boolean setProperty(WidgetProperty property, String propertyValue) {
        switch (property) {
            case SORT_TPYE:
                this.sortType = SortType.valueOf(propertyValue);
                return true;
            default:
                return super.setProperty(property, propertyValue);
        }
    }
}
