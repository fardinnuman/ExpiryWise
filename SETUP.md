ExpiryWise - Prerequisites & Quick Start
=========================================

Required:
- Windows 10/11
- JDK 21 LTS
- Apache Maven 3.9+
- Git
- Visual Studio Code

AUTOMATED SETUP
---------------
For Windows, run:

setup-prerequisites.bat

The script checks for Java 21 and Maven, installs missing prerequisites
using Windows Package Manager (winget), and can launch ExpiryWise.

MANUAL SETUP
------------
If you prefer to install the prerequisites manually:

JDK 21:
https://learn.microsoft.com/en-us/java/openjdk/download#openjdk-21

Apache Maven:
https://maven.apache.org/download.cgi

After installation, verify:

java -version
mvn -version

RUN EXPIRYWISE
--------------
Open a terminal in the project folder and run:

mvn clean javafx:run

BUILD THE PROJECT
-----------------
To compile and package the project:

mvn clean package

VS CODE
-------
If using Visual Studio Code, install:

- Extension Pack for Java
- Maven for Java