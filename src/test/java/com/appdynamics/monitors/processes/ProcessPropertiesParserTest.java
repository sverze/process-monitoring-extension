package com.appdynamics.monitors.processes;

import org.dom4j.DocumentException;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class ProcessPropertiesParserTest {
    private ProcessPropertiesParser processPropertiesParser;

    @Before
    public void before() {
        processPropertiesParser = new ProcessPropertiesParser();
    }

    @Test(expected = DocumentException.class)
    public void parseXmlThrowsExceptionWhenEmpty() throws DocumentException {
        // arrange
        StringReader processXmlReader = new StringReader("");

        // act
        processPropertiesParser.parseXML(processXmlReader);
    }

    @Test
    public void parseXmlShouldHaveNoProcesses() throws DocumentException {
        // arrange
        String processXml = "<process-monitor></process-monitor>";
        StringReader processXmlReader = new StringReader(processXml);

        // act
        Collection<ProcessProperties> properties = processPropertiesParser.parseXML(processXmlReader);

        // assert
        assertThat(properties.isEmpty(), is(true));
    }

    @Test
    public void parseXmlShouldCreateProcess() throws DocumentException {
        // arrange
    String processXml =
            "<process-monitor>" +
            "    <process>" +
            "        <name>Test Process</name>" +
            "        <command>/test/process arg1</command>" +
            "    </process>" +
            "</process-monitor>";
        StringReader processXmlReader = new StringReader(processXml);

        // act
        Collection<ProcessProperties> properties = processPropertiesParser.parseXML(processXmlReader);

        // assert
        assertThat(properties.isEmpty(), is(false));
        assertThat(properties.size(), is(1));
        ProcessProperties processProperties = properties.iterator().next();
        assertThat(processProperties, notNullValue());
        assertThat(processProperties.getName(), is("Test Process"));
        assertThat(processProperties.getCommand(), notNullValue());
        assertThat(processProperties.getCommand().toString(), is("/test/process arg1"));
    }

    @Test
    public void parseXmlShouldCreateManyProcesses() throws DocumentException {
        // arrange
        String processXml =
            "<process-monitor>" +
            "    <process>" +
            "        <name>Test Process 1</name>" +
            "        <command>/test/process arg1</command>" +
            "    </process>" +
            "    <process>" +
            "        <name>Test Process 2</name>" +
            "        <command>/test/process arg1 arg2.*</command>" +
            "    </process>" +
            "</process-monitor>";
        StringReader processXmlReader = new StringReader(processXml);

        // act
        Collection<ProcessProperties> properties = processPropertiesParser.parseXML(processXmlReader);

        // assert
        assertThat(properties.isEmpty(), is(false));
        assertThat(properties.size(), is(2));
        Iterator<ProcessProperties> propertiesIterator = properties.iterator();
        ProcessProperties processProperties = propertiesIterator.next();
        assertThat(processProperties, notNullValue());
        assertThat(processProperties.getName(), is("Test Process 1"));
        assertThat(processProperties.getCommand(), notNullValue());
        assertThat(processProperties.getCommand().toString(), is("/test/process arg1"));
        processProperties = propertiesIterator.next();
        assertThat(processProperties, notNullValue());
        assertThat(processProperties.getName(), is("Test Process 2"));
        assertThat(processProperties.getCommand(), notNullValue());
        assertThat(processProperties.getCommand().toString(), is("/test/process arg1 arg2.*"));
    }

    @Test(expected = DocumentException.class)
    public void parseXmlShouldThrowExceptionForDuplicates() throws DocumentException {
        // arrange
        String processXml =
            "<process-monitor>" +
            "    <process>" +
            "        <name>Test Process 1</name>" +
            "        <command>/test/process arg1</command>" +
            "    </process>" +
            "    <process>" +
            "        <name>Test Process 1</name>" +
            "        <command>/test/process arg1</command>" +
            "    </process>" +
            "</process-monitor>";
        StringReader processXmlReader = new StringReader(processXml);

        // act
        processPropertiesParser.parseXML(processXmlReader);
    }
}
