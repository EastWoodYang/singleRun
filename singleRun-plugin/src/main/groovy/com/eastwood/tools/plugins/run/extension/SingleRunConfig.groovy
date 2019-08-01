package com.eastwood.tools.plugins.run.extension

class SingleRunConfig {

    String target
    String[] modules
    Map<String, Object> params
    String applicationName
    String appName
    String extendBuild
    String[] needPermissions

    void appName(String appName) {
        this.appName = appName
    }

    void target(String target) {
        this.target = target
    }

    void modules(String modules) {
        this.modules = [modules]
    }

    void modules(String[] modules) {
        this.modules = modules
    }

    void params(Map<String, Object> params) {
        this.params = params
    }

    void applicationName(String name) {
        this.applicationName = name
    }

    void extendBuild(String filePath) {
        this.extendBuild = filePath
    }

    void needPermissions(String[] needPermissions) {
        this.needPermissions = needPermissions
    }

    def methodMissing(String name, def args) {

    }

    def propertyMissing(String name, def arg) {

    }

}