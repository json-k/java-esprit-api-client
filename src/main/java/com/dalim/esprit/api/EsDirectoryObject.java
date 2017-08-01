package com.dalim.esprit.api;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.annotations.SerializedName;

public class EsDirectoryObject {
  private String name, ID;
  @SerializedName("class")
  private EsClass esclass;

  public String getName() {
    return name;
  }

  public String getID() {
    return ID;
  }

  public EsClass getEsclass() {
    return esclass;
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
}
