package ru.at_consulting.autotest.soapclient;

import ru.at_consulting.autotest.api.AutotestLogger;
import ru.at_consulting.autotest.api.Request;
import ru.at_consulting.autotest.api.Response;

import javax.xml.soap.SOAPConnection;

public class SoapRequest implements Request {

  private static AutotestLogger log = AutotestLogger.getLoggerInstance(SoapClient.class.getSimpleName());
  private MessageHelper helper;


  public SoapRequest(SOAPConnection c, MessageHelper mh, Response r){
    helper = mh;
  }

//  TODO MHlp.getDOM() и транслировать в byte[]
  @Override
  public byte[] raw() {
    return MessageHelper.viewMessage(helper.getSoapMessage()).getBytes();
  }

//  TODO Actual SOAPMessage
  @Override
  public Object requestImpl() {
    return helper.getSoapMessage();
  }

//  TODO Вывод в строку тела сообщения
  @Override
  public String toString() {
    return "SoapRequest " + " { doc=" + MessageHelper.viewMessage(helper.getSoapMessage()) + " }";
  }
}
