package com.dalim.esprit.api.directory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.dalim.esprit.api.EsClass;
import com.google.gson.annotations.SerializedName;

public class EsDirectoryObject {
  private String name, ID;
  @SerializedName("class")
  private EsClass esclass;

  public static final EsDirectoryObject ROOT = new EsDirectoryObject();
  public static final EsDirectoryObject MAIN_ORG = EsDirectoryObject.from("Main Organization", EsClass.OrganizationalUnit);

  public EsDirectoryObject() {

  }

  public EsDirectoryObject(String ID, EsClass esClass) {
    this.ID = ID;
    this.esclass = esClass;
  }

  public String getName() {
    return name == null ? ID : name;
  }

  public String getID() {
    return ID;
  }

  public EsClass getEsclass() {
    return esclass;
  }

  public static EsDirectoryObject from(String ID, EsClass esClass) {
    return new EsDirectoryObject(ID, esClass);
  }

  public static class ListOf {
    private List<EsDirectoryObject> objectList;

    public List<EsDirectoryObject> getAll() {
      return objectList;
    }

    public List<EsDirectoryObject> getByESClass(EsClass esclass) {
      return getAll().stream().filter(o -> o.getEsclass() == esclass).collect(Collectors.toList());
    }

    public Optional<EsDirectoryObject> getByName(String name) {
      return objectList.stream().filter(o -> o.getName().equals(name)).findFirst();
    }

    public Optional<EsDirectoryObject> getByID(String id) {
      return objectList.stream().filter(o -> o.getID() == id).findFirst();
    }

  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((ID == null) ? 0 : ID.hashCode());
    result = prime * result + ((esclass == null) ? 0 : esclass.hashCode());
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
    EsDirectoryObject other = (EsDirectoryObject) obj;
    if (ID == null) {
      if (other.ID != null) {
        return false;
      }
    } else if (!ID.equals(other.ID)) {
      return false;
    }
    if (esclass != other.esclass) {
      return false;
    }
    return true;
  }



}
