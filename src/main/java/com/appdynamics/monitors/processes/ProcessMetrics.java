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

class ProcessMetrics {
    private String name;
    private int cpuPercent;
    private int memoryPercent;
    private int memoryAbsolute;
    private int numOfInstances;

    public ProcessMetrics() {
    }

    public ProcessMetrics(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCpuPercent() {
        return cpuPercent;
    }

    public ProcessMetrics addCpuPercent(int cpuPercent) {
        this.cpuPercent += cpuPercent;
        return this;
    }

    public ProcessMetrics addCpuPercent(String cpuPercent) {
        double cpuPercentAsDouble = Double.valueOf(cpuPercent);
        return addCpuPercent((int)cpuPercentAsDouble);
    }

    public int getMemoryPercent() {
        return memoryPercent;
    }

    public ProcessMetrics addMemoryPercent(int memoryPercent) {
        this.memoryPercent += memoryPercent;
        return this;
    }

    public ProcessMetrics addMemoryPercent(String memoryPercent) {
        double memoryPercentAsDouble = Double.valueOf(memoryPercent);
        return addMemoryPercent((int)memoryPercentAsDouble);
    }

    public int getMemoryAbsolute() {
        return memoryAbsolute;
    }

    public ProcessMetrics addMemoryAbsolute(int memoryAbsolute) {
        this.memoryAbsolute += memoryAbsolute;
        return this;
    }

    public ProcessMetrics addMemoryAbsolute(String memoryAbsolute) {
        double memoryAbsoluteAsDouble = Double.valueOf(memoryAbsolute);
        return addMemoryAbsolute((int)memoryAbsoluteAsDouble);
    }

    public int getNumOfInstances() {
        return numOfInstances;
    }

    public ProcessMetrics incrementNumOfInstances() {
        this.numOfInstances++;
        return this;
    }

    public void clear() {
        cpuPercent = 0;
        memoryPercent = 0;
        memoryAbsolute = 0;
        numOfInstances = 0;
    }

    @Override
    public String toString() {
        return "ProcessMetrics{" +
                "name='" + name + '\'' +
                ", cpuPercent=" + cpuPercent +
                ", memoryPercent=" + memoryPercent +
                ", memoryAbsolute=" + memoryAbsolute +
                ", numOfInstances=" + numOfInstances +
                '}';
    }
}
