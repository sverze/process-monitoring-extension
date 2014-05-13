package com.appdynamics.monitors.processes;

import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ProcessMonitorContext {
    public static final String DEFAULT_METRIC_PATH = "Custom Metrics";
    public static final int DEFAULT_PROCESS_SAMPLE_TIME = 1;
    public static final int DEFAULT_PROCESS_IDENTITY_CHECK_INTERVAL = 60;
    private Map<ProcessProperties, ProcessMonitor> processMonitors;
    private AManagedMonitor managedMonitor;
    private OperatingSystem operatingSystem;
    private String propertiesPath;
    private String metricPath;
    private int processIdentityCheckInterval;
    private int processSampleInterval;
    private Logger logger;

    public ProcessMonitorContext() {
        processMonitors = new HashMap<ProcessProperties, ProcessMonitor>();
        metricPath = DEFAULT_METRIC_PATH;
        processIdentityCheckInterval = DEFAULT_PROCESS_IDENTITY_CHECK_INTERVAL;
        processSampleInterval = DEFAULT_PROCESS_SAMPLE_TIME;
    }

    public Collection<ProcessMonitor> getProcessMonitors() {
        return processMonitors.values();
    }

    public ProcessMonitor getProcessMonitor(ProcessProperties processProperties) {
        return processMonitors.get(processProperties);
    }

    public void addProcessMonitors(ProcessProperties properties, ProcessMonitor monitor) {
        processMonitors.put(properties, monitor);
    }

    public AManagedMonitor getManagedMonitor() {
        return managedMonitor;
    }

    public void setManagedMonitor(AManagedMonitor managedMonitor) {
        this.managedMonitor = managedMonitor;
    }

    public OperatingSystem getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(OperatingSystem operatingSystem) {
        if (operatingSystem == null) {
            throw new IllegalArgumentException("Invalid Operating System");
        }
        this.operatingSystem = operatingSystem;
    }

    public String getPropertiesPath() {
        return propertiesPath;
    }

    public void setPropertiesPath(String propertiesPath) {
        if (propertiesPath == null || propertiesPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid Properties Path: " + propertiesPath);
        }
        this.propertiesPath = propertiesPath;
    }

    public String getMetricPath() {
        return metricPath;
    }

    public void setMetricPath(String metricPath) {
        if (metricPath == null || metricPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid Metric Path: " + metricPath);
        }

        this.metricPath = metricPath.trim();
    }

    public int getProcessIdentityCheckInterval() {
        return processIdentityCheckInterval;
    }

    public void setProcessIdentityCheckInterval(int processIdentityCheckInterval) {
        if (processIdentityCheckInterval <= 0 || processIdentityCheckInterval >= 1000) {
            throw new IllegalArgumentException("Invalid Process Identity Check Interval: " + processIdentityCheckInterval);
        }
        this.processIdentityCheckInterval = processIdentityCheckInterval;
    }

    public void setProcessIdentityCheckInterval(String processIdentityCheckInterval) {
        setProcessIdentityCheckInterval(Integer.parseInt(processIdentityCheckInterval));
    }

    public int getProcessSampleInterval() {
        return processSampleInterval;
    }

    public void setProcessSampleInterval(int processSampleInterval) {
        if (processSampleInterval <= 0 || processSampleInterval >= 1000) {
            throw new IllegalArgumentException("Invalid Process Sample Interval: " + processSampleInterval);
        }
        this.processSampleInterval = processSampleInterval;
    }

    public void setProcessSampleInterval(String processSampleInterval) {
        setProcessSampleInterval(Integer.parseInt(processSampleInterval));
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public enum OperatingSystem {
        Linux("Linux Processes"),
        Windows("Windows Processes");

        private String processGroupName;

        OperatingSystem(String processGroupName) {
            this.processGroupName = processGroupName;
        }

        public String getProcessGroupName() {
            return processGroupName;
        }

        public static OperatingSystem getOperatingSystem() {
            OperatingSystem operatingSystem = null;
            String os = System.getProperty("os.name").toLowerCase();
            // TODO: refine the supported O/S e.g. Red Hat, Win 2008
            if (os.contains("linux")) {
                operatingSystem = Linux;
            } else if (os.contains("win")) {
                operatingSystem = Windows;
            }
            return operatingSystem;
        }
    }

    @Override
    public String toString() {
        return "ProcessMonitorContext{" +
                "processMonitors=" + processMonitors.size() +
                ", managedMonitor=" + managedMonitor +
                ", operatingSystem=" + operatingSystem +
                ", propertiesPath='" + propertiesPath + '\'' +
                ", metricPath='" + metricPath + '\'' +
                ", processSampleInterval=" + processSampleInterval +
                ", processIdentityCheckInterval=" + processIdentityCheckInterval +
                '}';
    }
}
