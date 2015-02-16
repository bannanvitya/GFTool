package ru.at_consulting.gfTool.soapclient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.predic8.wstool.creator.RequestTemplateCreator;
import com.predic8.wstool.creator.SOARequestCreator;
import groovy.xml.MarkupBuilder;
import org.codehaus.groovy.runtime.metaclass.MissingPropertyExceptionNoStack;

import com.predic8.schema.ComplexType;
import com.predic8.schema.Element;
import com.predic8.schema.Schema;
import com.predic8.schema.SimpleType;
import com.predic8.schema.restriction.BaseRestriction;
import com.predic8.schema.restriction.facet.EnumerationFacet;
import com.predic8.wsdl.AbstractBinding;
import com.predic8.wsdl.Binding;
import com.predic8.wsdl.BindingOperation;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Operation;
import com.predic8.wsdl.Part;
import com.predic8.wsdl.Port;
import com.predic8.wsdl.PortType;
import com.predic8.wsdl.Service;

public class WsdlHelper {



    /* Parses WSDL definitions and identifies endpoints and operations. */
    public static HashMap<String, String> parseWSDL(Definitions wsdl){
        List<Service> services = wsdl.getServices();
        HashMap<String, String> map = null;

        /* Endpoint identification. */
        for(Service service : services){

            for(Port port: service.getPorts()){

                Binding binding = port.getBinding();
                AbstractBinding innerBinding = binding.getBinding();
                String soapPrefix = innerBinding.getPrefix();
                int soapVersion = detectSoapVersion(wsdl, soapPrefix);
                String style = detectStyle(innerBinding);

                if(style != null && (style.equals("document") || style.equals("rpc")) ){

                    List<BindingOperation> operations = binding.getOperations();
                    String endpointLocation = port.getAddress().getLocation().toString();

		    	    /* Identifies operations for each endpoint.. */
                    for(BindingOperation bindOp : operations){

                        StringWriter writer = new StringWriter();

                        //SOAPRequestCreator constructor: SOARequestCreator(Definitions, Creator, MarkupBuilder)
                        SOARequestCreator creator = new SOARequestCreator(wsdl, new RequestTemplateCreator(), new MarkupBuilder(writer));

                        //creator.createRequest(PortType name, Operation name, Binding name);
                        creator.createRequest(port.getName(), bindOp.getName(), port.getBinding().getName());

                        String msg = writer.toString();

                        map.put(bindOp.getName(), msg);
                    } //bindingOperations loop
                } //Binding check if
            }// Ports loop
        }
        return map;
    }



    /* Detects SOAP version used in a binding, given the wsdl content and the soap binding prefix. */
    private static int detectSoapVersion(Definitions wsdl, String soapPrefix){
        String soapNamespace = wsdl.getNamespace(soapPrefix).toString();
        if(soapNamespace.trim().equals("http://schemas.xmlsoap.org/wsdl/soap12/")){
            return 2;
        }else{
            return 1;
        }
    }

    private static String detectStyle(AbstractBinding binding){
        try{
            String r = binding.getProperty("style").toString();
            binding.getProperty("transport");
            return r.trim();
        }catch (MissingPropertyExceptionNoStack e){
            return null;
        }
    }



    private static List<Part> detectParameters(Definitions wsdl, BindingOperation bindOp){
        for(PortType pt : wsdl.getPortTypes()){
            for(Operation op : pt.getOperations()){
                if (op.getName().trim().equals(bindOp.getName().trim())){
                    return op.getInput().getMessage().getParts();
                }
            }
        }
        return null;
    }

    private static HashMap<String, String> fillParameters(Element element, String parent){
        HashMap<String, String> formParams = new HashMap<String, String>();
        try{
			/* Tries to parse it as a complex type first. */
            String xpath = null;
            if (parent != null) xpath = parent + "/" +element.getName();
            else xpath = element.getName();
            ComplexType ct = (ComplexType) element.getEmbeddedType();
    		/* Handles when ComplexType is not embedded but referenced by 'type'. */
            if (ct == null){
                Schema currentSchema = element.getSchema();
                ct = (ComplexType) currentSchema.getType(element.getType());
                if (ct == null) throw new ClassCastException("Complex Type is null after cast."); //Hashmap is empty here.
            }
            for (Element e : ct.getSequence().getElements()) {
    			/* Recursive parsing for nested complex types. */
                formParams.putAll(fillParameters(e,xpath));
            }
        }catch(ClassCastException cce){
			/* Simple element treatment. */
            if(element == null) return formParams;
			/* Handles simple types. */
            SimpleType simpleType = null;
            try{
                simpleType = (SimpleType) element.getEmbeddedType();
                if(simpleType == null){
                    Schema currentSchema = element.getSchema();
                    simpleType = (SimpleType) currentSchema.getType(element.getType());
                    if(simpleType == null){
						/* It is not simple type, so it is treated as a plain element. */
                        String xpath = "";
                        if (parent != null) xpath = parent + "/" +element.getName();
                        else xpath = element.getName();
                        if(element.getType() != null) return addParameter(xpath,element.getType().getQualifiedName(),null);
                        else return formParams;
                    }
                }
            }catch(ClassCastException cce2){
				/* It is not simple type, so it is treated as a plain element. */
                String xpath = "";
                if (parent != null) xpath = parent + "/" +element.getName();
                else xpath = element.getName();
                return addParameter(xpath,element.getType().getQualifiedName(),null);
            }
			/* Handles enumeration restriction. */
            BaseRestriction br = simpleType.getRestriction();
            if (br != null){
                List<EnumerationFacet> enums = br.getEnumerationFacets();
                if(enums != null && enums.size() > 0){
                    String defaultValue = enums.get(0).getValue();
                    formParams.putAll(addParameter(parent+"/"+element.getName(),"string",defaultValue));
                }
            }
            return formParams;
        }catch(Exception e){
            return null;
        }
        return formParams;
    }

    private static HashMap<String, String> addParameter(String path, String paramType, String value){
        HashMap<String,String> formParams = new HashMap<String,String> ();
        if(paramType.contains(":")){
            String[] stringParts = paramType.split(":");
            paramType = stringParts[stringParts.length-1];
        }
		/* If value is specified, it is directly set. */
        if(value != null){
            formParams.put("xpath:/"+path, value);
            return formParams;
        }
		/* Parameter value depends on parameter type. */
        if(paramType.equals("string")){
            formParams.put("xpath:/"+path, "paramValue");
        }else if(paramType.equals("int") || paramType.equals("double") ||
                paramType.equals("long")){
            formParams.put("xpath:/"+path, "0");
        }else if(paramType.equals("date")){
            Date date = new Date();
            SimpleDateFormat dt1 = new SimpleDateFormat("CCYY-MM-DD");
            String dateS = dt1.format(date);
            formParams.put("xpath:/"+path, dateS);
        }else if(paramType.equals("dateTime")){
            Date date = new Date();
            SimpleDateFormat dt1 = new SimpleDateFormat("CCYY-MM-DDThh:mm:ssZ");
            String dateS = dt1.format(date);
            formParams.put("xpath:/"+path, dateS);
        }
        return formParams;
    }


}