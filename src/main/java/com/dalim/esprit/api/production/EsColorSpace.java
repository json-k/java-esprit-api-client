package com.dalim.esprit.api.production;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.dalim.esprit.api.EsClass;
import com.dalim.esprit.api.EsObject;

public class EsColorSpace extends EsObject {

  @Override
  public EsClass getEsclass() {
    return EsClass.ColorSpace;
  }

  public static class ListOf {
    private List<EsColorSpace> objectList = new ArrayList<>();

    public List<EsColorSpace> getAll() {
      return objectList;
    }

    public Optional<EsColorSpace> getByName(String name) {
      return objectList.stream().filter(o -> o.getName().equals(name)).findFirst();
    }

    public Optional<EsColorSpace> getByID(int id) {
      return objectList.stream().filter(o -> o.getID() == id).findFirst();
    }


  }

}
