package com.dalim.esprit.api;

import java.util.List;
import java.util.Optional;

import com.google.gson.annotations.SerializedName;

public class ListOf<T extends EsBase> {
  private List<T> objectList;
  @SerializedName("class")
  private EsClass esclass;

  public List<T> getAll() {
    return objectList;
  }

  public EsClass getEsclass() {
    return esclass;
  }

  public Optional<T> getByName(String name) {
    return objectList.stream().filter(o -> o.getName().equals(name)).findFirst();
  }

  public Optional<T> getByID(int id) {
    return objectList.stream().filter(o -> o.getID() == id).findFirst();
  }

}
