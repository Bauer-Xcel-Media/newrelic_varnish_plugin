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
 * Collects Varnish metrics via the varnishstat command
 *
 * @author Jan Schumann <jan.schumann@bauerexcel.de>
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

    /**
     * Add an instance name to the command
     *
     * @param instance The name of the varnish instance
     */
    public void setInstance(String instance) {
        if (!instanceSet && null != instance) {
            this.command += " -n " + instance;
            instanceSet = true;
        }
    }

    /**
     * Add ssh connection command prefix
     *
     * @param command The ssh command to connect to the varnish node
     */
    public void setSshCommand(String command) {
        if (!sshCommandSet && null != command) {
            this.command = command + " -q " + this.command;
            sshCommandSet = true;
        }
    }

    /**
     * Runs the stats command runs without errors
     * If an error occurs, it can be retrieved via getError()
     *
     * @return Boolean
     */
    public Boolean isValid() {
        try {
            getStats();
        } catch (Exception e) {
            error = e.getMessage();
            return false;
        }

        return true;
    }

    /**
     * Fetch Statsisics
     *
     * @return ArrayList<Metric>
     * @throws InterruptedException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
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

    /**
     * Contains the error message, if an error occured by isValid()
     *
     * @return String
     */
    public String getError() {
        return error;
    }

    /**
     * Get the command this instances uses to gather data
     *
     * @return String
     */
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
}
