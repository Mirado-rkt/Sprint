@echo off
REM Suppression du dossier bin et du jar existant
if exist bin rd /s /q bin
if exist framework.jar del /f /q framework.jar

REM Création du dossier de sortie
mkdir bin

REM Compilation des sources Java
javac -source 17 -target 17 -cp "lib/jakarta.servlet-api-6.0.0.jar" -d bin src\framework\*.java src\com\framework\corps\*.java src\mg\itu\annotation\Controller\*.java src\mg\itu\annotation\Url\*.java src\mg\itu\annotation\Json\*.java src\mg\itu\Servlet\*.java src\mg\itu\utils\*.java src\mg\itu\framework\controller\*.java src\mg\itu\framework\exception\*.java src\mg\itu\framework\mapping\*.java

REM Création de l'archive JAR
jar cvf framework.jar -C bin .