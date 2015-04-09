package gfTool.api;

import cucumber.api.Transformer;

/**
 * Created by masia on 30/03/14.
 */
public class ParameterTransformer extends Transformer<Parameter> {

  @Override
  public Parameter transform(String param) {
    Parameter res = new Parameter();
    res.setValue(param);
    return res;
  }
}
