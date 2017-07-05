package com.dalim.esprit.api;

public class EsBase {
  private String name;
  private Integer ID;

  protected EsBase() {

  }

  protected EsBase(Integer iD) {

    ID = iD;
  }

  public String getName() {
    return name;
  }

  public Integer getID() {
    return ID;
  }



}
