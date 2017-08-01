package com.dalim.esprit.api.production;

import java.util.Optional;

import com.dalim.esprit.api.EsClass;
import com.dalim.esprit.api.EsObject;

public class EsProjectTemplate extends EsObject {
  private String shortName;

  /**
   * The name you see in the API.
   * 
   * @return
   */
  public String getShortName() {
    return shortName;
  }

  @Override
  public EsClass getEsclass() {
    return EsClass.Product;
  }

  public static class ListOf extends com.dalim.esprit.api.ListOf<EsProjectTemplate> {

    /**
     * This is the name you see in the UI. The 'name' is the reference you use when creating a job.
     * 
     * @param shortName
     * @return
     */
    public Optional<EsProjectTemplate> getByShortName(String shortName) {
      return getAll().stream().filter(o -> o.getShortName().equals(shortName)).findFirst();
    }


  }

}
