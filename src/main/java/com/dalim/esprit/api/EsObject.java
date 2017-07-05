package com.dalim.esprit.api;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.annotations.SerializedName;

public class EsObject extends EsBase {

  private Date lastModificationDate, creationDate;
  @SerializedName("class")
  private EsClass esclass;
  private String folderType;

  public static final EsObject ROOT = new EsObject();

  protected EsObject() {
    super();
  }

  protected EsObject(Integer ID) {
    super(ID);
  }

  public static EsObject from(Integer ID, EsClass esClass) {
    EsObject o = new EsObject(ID);
    o.esclass = esClass;
    return o;
  }

  public Date getLastModificationDate() {
    return lastModificationDate;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public EsClass getEsclass() {
    return esclass;
  }

  public String getFolderType() {
    return folderType;
  }

  public static class ListOf extends com.dalim.esprit.api.ListOf<EsObject> {

    public List<EsObject> getByESClass(EsClass esclass) {
      return getAll().stream().filter(o -> o.getEsclass() == esclass).collect(Collectors.toList());
    }

  }

}
