package com.dalim.esprit.api;

public class EsBase implements EsReferenceable {
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

  public String getPath() {
    return null;
  }

  /**
   * This is here to make certain method returns make sense.
   * 
   * @author Jason Keeber <jason@keeber.org>
   *
   */
  public static class ListOf extends com.dalim.esprit.api.ListOf<EsBase> {

  }

}
