package cc.blynk.server.core.model.web.product.metafields;

import cc.blynk.server.core.model.web.Role;
import cc.blynk.server.core.model.web.product.MetaField;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Currency;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 04.04.17.
 */
public class CostMetaField extends MetaField {

    public final Currency currency;

    public final double price;

    public final double perValue;

    public final MeasurementUnit units;

    @JsonCreator
    public CostMetaField(@JsonProperty("name") String name,
                         @JsonProperty("role") Role role,
                         @JsonProperty("currency") Currency currency,
                         @JsonProperty("price") double price,
                         @JsonProperty("perValue") double perValue,
                         @JsonProperty("units") MeasurementUnit units) {
        super(name, role);
        this.currency = currency;
        this.price = price;
        this.perValue = perValue;
        this.units = units;
    }

    @Override
    public MetaField copy() {
        return new CostMetaField(name, role, currency, price, perValue, units);
    }
}
