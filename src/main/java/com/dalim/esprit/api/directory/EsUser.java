package com.dalim.esprit.api.directory;

import org.keeber.esprit.EspritAPI.ApiRequest;

import com.dalim.esprit.api.EsClass;
import com.google.gson.annotations.SerializedName;

public class EsUser extends EsDirectoryObject {
  private String firstName, lastName, phone, fax, street, company, state, title, orgUnit, lang, country, city, color, zipCode, eMail, gender, login, defaultProfile;
  private Boolean userCanLog;

  public EsUser() {
    super();
  }

  public EsUser(String ID, String orgUnit) {
    super(ID, EsClass.User);
    this.orgUnit = orgUnit;
  }

  @SerializedName("unit.length")
  private String unitLength;
  @SerializedName("unit.resolution")
  private String unitResolution;

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getPhone() {
    return phone;
  }

  public String getFax() {
    return fax;
  }

  public String getStreet() {
    return street;
  }

  public String getOrgUnit() {
    return orgUnit;
  }

  public String getLang() {
    return lang;
  }

  public String getCountry() {
    return country;
  }

  public String getCity() {
    return city;
  }

  public String getColor() {
    return color;
  }

  public String getZipCode() {
    return zipCode;
  }

  public String geteMail() {
    return eMail;
  }

  public String getGender() {
    return gender;
  }

  public String getLogin() {
    return login;
  }

  public String getDefaultProfile() {
    return defaultProfile;
  }

  public boolean isUserCanLog() {
    return userCanLog == null ? false : userCanLog.booleanValue();
  }

  public String getUnitLength() {
    return unitLength;
  }

  public String getUnitResolution() {
    return unitResolution;
  }

  public String getCompany() {
    return company;
  }

  public String getState() {
    return state;
  }

  public String getTitle() {
    return title;
  }


  /**
   * Return creation params with the given options (all required)
   * 
   * @param orgUnit name
   * @param name or ID of the user (not the login)
   * @param defaultProfile of the user
   * @return
   */
  public static EsUser.CreationParams create(String orgUnit, String name, String defaultProfile) {
    return new CreationParams(orgUnit, name, defaultProfile);
  }

  /**
   * User creation parameters. The ORG, username (or ID) and defaultProfile are all required.
   * 
   * @author Jason Keeber <jason@keeber.org>
   *
   */
  public static class CreationParams extends ApiRequest<EsUser> {

    protected CreationParams(String orgUnit, String name, String defaultProfile) {
      super("directory.createUser", EsUser.class);
      put("name", name);
      put("orgUnit", orgUnit);
      put("defaultProfile", defaultProfile);
    }

    /**
     * Enables the user to log in ES. If not present the user will not be able to log in ES.
     * 
     * @param login
     * @return
     */
    public CreationParams withLogin(String login) {
      put("login", login);
      return this;
    }

    public CreationParams withName(String firstName, String lastName) {
      put("firstName", firstName);
      put("lastName", lastName);
      return this;
    }

    public CreationParams withPassword(String password) {
      put("password", password);
      return this;
    }

    public CreationParams withLang(String lang) {
      put("lang", "lang");
      return this;
    }
    
    public CreationParams withUserCanLog(boolean canlog) {
      put("userCanLog", canlog);
      return this;
    }

    public CreationParams withParam(String param, String value) {
      put(param, value);
      return this;
    }

  }

  /**
   * Returns edit parameters for existing user. The name is required.
   * 
   * @param name (not the login)
   * @return
   */
  public static EsUser.EditParams edit(String ID) {
    return new EditParams(ID);
  }

  /**
   * Edit parameters for existing customer.
   * 
   * @author Jason Keeber <jason@keeber.org>
   *
   */
  public static class EditParams extends ApiRequest<EsUser> {

    protected EditParams(String ID) {
      super("directory.editUser", EsUser.class);
      put("ID", ID);
    }

    /**
     * Enables the user to log in ES. If not present the user will not be able to log in ES.
     * 
     * @param login
     * @return
     */
    public EditParams withLogin(String login) {
      put("login", login);
      return this;
    }

    public EditParams withName(String firstName, String lastName) {
      put("firstName", firstName);
      put("lastName", lastName);
      return this;
    }

    public EditParams withPassword(String password) {
      put("password", password);
      return this;
    }

    public EditParams withLang(String lang) {
      put("lang", "lang");
      return this;
    }

    public EditParams withUserCanLog(boolean canlog) {
      put("userCanLog", canlog);
      return this;
    }

    public EditParams withParam(String param, String value) {
      put(param, value);
      return this;
    }

  }

}
