package no.nav.dokdisteformidling.consumer.eformidling.dokumentpakker.sbdh;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


/**
 * This type is the manifest root element. The container of all the file meta-data.
 *
 *
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ExternalServiceCode" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="ExternalServiceEditionCode" type="{http://www.w3.org/2001/XMLSchema}integer"/&gt;
 *         &lt;element name="SendersReference" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="Reportee" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="SentDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/&gt;
 *         &lt;element name="FileList" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="File" maxOccurs="unbounded" minOccurs="0"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="FileName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                             &lt;element name="CheckSum" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="PropertyList" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="Property" maxOccurs="unbounded" minOccurs="0"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="PropertyKey" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                             &lt;element name="PropertyValue" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "externalServiceCode",
        "externalServiceEditionCode",
        "sendersReference",
        "reportee",
        "sentDate",
        "fileList",
        "propertyList"
})
@XmlRootElement(name = "BrokerServiceManifest")
public class BrokerServiceManifest {

    @XmlElement(name = "ExternalServiceCode", required = true)
    protected String externalServiceCode;
    @XmlElement(name = "ExternalServiceEditionCode", required = true)
    protected BigInteger externalServiceEditionCode;
    @XmlElement(name = "SendersReference", required = true)
    protected String sendersReference;
    @XmlElement(name = "Reportee", required = true)
    protected String reportee;
    @XmlElement(name = "SentDate")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar sentDate;
    @XmlElement(name = "FileList")
    protected BrokerServiceManifest.FileList fileList;
    @XmlElement(name = "PropertyList")
    protected BrokerServiceManifest.PropertyList propertyList;

    /**
     * Gets the value of the externalServiceCode property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getExternalServiceCode() {
        return externalServiceCode;
    }

    /**
     * Sets the value of the externalServiceCode property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setExternalServiceCode(String value) {
        this.externalServiceCode = value;
    }

    /**
     * Gets the value of the externalServiceEditionCode property.
     *
     * @return possible object is
     * {@link BigInteger }
     */
    public BigInteger getExternalServiceEditionCode() {
        return externalServiceEditionCode;
    }

    /**
     * Sets the value of the externalServiceEditionCode property.
     *
     * @param value allowed object is
     *              {@link BigInteger }
     */
    public void setExternalServiceEditionCode(BigInteger value) {
        this.externalServiceEditionCode = value;
    }

    /**
     * Gets the value of the sendersReference property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSendersReference() {
        return sendersReference;
    }

    /**
     * Sets the value of the sendersReference property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSendersReference(String value) {
        this.sendersReference = value;
    }

    /**
     * Gets the value of the reportee property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getReportee() {
        return reportee;
    }

    /**
     * Sets the value of the reportee property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setReportee(String value) {
        this.reportee = value;
    }

    /**
     * Gets the value of the sentDate property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getSentDate() {
        return sentDate;
    }

    /**
     * Sets the value of the sentDate property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setSentDate(XMLGregorianCalendar value) {
        this.sentDate = value;
    }

    /**
     * Gets the value of the fileList property.
     *
     * @return possible object is
     * {@link BrokerServiceManifest.FileList }
     */
    public BrokerServiceManifest.FileList getFileList() {
        return fileList;
    }

    /**
     * Sets the value of the fileList property.
     *
     * @param value allowed object is
     *              {@link BrokerServiceManifest.FileList }
     */
    public void setFileList(BrokerServiceManifest.FileList value) {
        this.fileList = value;
    }

    /**
     * Gets the value of the propertyList property.
     *
     * @return possible object is
     * {@link BrokerServiceManifest.PropertyList }
     */
    public BrokerServiceManifest.PropertyList getPropertyList() {
        return propertyList;
    }

    /**
     * Sets the value of the propertyList property.
     *
     * @param value allowed object is
     *              {@link BrokerServiceManifest.PropertyList }
     */
    public void setPropertyList(BrokerServiceManifest.PropertyList value) {
        this.propertyList = value;
    }


    /**
     * This property should hold a list of the files included in the shipment.
     * This is optional.
     *
     *
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="File" maxOccurs="unbounded" minOccurs="0"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="FileName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *                   &lt;element name="CheckSum" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "file"
    })
    public static class FileList {

        @XmlElement(name = "File")
        protected List<BrokerServiceManifest.FileList.File> file;

        /**
         * Gets the value of the file property.
         *
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the file property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getFile().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link BrokerServiceManifest.FileList.File }
         */
        public List<BrokerServiceManifest.FileList.File> getFile() {
            if (file == null) {
                file = new ArrayList<BrokerServiceManifest.FileList.File>();
            }
            return this.file;
        }


        /**
         * This property should hold information about a file in the
         * package.
         *
         *
         * <p>Java class for anonymous complex type.
         *
         * <p>The following schema fragment specifies the expected content contained within this class.
         *
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *       &lt;sequence&gt;
         *         &lt;element name="FileName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
         *         &lt;element name="CheckSum" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
         *       &lt;/sequence&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "fileName",
                "checkSum"
        })
        public static class File {

            @XmlElement(name = "FileName", required = true)
            protected String fileName;
            @XmlElement(name = "CheckSum")
            protected String checkSum;

            /**
             * Gets the value of the fileName property.
             *
             * @return possible object is
             * {@link String }
             */
            public String getFileName() {
                return fileName;
            }

            /**
             * Sets the value of the fileName property.
             *
             * @param value allowed object is
             *              {@link String }
             */
            public void setFileName(String value) {
                this.fileName = value;
            }

            /**
             * Gets the value of the checkSum property.
             *
             * @return possible object is
             * {@link String }
             */
            public String getCheckSum() {
                return checkSum;
            }

            /**
             * Sets the value of the checkSum property.
             *
             * @param value allowed object is
             *              {@link String }
             */
            public void setCheckSum(String value) {
                this.checkSum = value;
            }

        }

    }


    /**
     * This property can hold a list of custom values agreed upon between sender
     * and receivers. This is optional.
     *
     *
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="Property" maxOccurs="unbounded" minOccurs="0"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="PropertyKey" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *                   &lt;element name="PropertyValue" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "property"
    })
    public static class PropertyList {

        @XmlElement(name = "Property")
        protected List<BrokerServiceManifest.PropertyList.Property> property;

        /**
         * Gets the value of the property property.
         *
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the property property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getProperty().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link BrokerServiceManifest.PropertyList.Property }
         */
        public List<BrokerServiceManifest.PropertyList.Property> getProperty() {
            if (property == null) {
                property = new ArrayList<BrokerServiceManifest.PropertyList.Property>();
            }
            return this.property;
        }


        /**
         * <p>Java class for anonymous complex type.
         *
         * <p>The following schema fragment specifies the expected content contained within this class.
         *
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *       &lt;sequence&gt;
         *         &lt;element name="PropertyKey" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
         *         &lt;element name="PropertyValue" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
         *       &lt;/sequence&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "propertyKey",
                "propertyValue"
        })
        public static class Property {

            @XmlElement(name = "PropertyKey", required = true)
            protected String propertyKey;
            @XmlElement(name = "PropertyValue", required = true)
            protected String propertyValue;

            /**
             * Gets the value of the propertyKey property.
             *
             * @return possible object is
             * {@link String }
             */
            public String getPropertyKey() {
                return propertyKey;
            }

            /**
             * Sets the value of the propertyKey property.
             *
             * @param value allowed object is
             *              {@link String }
             */
            public void setPropertyKey(String value) {
                this.propertyKey = value;
            }

            /**
             * Gets the value of the propertyValue property.
             *
             * @return possible object is
             * {@link String }
             */
            public String getPropertyValue() {
                return propertyValue;
            }

            /**
             * Sets the value of the propertyValue property.
             *
             * @param value allowed object is
             *              {@link String }
             */
            public void setPropertyValue(String value) {
                this.propertyValue = value;
            }

        }

    }

}

