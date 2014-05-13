package com.appdynamics.monitors.processes;

import com.appdynamics.monitors.processes.ProcessMonitorException;

import java.util.concurrent.Callable;

public interface ProcessMonitor extends Callable<ProcessMetrics> {
    public void initialize() throws ProcessMonitorException;

    public ProcessMetrics processMetrics() throws ProcessMonitorException;
}
