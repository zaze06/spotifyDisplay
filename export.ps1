if (!([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")) { Start-Process powershell.exe "-NoProfile -ExecutionPolicy Bypass -File `"$PSCommandPath`"" -Verb RunAs; exit }

D:
cd D:\IdeaProjects\spotifyDisplay

./gradlew shadowJar

rm win -Recurse -Force
mkdir win
java -jar ./packr-all-4.0.0.jar config-win.json
copy win/* "C:\Program Files\spotifyDisplay" -Force -Recurse

exit