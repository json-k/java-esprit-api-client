package com.dalim.esprit.api.production;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.keeber.esprit.EspritAPI;

import com.dalim.esprit.api.EsClass;
import com.google.gson.annotations.SerializedName;

public class EsSelectResult {
  private List<String> headers = new ArrayList<>();
  private List<Object[]> objectList = new ArrayList<>();
  @SerializedName("class")
  private EsClass esclass;

  public List<String> getHeaders() {
    return headers;
  }

  private String convertHeaderName(String headerName) {
    return headerName.replaceFirst("(.*)\\.", "").replaceAll("[^A-z]", "_");
  }

  
  
  /**
   * Converts this response to an array of Map, similar idea to a pure JSON response. It's a little
   * verbose because it duplicates the keys for every instance.
   * 
   * @return
   */
  public List<Map<String, Object>> convert() {
    return objectList.stream().map((row) -> {
      Map<String, Object> map = new HashMap<>();
      for (int i = 0; i < headers.size(); i++) {
        map.put(convertHeaderName(headers.get(i)), row[i]);
      }
      return map;
    }).collect(Collectors.toList());
  }

  /**
   * Convert this response to a List of the type of the provided class.
   * 
   * This is done by converting to a map with the {@link #convert()} method and then converting
   * through a Json Object.
   * 
   * @param clazz
   * @return
   */
  public <T> List<T> convert(Class<T> clazz) {
    return convert().stream().map(map -> EspritAPI.json.convert(map, clazz)).collect(Collectors.toList());
  }

  /**
   * Performs the supplied actions for each of the Row elements in this result.
   * 
   * @param consumer
   */
  public void forEach(Consumer<Object[]> consumer) {
    objectList.stream().forEach(o -> consumer.accept(o));
  }

  public List<Object[]> getObjectList() {
    return objectList;
  }

  public EsClass getEsclass() {
    return esclass;
  }



}
