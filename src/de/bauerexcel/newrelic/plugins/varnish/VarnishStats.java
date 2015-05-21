package de.bauerexcel.newrelic.plugins.varnish;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by jan.schumann on 20.05.15.
 */
public class VarnishStats {
    private final static String COMMAND = "varnishstat -1 -x";

    private String command;
    private Runtime runtime = Runtime.getRuntime();
    private String error;
    private Boolean instanceSet = false;
    private Boolean sshCommandSet = false;

    public VarnishStats() {
        this.command = COMMAND;
    }

    public void setInstance(String instance) {
        if (!instanceSet && null != instance) {
            this.command += " -n " + instance;
            instanceSet = true;
        }
    }

    public void setSshCommand(String command) {
        if (!sshCommandSet && null != command) {
            this.command = command + " -q " + this.command;
            sshCommandSet = true;
        }
    }

    public Boolean isValid() {
        try {
            getStats();
        } catch (Exception e) {
            error = e.getMessage();
            return false;
        }

        return true;
    }

    public String getError() {
        return error;
    }

    public String getCommand() {
        return command;
    }

    private NodeList getStats() throws java.io.IOException, java.lang.InterruptedException, ParserConfigurationException, SAXException {
        Process p = this.runtime.exec(command);
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String aux = "";

        while ((aux = in.readLine()) != null) {
            builder.append(aux);
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document dom = db.parse(new InputSource(new ByteArrayInputStream(builder.toString().replace("\t", "").getBytes("utf-8"))));

        NodeList nlist = dom.getElementsByTagName("stat");

        p.waitFor();

        return nlist;
    }


    public ArrayList<Metric> fetch() throws InterruptedException, ParserConfigurationException, SAXException, IOException {
        ArrayList<Metric> results = new ArrayList();

        NodeList stats = getStats();
        for (int i = 0; i < stats.getLength(); i++) {
            Element statsElement = (Element) stats.item(i);
            Metric metric = new Metric(statsElement);
            results.add(metric);
        }

        return results;
    }

}
