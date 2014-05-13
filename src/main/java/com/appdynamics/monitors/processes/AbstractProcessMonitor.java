package com.appdynamics.monitors.processes;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

abstract class AbstractProcessMonitor implements ProcessMonitor {
    private ProcessProperties properties;
    private ProcessMonitorContext context;
    protected Logger logger;

    public AbstractProcessMonitor(ProcessProperties properties, ProcessMonitorContext context) {
        this.properties = properties;
        this.context = context;
        this.logger = context.getLogger();
    }

    public ProcessMonitorContext getContext() {
        return context;
    }

    public ProcessProperties getProperties() {
        return properties;
    }

    @Override
    public ProcessMetrics call() throws Exception {
        return processMetrics();
    }

    protected List<String> execute(String command) throws ProcessMonitorException {
        return execute(new String[] {command});
    }

    protected List<String> execute(String[] commands) throws ProcessMonitorException {
        String command = Arrays.toString(commands);
        Process process = null;
        try {
            logger.debug("Executing command " + command);
            process = Runtime.getRuntime().exec(commands);
            process.waitFor();

            if (process.exitValue() == 0) {
                return readLines(process.getInputStream());
            }

            throw new ProcessMonitorException("Error encountered while executing command " + command + "\n"
                    + asString(readLines(process.getErrorStream())));

        } catch (InterruptedException e) {
            throw new ProcessMonitorException("Interrupted while processing " + command, e);
        } catch (IOException e) {
            throw new ProcessMonitorException("Failed to execute command " + command, e);
        } finally {
            if (process != null) {
                closeQuietly(process.getInputStream());
                closeQuietly(process.getErrorStream());
                closeQuietly(process.getOutputStream());
                process.destroy();
            }
        }
    }

    protected List<String> readLines(InputStream inputStream) {
        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader = null;
        List<String> processLines = new ArrayList<String>();
        try {
            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);
            String processLine;
            while ((processLine = bufferedReader.readLine()) != null) {
                processLines.add(processLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(bufferedReader);
            closeQuietly(inputStreamReader);
        }
        return processLines;
    }

    protected String asString(Collection<String> lines) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String line : lines) {
            stringBuilder.append(line).append("\n");
        }
        return stringBuilder.toString();
    }

    private void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignore) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Reader did not close quietly", ignore);
                }
            }
        }
    }
}
