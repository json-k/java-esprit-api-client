package com.dalim.esprit.api.directory;

import java.util.ArrayList;
import java.util.List;

import com.dalim.esprit.api.EsClass;
import com.google.gson.annotations.SerializedName;

public class EsGroup {
  private String ID,description;
  @SerializedName("class")
  private EsClass esclass;
  private List<EsUser> userList = new ArrayList<>();

  public String getID() {
    return ID;
  }

  public String getDescription() {
    return description;
  }

  /**
   * This returns the ID in this case - because they are kind of the same thing to dalim with
   * certain objects.
   * 
   * @return
   */
  public String getName() {
    return ID;
  }

  public EsClass getEsclass() {
    return esclass;
  }

  public List<EsUser> getUserList() {
    return userList;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((ID == null) ? 0 : ID.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    EsGroup other = (EsGroup) obj;
    if (ID == null) {
      if (other.ID != null) {
        return false;
      }
    } else if (!ID.equals(other.ID)) {
      return false;
    }
    return true;
  }


}
