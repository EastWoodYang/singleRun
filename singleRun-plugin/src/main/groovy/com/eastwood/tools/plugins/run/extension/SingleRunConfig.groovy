package com.eastwood.tools.plugins.run.extension

class SingleRunConfig {

    String target
    String[] modules
    Map<String, Object> bundle
    String applicationName

    void target(String target) {
        this.target = target
    }

    void modules(String modules) {
        this.modules = [modules]
    }

    void modules(String[] modules) {
        this.modules = modules
    }

    void bundle(Map<String, Object> args) {
        this.bundle = args
    }

    void applicationName(String name) {
        this.applicationName = name
    }

    def methodMissing(String name, def args) {

    }

    def propertyMissing(String name, def arg) {

    }

}