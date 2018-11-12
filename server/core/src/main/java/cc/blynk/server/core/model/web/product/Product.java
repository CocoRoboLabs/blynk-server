package cc.blynk.server.core.model.web.product;

import cc.blynk.server.core.model.DataStream;
import cc.blynk.server.core.model.device.ConnectionType;
import cc.blynk.server.core.model.serialization.JsonParser;
import cc.blynk.server.core.model.web.product.events.Event;
import cc.blynk.server.core.model.web.product.metafields.TemplateIdMetaField;
import cc.blynk.server.core.protocol.exceptions.IllegalCommandBodyException;
import cc.blynk.utils.ArrayUtil;

import java.util.HashSet;
import java.util.Set;

import static cc.blynk.server.internal.EmptyArraysUtil.EMPTY_DATA_STREAMS;
import static cc.blynk.server.internal.EmptyArraysUtil.EMPTY_EVENTS;
import static cc.blynk.server.internal.EmptyArraysUtil.EMPTY_META_FIELDS;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 04.04.17.
 */
public class Product {

    public int id;

    public int parentId = -1;

    public volatile String name;

    public volatile String boardType;

    public volatile ConnectionType connectionType;

    public volatile String description;

    public volatile String logoUrl;

    public volatile long lastModifiedTs;

    public long createdAt;

    public volatile MetaField[] metaFields = EMPTY_META_FIELDS;

    public volatile DataStream[] dataStreams = EMPTY_DATA_STREAMS;

    public volatile Event[] events = EMPTY_EVENTS;

    public WebDashboard webDashboard = new WebDashboard();

    public volatile OtaProgress otaProgress;

    public int deviceCount;

    public volatile int version;

    public Product() {
        this.createdAt = System.currentTimeMillis();
        this.lastModifiedTs = createdAt;
    }

    public Product(Product product) {
        this();
        this.name = product.name;
        this.boardType = product.boardType;
        this.connectionType = product.connectionType;
        this.description = product.description;
        this.logoUrl = product.logoUrl;
        this.metaFields = product.copyMetaFields();
        this.dataStreams = product.copyDataStreams();
        this.events = product.copyEvents();
        this.webDashboard = product.webDashboard.copy();
    }

    @SuppressWarnings("NonAtomicOperationOnVolatileField")
    public void update(Product updatedProduct) {
        this.name = updatedProduct.name;
        this.boardType = updatedProduct.boardType;
        this.connectionType = updatedProduct.connectionType;
        this.description = updatedProduct.description;
        this.logoUrl = updatedProduct.logoUrl;
        this.metaFields = updatedProduct.metaFields;
        this.dataStreams = updatedProduct.dataStreams;
        this.events = updatedProduct.events;
        this.webDashboard = updatedProduct.webDashboard;
        this.lastModifiedTs = System.currentTimeMillis();
        this.version++;
    }

    public boolean isSubProduct() {
        return parentId > 0;
    }

    public boolean isValidEvents() {
        Set<Integer> set = new HashSet<>();
        for (Event event : events) {
            String eventCode = event.eventCode;
            set.add(eventCode == null ? 0 : event.eventCode.hashCode());
        }
        return set.size() == events.length;
    }

    public Event findEventByType(EventType eventType) {
        for (Event event : events) {
            if (event.getType() == eventType) {
                return event;
            }
        }
        return null;
    }

    public Event findEventByCode(int hashcode) {
        for (Event event : events) {
            if (event.isSame(hashcode)) {
                return event;
            }
        }
        return null;
    }

    public boolean containsTemplateId(String templateId) {
        for (MetaField metaField : metaFields) {
            if (metaField instanceof TemplateIdMetaField) {
                TemplateIdMetaField templateIdMetaField = (TemplateIdMetaField) metaField;
                if (templateIdMetaField.containsTemplate(templateId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void validate() {
        if (name == null || name.isEmpty()) {
            throw new IllegalCommandBodyException("Product name is empty.");
        }
        for (MetaField metaField : metaFields) {
            metaField.validate();
        }
    }

    public void setOtaProgress(OtaProgress otaProgress) {
        this.otaProgress = otaProgress;
        this.lastModifiedTs = System.currentTimeMillis();
    }

    public MetaField[] copyMetaFields() {
        return ArrayUtil.copy(metaFields, MetaField.class);
    }

    private DataStream[] copyDataStreams() {
        return ArrayUtil.copy(dataStreams, DataStream.class);
    }

    private Event[] copyEvents() {
        return ArrayUtil.copy(events, Event.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Product)) {
            return false;
        }

        Product product = (Product) o;

        return id == product.id;

    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return JsonParser.toJson(this);
    }
}
