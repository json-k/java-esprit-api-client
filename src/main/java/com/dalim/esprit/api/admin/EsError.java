package com.dalim.esprit.api.admin;

import com.google.gson.JsonElement;

public class EsError {
  private String code, message;
  private JsonElement data;

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public JsonElement getData() {
    return data;
  }

  public EsException toException() {
    return new EsException(this);
  }

  public void throwException() throws EsException {
    throw new EsException(this);
  }

  private String dataToString() {
    return (getData() != null && getData().isJsonObject() && getData().getAsJsonObject().has("longMessage") ? getData().getAsJsonObject().getAsJsonPrimitive("longMessage") : getData()) + "";
  }

  public static class EsException extends Exception {

    public EsException(EsError error) {
      super("[" + error.getCode() + "] " + error.getMessage() + " " + error.dataToString());
    }
  }

}
