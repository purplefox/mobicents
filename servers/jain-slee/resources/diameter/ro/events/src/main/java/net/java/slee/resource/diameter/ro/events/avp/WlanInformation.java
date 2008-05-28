package net.java.slee.resource.diameter.ro.events.avp;
/**
 * Defines an interface representing the WLAN-Information grouped AVP type. From the Diameter Ro Reference Point Protocol Details (3GPP TS 32.299 V7.1.0) specification: 7.2.121 WLAN-Information AVP The WLAN-Information AVP (AVP code 875) is of type Grouped. Its purpose is to allow the transmission of additional WLAN service specific information elements. The format and the contents of the fields inside the WLAN- Information AVP is specified in TS 32.252 [22]. It has the following ABNF grammar: WLAN-Information ::= AVP Header: 875 [ WLAN-Session-Id ] [ PDG-Address ] [ PDG-Charging-Id ] [ WAG-Address ] [ WAG-PLMN-Id ] [ WLAN-Radio-Container ] [ WLAN-UE-Local-IPAddress ]
 */
public interface WlanInformation extends net.java.slee.resource.diameter.base.events.avp.GroupedAvp{
    /**
     * Returns the value of the PDG-Address AVP, of type Address. A return value of null implies that the AVP has not been set.
     */
    abstract net.java.slee.resource.diameter.base.events.avp.AddressAvp getPdgAddress();

    /**
     * Returns the value of the PDG-Charging-Id AVP, of type Unsigned32. A return value of null implies that the AVP has not been set.
     */
    abstract long getPdgChargingId();

    /**
     * Returns the value of the WAG-Address AVP, of type Address. A return value of null implies that the AVP has not been set.
     */
    abstract net.java.slee.resource.diameter.base.events.avp.AddressAvp getWagAddress();

    /**
     * Returns the value of the WAG-PLMN-Id AVP, of type OctetString. A return value of null implies that the AVP has not been set.
     */
    abstract byte[] getWagPlmnId();

    /**
     * Returns the value of the WLAN-Radio-Container AVP, of type Grouped. A return value of null implies that the AVP has not been set.
     */
    abstract net.java.slee.resource.diameter.ro.events.avp.WlanRadioContainer getWlanRadioContainer();

    /**
     * Returns the value of the WLAN-Session-Id AVP, of type UTF8String. A return value of null implies that the AVP has not been set.
     */
    abstract java.lang.String getWlanSessionId();

    /**
     * Returns the value of the WLAN-UE-Local-IPAddress AVP, of type Address. A return value of null implies that the AVP has not been set.
     */
    abstract net.java.slee.resource.diameter.base.events.avp.AddressAvp getWlanUeLocalIpaddress();

    /**
     * Returns true if the PDG-Address AVP is present in the message.
     */
    abstract boolean hasPdgAddress();

    /**
     * Returns true if the PDG-Charging-Id AVP is present in the message.
     */
    abstract boolean hasPdgChargingId();

    /**
     * Returns true if the WAG-Address AVP is present in the message.
     */
    abstract boolean hasWagAddress();

    /**
     * Returns true if the WAG-PLMN-Id AVP is present in the message.
     */
    abstract boolean hasWagPlmnId();

    /**
     * Returns true if the WLAN-Radio-Container AVP is present in the message.
     */
    abstract boolean hasWlanRadioContainer();

    /**
     * Returns true if the WLAN-Session-Id AVP is present in the message.
     */
    abstract boolean hasWlanSessionId();

    /**
     * Returns true if the WLAN-UE-Local-IPAddress AVP is present in the message.
     */
    abstract boolean hasWlanUeLocalIpaddress();

    /**
     * Sets the value of the PDG-Address AVP, of type Address.
     */
    abstract void setPdgAddress(net.java.slee.resource.diameter.base.events.avp.AddressAvp pdgAddress);

    /**
     * Sets the value of the PDG-Charging-Id AVP, of type Unsigned32.
     */
    abstract void setPdgChargingId(long pdgChargingId);

    /**
     * Sets the value of the WAG-Address AVP, of type Address.
     */
    abstract void setWagAddress(net.java.slee.resource.diameter.base.events.avp.AddressAvp wagAddress);

    /**
     * Sets the value of the WAG-PLMN-Id AVP, of type OctetString.
     */
    abstract void setWagPlmnId(byte[] wagPlmnId);

    /**
     * Sets the value of the WLAN-Radio-Container AVP, of type Grouped.
     */
    abstract void setWlanRadioContainer(net.java.slee.resource.diameter.ro.events.avp.WlanRadioContainer wlanRadioContainer);

    /**
     * Sets the value of the WLAN-Session-Id AVP, of type UTF8String.
     */
    abstract void setWlanSessionId(java.lang.String wlanSessionId);

    /**
     * Sets the value of the WLAN-UE-Local-IPAddress AVP, of type Address.
     */
    abstract void setWlanUeLocalIpaddress(net.java.slee.resource.diameter.base.events.avp.AddressAvp wlanUeLocalIpaddress);

}
