package com.eastwood.tools.plugins.run

import com.eastwood.tools.plugins.run.extension.SingleRunConfig
import com.eastwood.tools.plugins.run.extension.SingleRunExtension
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.util.ConfigureUtil

class SingleRunSettingsPlugin implements Plugin<Settings> {

    private static String BUILD_TEMPLATE_PATH = "/build.tmpl"
    private static String ACTIVITY_TEMPLATE_PATH = "/SingleRunActivity.tmpl"
    private static String ANDROID_MANIFEST_TEMPLATE_PATH = "/AndroidManifest.tmpl"
    private static String ANDROID_MANIFEST_WITH_LAUNCHER_TEMPLATE_PATH = "/AndroidManifestWithLauncher.tmpl"

    Settings settings
    File projectDir

    void apply(Settings settings) {
        this.settings = settings
        projectDir = settings.rootProject.projectDir

        settings.metaClass.singleRun { Closure closure ->
            SingleRunExtension jumperExtension = new SingleRunExtension()
            ConfigureUtil.configure(closure, jumperExtension)

            def runDir = new File(projectDir, 'run')
            if (jumperExtension.runDir != null) {
                runDir = new File(projectDir, jumperExtension.runDir)
            }

            if (!runDir.exists()) {
                runDir.mkdir()
            }

            HashMap<String, SingleRunConfig> singleRunConfigs = jumperExtension.getSingleRunConfigs()
            singleRunConfigs.each {
                SingleRunConfig singleRunConfig = it.value
                def moduleName = it.key
                def moduleDir = new File(runDir, moduleName)
                if (!moduleDir.exists()) {
                    moduleDir.mkdir()
                }

                def buildFile = new File(moduleDir, 'build.gradle')
                InputStream inputStream = SingleRunSettingsPlugin.class.getResourceAsStream(BUILD_TEMPLATE_PATH);
                Scanner scanner = new Scanner(inputStream)
                String template = scanner.useDelimiter("\\A").next();
                String fileContent = template.replace("%APPLICATION_ID%", "singleRun." + moduleName)

                String extendBuild = null
                if (singleRunConfig.extendBuild != null) {
                    def buildScriptFile = new File(projectDir, singleRunConfig.extendBuild)
                    if (buildScriptFile.exists()) {
                        extendBuild = separatorsToUnix(buildScriptFile.canonicalFile.absolutePath)
                    }
                }

                if (extendBuild != null) {
                    fileContent = fileContent.replace("%EXTEND_BUILD%", "apply from: '" + extendBuild + "'")
                } else {
                    fileContent = fileContent.replace("%EXTEND_BUILD%", "")
                }

                buildFile.setText(fileContent)

                def androidManifestFile = new File(moduleDir, 'src/main/AndroidManifest.xml')
                androidManifestFile.getParentFile().mkdirs()
                inputStream = SingleRunSettingsPlugin.class.getResourceAsStream(
                        singleRunConfig.target != null ? ANDROID_MANIFEST_WITH_LAUNCHER_TEMPLATE_PATH : ANDROID_MANIFEST_TEMPLATE_PATH);
                scanner = new Scanner(inputStream)
                template = scanner.useDelimiter("\\A").next()
                String applicationReplaceHolder = "tools:replace=\"android:label\""
                if (singleRunConfig.applicationName != null) {
                    applicationReplaceHolder = "android:name=\"${singleRunConfig.applicationName}\"\n        tools:replace=\"android:name,android:label\""
                }

                String appName = moduleName
                if (singleRunConfig.appName != null) {
                    appName = singleRunConfig.appName
                }
                fileContent = template.replace("%APPLICATION_NAME%", applicationReplaceHolder)
                        .replace("%APP_NAME%", appName)
                androidManifestFile.setText(fileContent, 'utf-8')

                if (singleRunConfig.target != null) {
                    def singleRunActivityFile = new File(moduleDir, '/src/main/java/singleRun/SingleRunActivity.java')
                    singleRunActivityFile.getParentFile().mkdirs()

                    inputStream = SingleRunSettingsPlugin.class.getResourceAsStream(ACTIVITY_TEMPLATE_PATH);
                    scanner = new Scanner(inputStream)
                    template = scanner.useDelimiter("\\A").next()

                    String checkPermissions = "\n"
                    String item = "        if (checkSelfPermission(Manifest.permission.%s) != PackageManager.PERMISSION_GRANTED) {\n            return false;\n        }\n"
                    singleRunConfig.needPermissions.each {
                        checkPermissions += String.format(item, it)
                    }

                    String requestPermissions = '\n'
                    item = '                Manifest.permission.%s,\n'
                    singleRunConfig.needPermissions.each {
                        requestPermissions += String.format(item, it)
                    }

                    String bundle = '\n'
                    singleRunConfig.params.each {
                        if (it.value instanceof String) {
                            bundle += '        intent.putExtra("' + it.key + '", "' + it.value + '");\n'
                        } else if (it.value instanceof Float) {
                            bundle += '        intent.putExtra("' + it.key + '", ' + it.value + 'f);\n'
                        } else if (it.value instanceof Long) {
                            bundle += '        intent.putExtra("' + it.key + '", ' + it.value + 'L);\n'
                        } else if (it.value instanceof Double) {
                            bundle += '        intent.putExtra("' + it.key + '", ' + it.value + 'd);\n'
                        } else {
                            bundle += '        intent.putExtra("' + it.key + '", ' + it.value + ');\n'
                        }
                    }

                    fileContent = template
                            .replace("%REQUEST_PERMISSIONS%", requestPermissions)
                            .replace("%CHECK_PERMISSIONS%", checkPermissions)
                            .replace("%TARGET%", singleRunConfig.target)
                            .replace("%BUNDLE%", bundle)

                    singleRunActivityFile.setText(fileContent)
                }

                settings.include(":${moduleName}")
                settings.project(":${moduleName}").setProjectDir(new File(runDir, moduleName))
            }
        }

        def runScript = new File(settings.rootDir, 'singleRun.gradle')
        if (!runScript.exists()) {
            return
        }
        settings.apply from: 'singleRun.gradle'
    }

    String separatorsToUnix(String path) {
        return path != null && path.indexOf(92) != -1 ? path.replace('\\', '/') : path;
    }
}