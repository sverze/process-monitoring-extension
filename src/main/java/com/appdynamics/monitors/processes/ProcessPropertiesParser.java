/**
 * Copyright 2013 AppDynamics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.appdynamics.monitors.processes;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

class ProcessPropertiesParser {
    public List<ProcessProperties> parseXML(String fileName) throws DocumentException, FileNotFoundException {
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(fileName);
            return parseXML(fileReader);
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    public List<ProcessProperties> parseXML(Reader xmlReader) throws DocumentException {
        List<ProcessProperties> processes = new ArrayList<ProcessProperties>();
        SAXReader reader = new SAXReader();
        Document document = reader.read(xmlReader);
        Element root = document.getRootElement();

        for (Iterator<Element> rootIterator = root.elementIterator(); rootIterator.hasNext(); ) {
            Element element = rootIterator.next();

            if (element.getName().equals("process")) {
                ProcessProperties processProperties = new ProcessProperties();
                for (Iterator<Element> elementIterator = element.elementIterator(); elementIterator.hasNext(); ) {
                    Element processElement = elementIterator.next();
                    if (processElement.getName().equals("command")) {
                        processProperties.setCommand(Pattern.compile(processElement.getText()));
                    } else if (processElement.getName().equals("name")) {
                        processProperties.setName(processElement.getText());
                    }
                }

                if (!processProperties.isValid()) {
                    throw new DocumentException("Found an invalid process element in the properties: " + processProperties);
                }

                if (processes.contains(processProperties)) {
                    throw new DocumentException("Duplicate process description found in the properties: " + processProperties);
                }

                processes.add(processProperties);
            }
        }
        return processes;
    }

    public List<ProcessProperties> parseJSON(String jsonString) throws DocumentException {
        List<ProcessProperties> processes = new ArrayList<ProcessProperties>();
//        JsonParser parser = JsonParserFactory.createParser(new StringReader(jsonString));
        return processes;
    }
}
