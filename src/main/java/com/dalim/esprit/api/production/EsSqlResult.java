package com.dalim.esprit.api.production;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.keeber.esprit.EspritAPI;

import com.dalim.esprit.api.admin.EsError;
import com.google.gson.JsonObject;
import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

public class EsSqlResult {
  public enum Table {
    CUSTOMER, JOB, DOCUMENT, FOLDER, LOG, FILE, DEADLINE, PRODUCTIONLIST, JOBAPPROVER, DOCUMENTAPPROVER;
  }

  public static String formatError(EsError error) {
    String response = "[" + error.getMessage() + "]";
    JsonObject o = error.getData().getAsJsonObject();
    if (o.has("longMessage")) {
      response += o.get("longMessage").getAsString();
    }
    return response;
  }

  private List<Header> headers = new ArrayList<>();
  private List<Object[]> objectList = new ArrayList<>();

  public List<Object[]> getObjectList() {
    return objectList;
  }

  public List<Header> getHeaders() {
    return headers;
  }

  public String toTable(String delimiter) {
    return new StringBuilder(headers.stream().map(h -> "[" + h.getTable() + "][" + h.type + "]" + h.getAlias()).collect(Collectors.joining(delimiter))).append("\n")
        .append(getObjectList().stream().map(row -> headers.stream().map(h -> h.toString(row[headers.indexOf(h)])).collect(Collectors.joining(delimiter))).collect(Collectors.joining("\n"))).toString();
  }

  public int getLength() {
    return objectList.size();
  }

  public Row getRow(int r) {
    return new Row(objectList.get(r));
  }

  /**
   * Converts this response to an array of Map, similar idea to a pure JSON response. It's a little
   * verbose because it duplicates the keys for every instance.
   * 
   * @return
   */
  public List<Map<String, Object>> convert() {
    return objectList.stream().map((o) -> {
      Map<String, Object> map = new HashMap<>();
      for (Header header : headers) {
        map.put(header.getAlias(), new Row(o).getValueByHeader(header));
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
  public void forEach(Consumer<Row> consumer) {
    objectList.stream().forEach(o -> consumer.accept(new Row(o)));
  }

  public class Row {
    private Object[] objects;

    private Row(Object[] objects) {
      this.objects = objects;
    }

    public <T> T getValueByHeaderName(String alias, Class<T> clazz) {
      Optional<Header> oHeader = getHeaderByName(alias);
      return getValueByHeader(oHeader.orElseThrow(() -> new RuntimeException("Header not found for [alias=" + alias + "]")));
    }

    public <T> T getValueByHeaderName(Table table, String alias) {
      Optional<Header> oHeader = getHeaderByName(table, alias);
      return getValueByHeader(oHeader.orElseThrow(() -> new RuntimeException("Header not found for [alias=" + alias + "]")));
    }

    public Optional<Header> getHeaderByName(String alias) {
      return headers.stream().filter(h -> h.getAlias().equals(alias)).findFirst();
    }

    public Optional<Header> getHeaderByName(Table table, String alias) {
      return headers.stream().filter(h -> h.getAlias().equals(alias) && h.getTable() == table).findFirst();
    }

    @SuppressWarnings("unchecked")
    public <T> T getValueByHeader(Header header) {
      return (T) header.toObject(objects[headers.indexOf(header)], header.getJavaType());
    }


  }

  public static class Header {
    public enum DataType {
      B, I, F, DT, D, T, IM
    }

    private String name, alias;
    private Table table;
    private DataType type;

    public String getName() {
      return name;
    }

    public String getAlias() {
      return alias == null ? name : alias;
    }

    public Table getTable() {
      return table;
    }

    public Class<?> getJavaType() {
      if (type == DataType.B) {
        return Boolean.class;
      }
      if (type == DataType.F) {
        return float.class;
      }
      if (type == DataType.I) {
        return int.class;
      }
      if (type == DataType.DT || type == DataType.D) {
        return Date.class;
      }
      return String.class;
    }

    public String toString(Object o) {
      if (o == null) {
        return "";
      }
      if (getJavaType().equals(Boolean.class)) {
        return Boolean.toString(toObject(o, Boolean.class));
      }
      if (getJavaType().equals(float.class)) {
        return String.format("%.4f", toObject(o, float.class));
      }
      if (getJavaType().equals(int.class)) {
        return String.format("%d", toObject(o, int.class));
      }
      if (getJavaType().equals(Date.class)) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(toObject(o, Date.class).getTime()), ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      }
      return o.toString();
    }

    @SuppressWarnings("unchecked")
    public <T> T toObject(Object o, Class<T> clazz) {
      if (o == null) {
        return null;
      }
      if (!clazz.equals(getJavaType())) {
        throw new RuntimeException("Requested class [" + clazz + "] does not match [" + type + "].");
      }
      if (clazz.equals(Boolean.class)) {
        return (T) Boolean.valueOf(o.toString());
      }
      if (clazz.equals(float.class)) {
        return (T) Float.valueOf(o.toString());
      }
      if (clazz.equals(int.class)) {
        return (T) Integer.valueOf(new BigDecimal(o.toString()).toBigInteger().intValue());
      }
      if (clazz.equals(String.class)) {
        return (T) o.toString();
      }
      if (clazz.equals(Date.class)) {
        Parser parser = new Parser();
        List<DateGroup> groups = parser.parse(o.toString());
        for (DateGroup group : groups) {
          List<Date> dates = group.getDates();
          if (dates.size() > 0) {
            return (T) dates.get(0);
          }
        }
        return null;
      }
      return null;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((alias == null) ? 0 : alias.hashCode());
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((table == null) ? 0 : table.hashCode());
      result = prime * result + ((type == null) ? 0 : type.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Header other = (Header) obj;
      if (alias == null) {
        if (other.alias != null)
          return false;
      } else if (!alias.equals(other.alias))
        return false;
      if (name == null) {
        if (other.name != null)
          return false;
      } else if (!name.equals(other.name))
        return false;
      if (table == null) {
        if (other.table != null)
          return false;
      } else if (!table.equals(other.table))
        return false;
      if (type != other.type)
        return false;
      return true;
    }

  }

}
