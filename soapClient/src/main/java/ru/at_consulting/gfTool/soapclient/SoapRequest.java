package ru.at_consulting.gfTool.soapclient;

import ru.at_consulting.gfTool.api.Request;

public class SoapRequest implements Request {

  private String message;


  public SoapRequest(String rqst){
      message = rqst;
  }

  @Override
  public byte[] raw() {
      return message.toString().getBytes();
  }

//  TODO Actual SOAPMessage
  @Override
  public Object requestImpl() {
      return null;
  }

//  TODO Вывод в строку тела сообщения
  @Override
  public String toString() {
      return null;
  }
}
