package com.dalim.esprit.api.document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.dalim.esprit.api.EsBase;
import com.dalim.esprit.api.EsClass;
import com.google.gson.annotations.SerializedName;

public class EsDocumentWorkflow extends EsBase {

  public static class ListOf {
    private List<EsDocumentWorkflow> workflowList = new ArrayList<>();
    @SerializedName("class")
    private EsClass esclass;
    private Integer ID;

    public List<EsDocumentWorkflow> getAll() {
      return workflowList;
    }

    public EsClass getEsclass() {
      return esclass;
    }
    
    public Integer getID() {
      return ID;
    }

    public Optional<EsDocumentWorkflow> getByName(String name) {
      return workflowList.stream().filter(o -> o.getName().equals(name)).findFirst();
    }

    public Optional<EsDocumentWorkflow> getByID(int id) {
      return workflowList.stream().filter(o -> o.getID() == id).findFirst();
    }

  }


}
