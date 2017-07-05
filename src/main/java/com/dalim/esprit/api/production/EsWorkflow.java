package com.dalim.esprit.api.production;

import java.util.List;
import java.util.stream.Collectors;

import com.dalim.esprit.api.EsBase;
import com.dalim.esprit.api.EsClass;

public class EsWorkflow extends EsBase {
  private EsClass onClass;

  public EsClass getOnClass() {
    return onClass;
  }

  public static class ListOf extends com.dalim.esprit.api.ListOf<EsWorkflow> {

    public List<EsWorkflow> getByWorkflowClass(EsClass esclass) {
      return getAll().stream().filter(w -> w.onClass.equals(esclass)).collect(Collectors.toList());
    }

  }

}
