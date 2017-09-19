package com.dalim.esprit.api;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.gson.annotations.SerializedName;

public class EsObject extends EsBase implements EsClassable {

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


  /**
   * ES Objects are the same based on their Esclass and ID (or so it appears) so they are compared
   * here via reflection - allowing different objects to be compared.
   * 
   * <p>
   * Technically this breaks Java's equal behavior because it compares objects of two different
   * kinds - but what the hey, lets see what happens.
   * 
   */
  @Override
  public boolean equals(Object other) {
    if (other != null) {
      try {
        for (String fname : new String[] {"Esclass", "ID"}) {
          if (!Objects.equals(getClass().getMethod("get" + fname).invoke(this), other.getClass().getMethod("get" + fname).invoke(other))) {
            return false;
          }
        }
      } catch (IllegalAccessException | IllegalArgumentException | SecurityException | InvocationTargetException | NoSuchMethodException e) {
        return false;
      }
    }
    return true;
  }

}
