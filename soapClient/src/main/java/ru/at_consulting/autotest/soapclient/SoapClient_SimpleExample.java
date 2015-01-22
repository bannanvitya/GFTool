package ru.at_consulting.autotest.soapclient;

import javax.xml.soap.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SoapClient_SimpleExample {

  public static String getMessage(){
    return "Hello from SOAP module!";
  }

  public static void main(String[] args) throws SOAPException {
        //Сначала создаем соединение
    // вынесено в precondition(), newSession()
    SOAPConnectionFactory soapConnFactory
            = SOAPConnectionFactory.newInstance();
    SOAPConnection connection
            = soapConnFactory.createConnection();

         //Затем создаем сообщение
    //Вынесено в SoapProfile -> MessageHelper
    MessageFactory messageFactory = MessageFactory.newInstance();
    SOAPMessage message = messageFactory.createMessage();

    //Добавляем аутентификацию к сервису
    String username = "d";
    String password = "d";
    String authorization = new sun.misc.BASE64Encoder().encode((username + ":" + password).getBytes());
    MimeHeaders hd = message.getMimeHeaders();
    hd.addHeader("Authorization", "Basic " + authorization);

    //Создаем объекты, представляющие различные компоненты сообщения
    SOAPPart soapPart = message.getSOAPPart();
    SOAPEnvelope envelope = soapPart.getEnvelope();
    SOAPBody body = envelope.getBody();

    //Формирование сообщения из внешнего файла
    StreamSource preppedMsgSrc = null;
    try {
      preppedMsgSrc = new StreamSource(
              new FileInputStream("checkMemberBonusCard.xml"));
    } catch (FileNotFoundException ex) {
      Logger.getLogger(SoapClient_SimpleExample.class.getName()).log(Level.SEVERE, null, ex);
    }
    soapPart.setContent(preppedMsgSrc);

    //Сохранение сообщения
    message.saveChanges();

    //Проверка созданного сообщения
    System.out.println("\nREQUEST:\n");
    try {
      message.writeTo(System.out);
    } catch (IOException ex) {
      Logger.getLogger(SoapClient_SimpleExample.class.getName()).log(Level.SEVERE, null, ex);
    }
    System.out.println();

        //Отправка сообщения и получение ответа
    //Установка адресата
    String destination
            = "http://ms-glass017:7008/BonusCardService";
    //Отправка
    SOAPMessage reply = connection.call(message, destination);

    //Проверка полученного ответа
    System.out.println("\nRESPONSE:\n");
    //Создание XSLT-процессора
    TransformerFactory transformerFactory
            = TransformerFactory.newInstance();
    Transformer transformer = null;
    try {
      transformer = transformerFactory.newTransformer();
    } catch (TransformerConfigurationException ex) {
      Logger.getLogger(SoapClient_SimpleExample.class.getName()).log(Level.SEVERE, null, ex);
    }
    //Получение содержимого ответа
    Source sourceContent = reply.getSOAPPart().getContent();
    //Задание выходного потока для результата преобразования
    StreamResult result = new StreamResult(System.out);
    try {
      transformer.transform(sourceContent, result);
    } catch (TransformerException ex) {
      Logger.getLogger(SoapClient_SimpleExample.class.getName()).log(Level.SEVERE, null, ex);
    }
    System.out.println();

    //Закрываем соединение
    connection.close();
  }
}
