package cc.blynk.server.core.model.web.product.metafields;

import cc.blynk.server.core.model.web.Role;
import cc.blynk.server.core.model.web.product.MetaField;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 04.04.17.
 */
public class ContactMetaField extends MetaField {

    public final String contact;

    public final String firstName;

    public volatile boolean isFirstNameEnabled;

    public final String lastName;

    public volatile boolean isLastNameEnabled;

    public final String email;

    public volatile boolean isEmailEnabled;

    public final String phone;

    public volatile boolean isPhoneEnabled;

    public final String streetAddress;

    public volatile boolean isStreetAddressEnabled;

    public final String country;

    public volatile boolean isCountryEnabled;

    public final String city;

    public volatile boolean isCityEnabled;

    public final String state;

    public volatile boolean isStateEnabled;

    public final String zip;

    public volatile boolean isZipEnabled;

    public volatile boolean isDefaultsEnabled;

    @JsonCreator
    public ContactMetaField(@JsonProperty("id") int id,
                            @JsonProperty("name") String name,
                            @JsonProperty("role") Role role,
                            @JsonProperty("isDefault") boolean isDefault,
                            @JsonProperty("contact") String contact,
                            @JsonProperty("firstName") String firstName,
                            @JsonProperty("isFirstNameEnabled") boolean isFirstNameEnabled,
                            @JsonProperty("lastName") String lastName,
                            @JsonProperty("isLastNameEnabled") boolean isLastNameEnabled,
                            @JsonProperty("email") String email,
                            @JsonProperty("isEmailEnabled") boolean isEmailEnabled,
                            @JsonProperty("phone") String phone,
                            @JsonProperty("isPhoneEnabled") boolean isPhoneEnabled,
                            @JsonProperty("streetAddress") String streetAddress,
                            @JsonProperty("isStreetAddressEnabled") boolean isStreetAddressEnabled,
                            @JsonProperty("country") String country,
                            @JsonProperty("isCountryEnabled") boolean isCountryEnabled,
                            @JsonProperty("city") String city,
                            @JsonProperty("isCityEnabled") boolean isCityEnabled,
                            @JsonProperty("state") String state,
                            @JsonProperty("isStateEnabled") boolean isStateEnabled,
                            @JsonProperty("zip") String zip,
                            @JsonProperty("isZipEnabled") boolean isZipEnabled,
                            @JsonProperty("isDefaultsEnabled") boolean isDefaultsEnabled) {
        super(id, name, role, isDefault);
        this.contact = contact;
        this.firstName = firstName;
        this.isFirstNameEnabled = isFirstNameEnabled;
        this.lastName = lastName;
        this.isLastNameEnabled = isLastNameEnabled;
        this.email = email;
        this.isEmailEnabled = isEmailEnabled;
        this.phone = phone;
        this.isPhoneEnabled = isPhoneEnabled;
        this.streetAddress = streetAddress;
        this.isStreetAddressEnabled = isStreetAddressEnabled;
        this.country = country;
        this.isCountryEnabled = isCountryEnabled;
        this.city = city;
        this.isCityEnabled = isCityEnabled;
        this.state = state;
        this.isStateEnabled = isStateEnabled;
        this.zip = zip;
        this.isZipEnabled = isZipEnabled;
        this.isDefaultsEnabled = isDefaultsEnabled;
    }

    @Override
    public void update(MetaField metaField) {
        super.update(metaField);
        ContactMetaField contactMetaField = (ContactMetaField) metaField;
        this.isFirstNameEnabled = contactMetaField.isFirstNameEnabled;
        this.isLastNameEnabled = contactMetaField.isLastNameEnabled;
        this.isEmailEnabled = contactMetaField.isEmailEnabled;
        this.isPhoneEnabled = contactMetaField.isPhoneEnabled;
        this.isStreetAddressEnabled = contactMetaField.isStreetAddressEnabled;
        this.isCountryEnabled = contactMetaField.isCountryEnabled;
        this.isCityEnabled = contactMetaField.isCityEnabled;
        this.isStateEnabled = contactMetaField.isStateEnabled;
        this.isZipEnabled = contactMetaField.isZipEnabled;
        this.isDefaultsEnabled = contactMetaField.isDefaultsEnabled;
    }

    @Override
    public MetaField copy() {
        return new ContactMetaField(id, name, role, isDefault,
                contact,
                firstName, isFirstNameEnabled,
                lastName, isLastNameEnabled,
                email, isEmailEnabled,
                phone, isPhoneEnabled,
                streetAddress, isStreetAddressEnabled,
                country, isCountryEnabled,
                city, isCityEnabled,
                state, isStateEnabled,
                zip, isZipEnabled,
                isDefaultsEnabled);
    }
}
