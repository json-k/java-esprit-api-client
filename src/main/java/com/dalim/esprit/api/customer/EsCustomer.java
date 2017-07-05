package com.dalim.esprit.api.customer;

import java.util.List;

import org.keeber.esprit.EspritAPI.ApiRequest;

import com.dalim.esprit.api.EsObject;
import com.dalim.esprit.api.EsRef;
import com.dalim.esprit.api.EsStatus;
import com.google.gson.annotations.SerializedName;

public class EsCustomer extends EsObject {
  private String XMP, description, webSite, lastModificationUser, code, creationUser, mail, phone;
  private Boolean ntlm;
  private Integer defaultProjectTemplateID;
  private List<ESTemplate> projectTemplateList;
  @SerializedName("phone-2")
  private String phone2;
  /*
   * Billing address (FTLOGW)
   */
  @SerializedName("billing.address")
  private String billingAddress;
  @SerializedName("billing.city")
  private String billingCity;
  @SerializedName("billing.zipCode")
  private String billingZipcode;
  @SerializedName("billing.state")
  private String billingState;
  @SerializedName("billing.country")
  private String billingCountry;
  /*
   * Shipping address (FTLOGW)
   */
  @SerializedName("shipping.address")
  private String shippingAddress;
  @SerializedName("shipping.city")
  private String shippingCity;
  @SerializedName("shipping.zipCode")
  private String shippingZipcode;
  @SerializedName("shipping.state")
  private String shippingState;
  @SerializedName("shipping.country")
  private String shippingCountry;

  /*
   * Getters
   */

  public String getXMP() {
    return XMP;
  }

  public String getDescription() {
    return description;
  }

  public String getWebSite() {
    return webSite;
  }

  public String getLastModificationUser() {
    return lastModificationUser;
  }

  public String getCode() {
    return code;
  }

  public String getCreationUser() {
    return creationUser;
  }

  public String getMail() {
    return mail;
  }

  public String getPhone() {
    return phone;
  }

  public Boolean getNtlm() {
    return ntlm;
  }

  public Integer getDefaultProjectTemplateID() {
    return defaultProjectTemplateID;
  }

  public List<ESTemplate> getProjectTemplateList() {
    return projectTemplateList;
  }

  public String getPhone2() {
    return phone2;
  }

  public String getBillingAddress() {
    return billingAddress;
  }

  public String getBillingCity() {
    return billingCity;
  }

  public String getBillingZipcode() {
    return billingZipcode;
  }

  public String getBillingState() {
    return billingState;
  }

  public String getBillingCountry() {
    return billingCountry;
  }

  public String getShippingAddress() {
    return shippingAddress;
  }

  public String getShippingCity() {
    return shippingCity;
  }

  public String getShippingZipcode() {
    return shippingZipcode;
  }

  public String getShippingState() {
    return shippingState;
  }

  public String getShippingCountry() {
    return shippingCountry;
  }

  /**
   * Template class as returned by parent Customer.
   * 
   * @author Jason
   *
   */
  public static class ESTemplate {
    private Integer projectTemplateID;
    private String projectTemplateName;

    public Integer getProjectTemplateID() {
      return projectTemplateID;
    }

    public String getProjectTemplateName() {
      return projectTemplateName;
    }

  }

  /*
   * Getters
   */
  public static EsCustomer.CreationParams create(String name) {
    return new CreationParams(name);
  }

  /**
   * Creation parameters for new customers. The name parameter is the only required parameter.
   * 
   * @author Jason
   *
   */
  public static class CreationParams extends ApiRequest<EsObject> {

    public CreationParams(String name) {
      super("customer.create", EsObject.class);
      put("name", name);
    }

    public CreationParams withCode(String code) {
      put("code", code);
      return this;
    }

    public CreationParams withDescription(String description) {
      put("description", description);
      return this;
    }

    public CreationParams withDefaultProjectTemplateID(Integer defaultProjectTemplateID) {
      put("defaultProjectTemplateID", defaultProjectTemplateID);
      return this;
    }

    public CreationParams addTemplate(String ID) {
      arr("projectTemplateList", ID);
      return this;
    }

    public CreationParams addMetadata(String namespace, String property, Object value) {
      arr("metadatas", new Object[] {namespace, property, value});
      return this;
    }

    public CreationParams withParam(String param, String value) {
      put(param, value);
      return this;
    }

  }

  /**
   * Edit parameters for existing customers. The path or ID is required.
   * 
   * @param ref
   * @return
   */
  public static EsCustomer.EditParams edit(EsRef ref) {
    return new EditParams(ref);
  }

  public static class EditParams extends ApiRequest<EsStatus> {

    public EditParams(EsRef ref) {
      super("customer.edit", EsStatus.class);
      put("path", ref.getPath());
      put("ID", ref.getID());
    }

    public EditParams withCode(String code) {
      put("code", code);
      return this;
    }

    public EditParams withDescription(String description) {
      put("description", description);
      return this;
    }

    public EditParams addMetadata(String namespace, String property, Object value) {
      this.arr("metadatas", new Object[] {namespace, property, value});
      return this;
    }

    public EditParams withParam(String param, String value) {
      put(param, value);
      return this;
    }

  }

}
