package cc.blynk.server.core.model.web.product.metafields;

import cc.blynk.server.core.model.web.product.MetaField;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.LocalTime;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 04.04.17.
 */
public class RangeTimeMetaField extends MetaField {

    @JsonSerialize(using = LocalTimeToIntSerializer.class)
    @JsonDeserialize(using = IntToLocalTimeSerializer.class)
    public final LocalTime from;

    @JsonSerialize(using = LocalTimeToIntSerializer.class)
    @JsonDeserialize(using = IntToLocalTimeSerializer.class)
    public final LocalTime to;

    @JsonCreator
    public RangeTimeMetaField(@JsonProperty("id") int id,
                              @JsonProperty("name") String name,
                              @JsonProperty("roleIds") int[] roleIds,
                              @JsonProperty("includeInProvision") boolean includeInProvision,
                              @JsonProperty("isMandatory") boolean isMandatory,
                              @JsonProperty("isDefault") boolean isDefault,
                              @JsonProperty("icon") String icon,
                              @JsonProperty("from") LocalTime from,
                              @JsonProperty("to") LocalTime to) {
        super(id, name, roleIds, includeInProvision, isMandatory, isDefault, icon);
        this.from = from;
        this.to = to;
    }

    @Override
    public MetaField copySpecificFieldsOnly(MetaField metaField) {
        return new RangeTimeMetaField(
                id, metaField.name, metaField.roleIds,
                metaField.includeInProvision, metaField.isMandatory, metaField.isDefault,
                metaField.icon, from, to);
    }

    @Override
    public MetaField copy() {
        return new RangeTimeMetaField(id, name, roleIds,
                includeInProvision, isMandatory, isDefault,
                icon, from, to);
    }
}
