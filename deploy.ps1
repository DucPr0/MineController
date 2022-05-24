# $ErrorActionPreference = "Stop"

mvn install

$currentDirectory = $pwd.Path

$pluginFileName = "MineController.jar"
$serverDirectory = "C:\Users\Duc Nguyen Huu\Desktop\server3\"
$pluginDirectory = "${serverDirectory}plugins"

rm "${serverDirectory}plugins\$pluginFileName"
mv ".\target\$pluginFileName" $pluginDirectory
cd "${serverDirectory}"

$serverProcess = Start-Process "${serverDirectory}run.bat" -PassThru
[Environment]::SetEnvironmentVariable("serverProcessId", $serverProcess.Id, "user")

cd $currentDirectory
