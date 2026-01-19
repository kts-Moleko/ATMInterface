package com.calpay.core.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Value object representing a South African address.
 * 
 * <p><b>Why value object?</b> Addresses have no identity - two addresses
 * with the same fields are considered equal. They're immutable and can
 * be safely shared between entities.
 * 
 * <p><b>South African Context:</b>
 * <ul>
 *   <li>Provinces: 9 provinces (Gauteng, Western Cape, etc.)</li>
 *   <li>Postal codes: 4 digits (e.g., "2000" for Johannesburg CBD)</li>
 *   <li>Cities/Towns: Major cities like Johannesburg, Cape Town, Durban</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public class Address implements Serializable {
	private static final long serialVersionUID = 1L;

	private final String addressLine1;
	private final String addressLine2;
	private final String suburb;
	private final String city;
	private final String province;
	private final String postalCode;
	private final String country;

	/**
	 * Constructs an address (use Builder for clarity).
	 */
	private Address(Builder builder) {
	    this.addressLine1 = builder.addressLine1;
	    this.addressLine2 = builder.addressLine2;
	    this.suburb = builder.suburb;
	    this.city = Objects.requireNonNull(builder.city, "City cannot be null");
	    this.province = Objects.requireNonNull(builder.province, "Province cannot be null");
	    this.postalCode = Objects.requireNonNull(builder.postalCode, "Postal code cannot be null");
	    this.country = builder.country != null ? builder.country : "South Africa";
	    
	    validateProvince(this.province);
	    validatePostalCode(this.postalCode);
	}

	private void validateProvince(String province) {
	    String[] validProvinces = {
	        "Gauteng", "Western Cape", "KwaZulu-Natal", "Eastern Cape",
	        "Limpopo", "Mpumalanga", "North West", "Free State", "Northern Cape"
	    };
	    
	    for (String valid : validProvinces) {
	        if (valid.equalsIgnoreCase(province)) {
	            return;
	        }
	    }
	    
	    throw new IllegalArgumentException("Invalid SA province: " + province);
	}

	private void validatePostalCode(String postalCode) {
	    if (!postalCode.matches("\\d{4}")) {
	        throw new IllegalArgumentException(
	            "Invalid SA postal code. Expected 4 digits (e.g., 2000): " + postalCode
	        );
	    }
	}

	// Getters
	public String getAddressLine1() { return addressLine1; }
	public String getAddressLine2() { return addressLine2; }
	public String getSuburb() { return suburb; }
	public String getCity() { return city; }
	public String getProvince() { return province; }
	public String getPostalCode() { return postalCode; }
	public String getCountry() { return country; }

	/**
	 * Returns full formatted address.
	 * 
	 * <p><b>Example:</b>
	 * <pre>
	 * 123 Main Street
	 * Sandton
	 * Johannesburg, Gauteng, 2000
	 * South Africa
	 * </pre>
	 */
	public String getFullAddress() {
	    StringBuilder sb = new StringBuilder();
	    
	    if (addressLine1 != null) sb.append(addressLine1).append("\n");
	    if (addressLine2 != null) sb.append(addressLine2).append("\n");
	    if (suburb != null) sb.append(suburb).append("\n");
	    
	    sb.append(city).append(", ").append(province).append(", ").append(postalCode);
	    
	    if (country != null) sb.append("\n").append(country);
	    
	    return sb.toString();
	}

	/**
	 * Returns single-line address.
	 */
	public String getOneLine() {
	    return String.format("%s, %s, %s, %s",
	        addressLine1 != null ? addressLine1 : "",
	        city, province, postalCode
	    ).replaceAll(", ,", ",").trim();
	}

	// Builder
	public static class Builder {
	    private String addressLine1;
	    private String addressLine2;
	    private String suburb;
	    private String city;
	    private String province;
	    private String postalCode;
	    private String country = "South Africa";
	    
	    public Builder addressLine1(String addressLine1) {
	        this.addressLine1 = addressLine1;
	        return this;
	    }
	    
	    public Builder addressLine2(String addressLine2) {
	        this.addressLine2 = addressLine2;
	        return this;
	    }
	    
	    public Builder suburb(String suburb) {
	        this.suburb = suburb;
	        return this;
	    }
	    
	    public Builder city(String city) {
	        this.city = city;
	        return this;
	    }
	    
	    public Builder province(String province) {
	        this.province = province;
	        return this;
	    }
	    
	    public Builder postalCode(String postalCode) {
	        this.postalCode = postalCode;
	        return this;
	    }
	    
	    public Builder country(String country) {
	        this.country = country;
	        return this;
	    }
	    
	    public Address build() {
	        return new Address(this);
	    }
	}

	@Override
	public boolean equals(Object o) {
	    if (this == o) return true;
	    if (o == null || getClass() != o.getClass()) return false;
	    Address address = (Address) o;
	    return Objects.equals(addressLine1, address.addressLine1) &&
	           Objects.equals(addressLine2, address.addressLine2) &&
	           Objects.equals(city, address.city) &&
	           Objects.equals(province, address.province) &&
	           Objects.equals(postalCode, address.postalCode);
	}

	@Override
	public int hashCode() {
	    return Objects.hash(addressLine1, addressLine2, city, province, postalCode);
	}

	@Override
	public String toString() {
	    return getOneLine();
	}
	}
