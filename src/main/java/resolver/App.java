package resolver;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.xml.sax.*;
import org.w3c.dom.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        List<String> contents = getContents("/Users/carlos.araujo/Downloads/translations/nl/inheritance/jcr_root/content/wcgcom/corp-masters/nl");
        for(String x: contents){
            changeXML(x);
            System.out.println( x );
        }
    }

    public static boolean changeXML(String xmlFilePath) {

        Document dom;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(xmlFilePath);
            Element doc = dom.getDocumentElement();

            NodeList children = doc.getChildNodes();

            Node current = null;
            int count = children.getLength();
            for (int i = 0; i < count; i++) {
                current = children.item(i);
                if (current.getNodeType() == Node.ELEMENT_NODE && current.getNodeName().equals("jcr:content")) {
                    Element element = (Element) current;
                    setMixingTypes(element);
                    modifyMixinTypes(element);
                }
            }

            File fileToDelete = new File(xmlFilePath);
            fileToDelete.delete();

            TransformerFactory transformerFactory = TransformerFactory.newInstance();

            DOMSource source = new DOMSource(dom);

            FileWriter writer = new FileWriter(new File(xmlFilePath));
            StreamResult result = new StreamResult(writer);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(source, result);

            return true;

        } catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
            return false;
        } catch (SAXException se) {
            System.out.println(se.getMessage());
            return false;
        } catch (java.io.IOException ioe) {
            System.err.println(ioe.getMessage());
            return false;
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
            return false;
        } catch (TransformerException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void modifyMixinTypes( Element element){
        NodeList children = element.getChildNodes();

        Node current = null;
        int count = children.getLength();
        for (int i = 0; i < count; i++) {
            current = children.item(i);
            if (current.getNodeType() == Node.ELEMENT_NODE) {
                Element currElement = (Element) current;
                setMixingTypes(currElement);
                modifyMixinTypes(currElement);
            }
        }
    }

    private static void setMixingTypes(Element currElement){
        String mixingTypes= currElement.getAttribute("jcr:mixinTypes");
        String tagName = currElement.getTagName();
        if(mixingTypes!=null && !mixingTypes.contains("cq:LiveSyncCancelled") && !tagName.equals("cq:LiveSyncConfig") && !tagName.equals("file")){
            if(mixingTypes.equals("")){
                currElement.setAttribute("jcr:mixinTypes","[cq:LiveSyncCancelled]");
            }else{
                String composed = mixingTypes.replace("]","");
                composed = composed+ ",cq:LiveSyncCancelled]";
                currElement.setAttribute("jcr:mixinTypes",composed);
            }
            System.out.println(currElement.getTagName());
            System.out.println(currElement.getAttribute("jcr:mixinTypes"));
        }
    }

    private static void setPropertyInheritanceCancelled(Element currElement){
        String propertyInheritanceCancelled= currElement.getAttribute("cq:propertyInheritanceCancelled");
        String primaryType = currElement.getAttribute("jcr:primaryType");
        if(propertyInheritanceCancelled.equals("")&& primaryType.equals("cq:PageContent")){
            currElement.setAttribute("cq:propertyInheritanceCancelled","[jcr:title]");
            System.out.println(currElement.getTagName());
            System.out.println(currElement.getAttribute("cq:propertyInheritanceCancelled"));
        }
    }

    private static String getTextValue(String def, Element doc, String tag) {
        String value = def;
        NodeList nl;
        nl = doc.getElementsByTagName(tag);
        if (nl.getLength() > 0 && nl.item(0).hasChildNodes()) {
            value = nl.item(0).getFirstChild().getNodeValue();
        }
        return value;
    }

    private static List<String> getContents(String path){

        List<String> result = new ArrayList<>();
        File folder = new File(path);

        for (final File f : folder.listFiles()) {

            if (f.isDirectory()) {
                List<String> ff = getContents(f.getAbsolutePath());
                for(String fx : ff){
                    result.add(fx);
                }
            }else if(f.isFile() && f.getName().contains(".content.xml")){
                result.add(f.getAbsolutePath());
            }
            }
        return result;
    }
}
