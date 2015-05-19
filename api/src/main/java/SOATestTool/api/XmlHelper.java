package SOATestTool.api;

import org.w3c.dom.*;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ekhatko on 7/21/14.
 */
public class XmlHelper {

  public static AutotestLogger log = AutotestLogger.getLoggerInstance(XmlHelper.class.getSimpleName());

  public static void setElementValue(Node node, String xpath, String newValue) {
    XPath xPath = XPathFactory.newInstance().newXPath();
    NodeList nodes = null;
    try {
      nodes = (NodeList) xPath.evaluate(xpath, node, XPathConstants.NODESET);
    } catch (XPathExpressionException e) {
      log.error("Node " + xpath + " not found in current profile", e);
    }
    for (int i = 0; i < nodes.getLength(); ++i) {
      Element e = (Element) nodes.item(i);
      setElementValue(e, newValue);
    }
  }

  public static void setElementValue(Node elem, String value) {
    if ((elem != null) && (elem.hasChildNodes())) {
      for (Node kid = elem.getFirstChild(); kid != null; kid = kid.getNextSibling()) {
        if (kid.getNodeType() == Node.TEXT_NODE) {
          kid.setNodeValue(value);
          return;
        }
      }
    }
    log.debug("Unable to set value for node: " + getElementValue(elem) + "=" + value);
  }

  public static String getElementValue(Node elem) {
    if ((elem != null) && (elem.hasChildNodes())) {
      for (Node kid = elem.getFirstChild(); kid != null; kid = kid.getNextSibling()) {
        if (kid.getNodeType() == Node.TEXT_NODE) {
          return kid.getNodeValue();
        }
      }
    }
    return "";
  }

  public static String getElementValue(Node node, String nodeName) {
    NodeList children = node.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if ((child.getNodeType() == Node.ELEMENT_NODE) && (child.getNodeName().equals(nodeName)))
        return getElementValue(child);
    }
    return null;
  }


  public static String getAttribute(Node node, String attrName) {
    NamedNodeMap attr = node.getAttributes();
    if (attr != null) {
      Node nodeAttr = attr.getNamedItem(attrName);
      if (nodeAttr != null) {
        return nodeAttr.getNodeValue();
      }
    }
    return null;
  }

  public static int getAttrInt(Node node, String attrName) {
    String s = getAttribute(node, attrName);
    if (s != null) {
      return Integer.parseInt(s);
    }
    return -1;
  }

  public static long getAttrLong(Node node, String attrName) {
    String s = getAttribute(node, attrName);
    if (s != null) {
      return Long.parseLong(s);
    }
    return -1L;
  }



  public static Document parseFile(String fileName) {
    Document doc = null;
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    docBuilderFactory.setIgnoringElementContentWhitespace(true);
    DocumentBuilder docBuilder = null;

    try {
      docBuilder = docBuilderFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      log.error("Wrong parser configuration:", e);
      return null;
    }
    File sourceFile = new File(fileName);
    try {
      doc = docBuilder.parse(sourceFile);
    } catch (SAXException e) {
      log.error("Wrong XML file structure: ", e);
      return null;
    } catch (IOException e) {
      log.error("Could not read source file: ", e);
    }
    return doc;
  }

  public static Document fromXML(String xml) throws ParserConfigurationException,
      IOException, SAXException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setIgnoringElementContentWhitespace(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    return builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
  }

  /**
   * @see //http://www.java2s.com/Code/Java/XML/ReadXmlfromInputStreamandreturnDocument.htm
   */
  public static Document readXml(InputStream is) throws SAXException, IOException, ParserConfigurationException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setValidating(false);
    dbf.setIgnoringComments(false);
    dbf.setIgnoringElementContentWhitespace(true);
    dbf.setNamespaceAware(true);

    DocumentBuilder db = dbf.newDocumentBuilder();
    db.setEntityResolver(new NullResolver());
    return db.parse(is);
  }

  /**
   * @see //http://stackoverflow.com/questions/139076/how-to-pretty-print-xml-from-java
   */
  public static String toXML(Document document, boolean format) throws TransformerException {
    if (format) {
      removeWhitespaceNodes(document.getDocumentElement());
    }
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    transformerFactory.setAttribute("indent-number", 2);
    Transformer transformer = transformerFactory.newTransformer();

    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    Writer out = new StringWriter();
    transformer.transform(new DOMSource(document), new StreamResult(out));
    return out.toString();
  }

  /**
   * @see //http://www.java.net/node/667186
   */
  public static void removeWhitespaceNodes(Element e) {
    NodeList children = e.getChildNodes();
    for (int i = children.getLength() - 1; i >= 0; i--) {
      Node child = children.item(i);
      if (child instanceof Text && ((Text) child).getData().trim().length() == 0) {
        e.removeChild(child);
      } else if (child instanceof Element) {
        removeWhitespaceNodes((Element) child);
      }
    }
  }

  public static Map<String, String> XmlAsMap(Node node) {
    Map<String, String> map = new HashMap<String, String>();
    NodeList nodeList = node.getChildNodes();
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node currentNode = nodeList.item(i);
      if (currentNode.hasAttributes()) {
        for (int j = 0; j < currentNode.getAttributes().getLength(); j++) {
          Node item = currentNode.getAttributes().item(i);
          if(item != null)
            map.put(item.getNodeName(), prepare(item.getTextContent()));
        }
      }
      if(currentNode.getFirstChild() != null ){
        if (currentNode.getFirstChild().getNodeType() == Node.ELEMENT_NODE) {
          map.putAll(XmlAsMap(currentNode));
        } else if (currentNode.getFirstChild().getNodeType() == Node.TEXT_NODE) {
          map.put(currentNode.getLocalName(), prepare(currentNode.getTextContent()));
        }
      }
    }
    return map;
  }

  private static String prepare(String unprepared){
    return unprepared.replaceAll("\\s{2,}", "").replaceAll("\\n", "");
  }
}
class NullResolver implements EntityResolver {
  public InputSource resolveEntity(String publicId, String systemId) throws SAXException,
      IOException {
    return new InputSource(new StringReader(""));
  }
}
