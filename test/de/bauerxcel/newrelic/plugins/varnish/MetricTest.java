package de.bauerxcel.newrelic.plugins.varnish;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import static org.junit.Assert.*;

public class MetricTest {
    @Test
    public void TestVarnishVersion51_NoIdent() throws IOException, SAXException, ParserConfigurationException {
        Metric metric = getMetricFromDocument("<?xml version=\"1.0\"?>\n" +
                "<stat>\n" +
                "    <type>MAIN</type>\n" +
                "    <name>uptime</name>\n" +
                "    <value>12334556</value>\n" +
                "    <flag>c</flag>\n" +
                "    <format>d</format>\n" +
                "    <description>Child process uptime</description>\n" +
                "</stat>");
        assertEquals("main", metric.getType());
        assertEquals("uptime", metric.getName());
        assertEquals(12334556.0, metric.getValue(), 0.0);
        assertNull(metric.getIdent());
        assertTrue(metric.isCounter());
        assertFalse(metric.isGauge());
        assertFalse(metric.isBitmap());
        assertEquals("child process uptime", metric.getDescription());
    }

    @Test
    public void TestVarnishVersion51_SimpleIdent() throws IOException, SAXException, ParserConfigurationException {
        Metric metric = getMetricFromDocument("<?xml version=\"1.0\"?>\n" +
                "<stat>\n" +
                "    <type>MEMPOOL</type>\n" +
                "    <ident>busyobj</ident>\n" +
                "    <name>live</name>\n" +
                "    <value>0</value>\n" +
                "    <flag>g</flag>\n" +
                "    <format>i</format>\n" +
                "    <description>In use</description>\n" +
                "</stat>");
        assertEquals("mempool", metric.getType());
        assertEquals("live", metric.getName());
        assertEquals(0, metric.getValue(), 0.0);
        assertEquals("busyobj", metric.getIdent());
        assertFalse(metric.isCounter());
        assertTrue(metric.isGauge());
        assertFalse(metric.isBitmap());
        assertEquals("in use", metric.getDescription());
    }

    @Test
    public void TestVarnishVersion51_MultiIdent() throws IOException, SAXException, ParserConfigurationException {
        Metric metric = getMetricFromDocument("<?xml version=\"1.0\"?>\n" +
                "<stat>\n" +
                "    <type>vbe</type>\n" +
                "    <ident>boot.default</ident>\n" +
                "    <name>bereq_hdrbytes</name>\n" +
                "    <value>234453</value>\n" +
                "    <flag>c</flag>\n" +
                "    <format>B</format>\n" +
                "    <description>Request header bytes</description>\n" +
                "</stat>");
        assertEquals("vbe", metric.getType());
        assertEquals("bereq_hdrbytes", metric.getName());
        assertEquals(234453, metric.getValue(), 0.0);
        assertEquals("boot.default", metric.getIdent());
        assertTrue(metric.isCounter());
        assertFalse(metric.isGauge());
        assertFalse(metric.isBitmap());
        assertEquals("request header bytes", metric.getDescription());
    }

    @Test
    public void TestVarnishVersion52_NoIdent() throws IOException, SAXException, ParserConfigurationException {
        Metric metric = getMetricFromDocument("<?xml version=\"1.0\"?>\n" +
                "<stat>\n" +
                "    <name>MAIN.uptime</name>\n" +
                "    <value>12334556</value>\n" +
                "    <flag>c</flag>\n" +
                "    <format>d</format>\n" +
                "    <description>Child process uptime</description>\n" +
                "</stat>");
        assertEquals("main", metric.getType());
        assertEquals("uptime", metric.getName());
        assertEquals(12334556.0, metric.getValue(), 0.0);
        assertNull(metric.getIdent());
        assertTrue(metric.isCounter());
        assertFalse(metric.isGauge());
        assertFalse(metric.isBitmap());
        assertEquals("child process uptime", metric.getDescription());
    }

    @Test
    public void TestVarnishVersion52_SimpleIdent() throws IOException, SAXException, ParserConfigurationException {
        Metric metric = getMetricFromDocument("<?xml version=\"1.0\"?>\n" +
                "<stat>\n" +
                "    <name>MEMPOOL.busyobj.live</name>\n" +
                "    <value>0</value>\n" +
                "    <flag>g</flag>\n" +
                "    <format>i</format>\n" +
                "    <description>In use</description>\n" +
                "</stat>");
        assertEquals("mempool", metric.getType());
        assertEquals("live", metric.getName());
        assertEquals(0, metric.getValue(), 0.0);
        assertEquals("busyobj", metric.getIdent());
        assertFalse(metric.isCounter());
        assertTrue(metric.isGauge());
        assertFalse(metric.isBitmap());
        assertEquals("in use", metric.getDescription());
    }

    @Test
    public void TestVarnishVersion52_MultiIdent() throws IOException, SAXException, ParserConfigurationException {
        Metric metric = getMetricFromDocument("<?xml version=\"1.0\"?>\n" +
                "<stat>\n" +
                "    <name>VBE.boot.default.bereq_hdrbytes</name>\n" +
                "    <value>234453</value>\n" +
                "    <flag>c</flag>\n" +
                "    <format>B</format>\n" +
                "    <description>Request header bytes</description>\n" +
                "</stat>");
        assertEquals("vbe", metric.getType());
        assertEquals("bereq_hdrbytes", metric.getName());
        assertEquals(234453, metric.getValue(), 0.0);
        assertEquals("boot.default", metric.getIdent());
        assertTrue(metric.isCounter());
        assertFalse(metric.isGauge());
        assertFalse(metric.isBitmap());
        assertEquals("request header bytes", metric.getDescription());
    }

    private Metric getMetricFromDocument(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(new InputSource(new StringReader(xml)));

        NodeList nlist = document.getElementsByTagName("stat");
        Element element = (Element) nlist.item(0);

        return new Metric(element);
    }
}