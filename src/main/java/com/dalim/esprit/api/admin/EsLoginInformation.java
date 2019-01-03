package com.dalim.esprit.api.admin;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class EsLoginInformation extends EsLoginResponse {
  private ESUserInformation userInformation;

  public ESUserInformation getUserInformation() {
    return userInformation;
  }

  /**
   * I may have made a weird decision on these getters but the grammar just seems better and I am
   * not sure I can see this being used as a bean.
   * 
   * @author Jason
   *
   */
  public static class ESUserInformation {
    private String lastName, color, unitLength, name, unitResolution, firstName, orgUnit, lang;
    private boolean canBrowseFileSystem, canBrowseProductCustomer, canBrowseCollection, canBrowseCustomer;
    @SerializedName(value = "Roles")
    private List<String> roles = new ArrayList<>();

    public String getLastName() {
      return lastName;
    }

    public String getColor() {
      return color;
    }

    public String getUnitLength() {
      return unitLength;
    }

    public List<String> getRoles() {
      return roles;
    }

    public boolean hasRole(String role) {
      return roles.contains(role);
    }

    /**
     * This is the user name - if you needed to set the user in a field (such as a metadata field) -
     * this is the value that you would use.
     * 
     * @return the user name
     */
    public String getName() {
      return name;
    }

    public String getUnitResolution() {
      return unitResolution;
    }

    public String getFirstName() {
      return firstName;
    }

    public String getOrgUnit() {
      return orgUnit;
    }

    public String getLang() {
      return lang;
    }

    public boolean canBrowseFileSystem() {
      return canBrowseFileSystem;
    }

    public boolean canBrowseProductCustomer() {
      return canBrowseProductCustomer;
    }

    public boolean canBrowseCollection() {
      return canBrowseCollection;
    }

    public boolean canBrowseCustomer() {
      return canBrowseCustomer;
    }

  }
}
