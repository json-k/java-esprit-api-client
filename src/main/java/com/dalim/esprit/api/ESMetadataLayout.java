package com.dalim.esprit.api;

import java.util.List;

public  class ESMetadataLayout {
  private String tabName;
  private List<ESField> fields;

  public String getTabName() {
    return tabName;
  }

  public List<ESField> getFields() {
    return fields;
  }

  public static class ESField {
    private String name, nameSpace, value, type;

    public String getName() {
      return name;
    }

    public String getNameSpace() {
      return nameSpace;
    }

    public String getValue() {
      return value;
    }

    public String getType() {
      return type;
    }
  }

}
