package SOATestTool.api;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by masia on 30/03/14.
 */
public class Parameter {
  private String key;
  private String value;
  private static Map<String, String> metadata = new HashMap<String, String>();
  //private static AutotestLogger log = AutotestConfigurator.getLogger(Parameter.class.getSimpleName());

  public void setKey(String k){
    key = k;
  }

  public void setValue(String v){
    value = v;
  }

  public String getValue(){
    for(String k : metadata.keySet()){
      if(value.equals(k))
        value = metadata.get(k);
      else if(value.contains(k))
        value = value.replace(k, metadata.get(k));
    }
    return value;
  }

  public String getKey(){
    return key;
  }

  public static void addMetadata(String key, String value){
    //log.info("Added metadata: " + key + "-->" + value);
    metadata.put(key, value);
  }
  public static String getMetadata(String key){
    return metadata.get(key);
  }
    /*
    in case of log tool we've got 3 possible keys
        contains
        last_lines
        fresh_lines
    and for example 2 params
        contains
        last_lines
     and each param is instance of Param class that contains Key and Value, for example:
     contains -> diam_session_id=192.168.45.118
     last_lines -> 15000
     */
  public static void validateParameters(List<Parameter> params, List<String> possibleKeys){
    // check that all elements from params list exists in possible keys list
      if(!possibleKeys.containsAll(Parameter.keysList(params))) {
      //log.info("Possible parameters: " + Arrays.toString(possibleKeys.toArray()));
      throw new ParameterException();
    }
  }

  public static void validateMandatoryParameters(List<Parameter> params, List<String> obligatoryKeys){
      // check that all elements from obligatoryKeys list exists in params
      if(!Parameter.keysList(params).containsAll(obligatoryKeys)){
      //log.info("Obligatory parameters: " + Arrays.toString(obligatoryKeys.toArray()));
      throw new ParameterException();
    }
  }
// Parameter class List
  public static List<String> keysList(List<Parameter> params){
    // we're getting key values from params list and transform it ti list of String values
      List<String> res = new LinkedList<String>();
    for(Parameter p : params)
      res.add(p.getKey());
    return res;
  }

  public static Map<String, String> asMap(List<Parameter> params){
    Map<String, String> res = new HashMap<String, String>();
    for(Parameter p : params){
      res.put(p.getKey(), p.getValue());
    }
    return res;
  }

  public String toString(){
    return getKey() + " -> " + getValue();
  }
}
