package com.eastwood.tools.plugins.run.extension

class SingleRunConfig {

    String target
    String[] modules

    void target(String target) {
        this.target = target
    }

    void modules(String modules) {
        this.modules = [modules]
    }

    void modules(String[] modules) {
        this.modules = modules
    }

    def methodMissing(String name, def args) {

    }

    def propertyMissing(String name, def arg) {

    }

}