@if not exist "%JAVA_HOME%" set JAVA_HOME=..\..\..\_studio\3rdparty\jdk1.7.0_11
if not exist "%JAVA_HOME%" (
	echo "Please set the JAVA_HOME environment variable"
) ELSE (
    	"%JAVA_HOME%\bin\javac" -classpath PXCUPipeline.jar *.java
	"%JAVA_HOME%\bin\java" -classpath PXCUPipeline.jar;. DepthCamLogger
)



