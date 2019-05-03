package com.eastwood.tools.plugins.run.extension

import org.gradle.util.ConfigureUtil

class SingleRunExtension {

    private Map<String, SingleRunConfig> singleRunConfigs = new HashMap<>()

    Map<String, SingleRunConfig> getSingleRunConfigs() {
        return singleRunConfigs
    }

    String runDir

    void runDir(String runDir) {
        this.runDir = runDir
    }

    def methodMissing(String name, def args) {
        if (args[0] instanceof Closure) {
            SingleRunConfig singleRunConfig = new SingleRunConfig()
            ConfigureUtil.configure(args[0], singleRunConfig)
            singleRunConfigs.put(name, singleRunConfig)
        }
    }

    def propertyMissing(String name, def arg) {

    }

}