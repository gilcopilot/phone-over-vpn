@echo off

set DIR=%~dp0

set GRADLE_EXIT_CONSOLE=

if "%JAVA_HOME%"=="" (
    set JAVA_EXE=java
) else (
    set JAVA_EXE="%JAVA_HOME%\bin\java.exe"
)

set CLASSPATH=%DIR%\gradle\wrapper\gradle-wrapper.jar

%JAVA_EXE% -Dorg.gradle.appname=%DIR% -classpath %CLASSPATH% org.gradle.wrapper.GradleWrapperMain %*
