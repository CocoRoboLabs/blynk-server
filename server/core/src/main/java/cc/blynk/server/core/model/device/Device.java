package cc.blynk.server.core.model.device;

import cc.blynk.server.core.model.device.ota.DeviceOtaInfo;
import cc.blynk.server.core.model.device.ota.OTAStatus;
import cc.blynk.server.core.model.enums.PinType;
import cc.blynk.server.core.model.serialization.JsonParser;
import cc.blynk.server.core.model.web.product.MetaField;
import cc.blynk.server.core.model.web.product.WebDashboard;
import cc.blynk.server.core.model.widgets.Target;

import java.util.Arrays;
import java.util.List;

import static cc.blynk.server.internal.EmptyArraysUtil.EMPTY_META_FIELDS;
import static cc.blynk.utils.ArrayUtil.arrayToList;
import static cc.blynk.utils.ArrayUtil.concat;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 16.11.16.
 */
public class Device implements Target {

    public int id;

    public volatile int productId = -1;

    public volatile String name;

    public volatile String boardType;

    public volatile String token;

    public volatile String vendor;

    public volatile ConnectionType connectionType;

    public volatile Status status = Status.OFFLINE;

    public final long createdAt;

    public volatile long activatedAt;

    public volatile String activatedBy;

    public volatile long disconnectTime;

    public volatile long connectTime;

    public volatile String lastLoggedIP;

    public volatile long dataReceivedAt;

    public volatile long metadataUpdatedAt;

    public volatile long updatedAt;

    public volatile String metadataUpdatedBy;

    public volatile MetaField[] metaFields = EMPTY_META_FIELDS;

    public volatile HardwareInfo hardwareInfo;

    public volatile DeviceOtaInfo deviceOtaInfo;

    public volatile String iconName;

    public volatile WebDashboard webDashboard = new WebDashboard();

    public boolean isNotValid() {
        return boardType == null || boardType.isEmpty() || boardType.length() > 50
                || (name != null && name.length() > 50);
    }

    public Device() {
        this.createdAt = System.currentTimeMillis();
    }

    public Device(String name, String boardType, String token, int productId, ConnectionType connectionType) {
        this();
        this.name = name;
        this.boardType = boardType;
        this.token = token;
        this.productId = productId;
        this.connectionType = connectionType;
    }

    public Device(int id, String name, String boardType) {
        this();
        this.id = id;
        this.name = name;
        this.boardType = boardType;
    }

    public MetaField findMetaFieldById(int id) {
        for (MetaField metaField : metaFields) {
            if (metaField.id == id) {
                return metaField;
            }
        }
        return null;
    }

    public int findMetaFieldIndex(int id) {
        for (int i = 0; i < metaFields.length; i++) {
            if (metaFields[i].id == id) {
                return i;
            }
        }
        return -1;
    }

    public void updateMetaFields(MetaField[] updatedMetaFields) {
        MetaField[] metaFieldsCopy = Arrays.copyOf(metaFields, metaFields.length);

        for (MetaField updatedMetaField : updatedMetaFields) {
            for (int i = 0; i < metaFieldsCopy.length; i++) {
                MetaField existingMetaField = metaFieldsCopy[i];
                if (existingMetaField.id == updatedMetaField.id) {
                    metaFieldsCopy[i] = existingMetaField.copySpecificFieldsOnly(updatedMetaField);
                    break;
                }
            }
        }

        this.metaFields = metaFieldsCopy;
    }

    public void addMetaFields(MetaField[] metaFields) {
        this.metaFields = concat(this.metaFields, metaFields);
    }

    public void deleteMetaFields(MetaField[] metaFields) {
        List<MetaField> updatedSet = arrayToList(this.metaFields);
        updatedSet.removeAll(arrayToList(metaFields));
        this.metaFields = updatedSet.toArray(new MetaField[0]);
    }

    @Override
    public int[] getDeviceIds() {
        return new int[] {id};
    }

    @Override
    public boolean isSelected(int deviceId) {
        return id == deviceId;
    }

    @Override
    public int[] getAssignedDeviceIds() {
        return new int[] {id};
    }

    @Override
    public int getDeviceId() {
        return id;
    }

    public void updateFromMobile(Device newDevice) {
        this.name = newDevice.name;
        this.vendor = newDevice.vendor;
        this.boardType = newDevice.boardType;
        this.connectionType = newDevice.connectionType;
        this.iconName = newDevice.iconName;
        //that's fine. leave this fields as it is. It cannot be update from app client.
        //this.hardwareInfo = newDevice.hardwareInfo;
        //this.deviceOtaInfo = newDevice.deviceOtaInfo;
        this.updatedAt = System.currentTimeMillis();
    }

    public void updateFromWeb(Device newDevice) {
        this.productId = newDevice.productId;
        this.metaFields = newDevice.metaFields;
        this.webDashboard = newDevice.webDashboard;
        updateFromMobile(newDevice);
    }

    public void updateWebDashboard(byte pin, PinType type, String value, long now) {
        webDashboard.update(id, pin, type, value);
        this.dataReceivedAt = now;
    }

    public void disconnected() {
        this.status = Status.OFFLINE;
        this.disconnectTime = System.currentTimeMillis();
    }

    public void connected() {
        this.status = Status.ONLINE;
        this.connectTime = System.currentTimeMillis();
    }

    public void erase() {
        this.token = null;
        this.disconnectTime = 0;
        this.connectTime = 0;
        this.lastLoggedIP = null;
        this.status = Status.OFFLINE;
        this.hardwareInfo = null;
        this.deviceOtaInfo = null;
        this.webDashboard = null;
        this.dataReceivedAt = 0;
        this.metadataUpdatedAt = 0;
        this.metadataUpdatedBy = null;
        this.updatedAt = 0;
    }

    //for single device update device always updated when ota is initiated.
    public void updateOTAInfo(String initiatedBy) {
        long now = System.currentTimeMillis();
        this.deviceOtaInfo = new DeviceOtaInfo(initiatedBy, now, now, OTAStatus.STARTED);
    }

    @Override
    public String toString() {
        return JsonParser.toJson(this);
    }
}
