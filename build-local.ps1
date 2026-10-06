param(
    [string[]]$Tasks = @('clean', 'build'),
    [string]$JdkHome = '',
    [string]$GradleUserHome = ''
)
$ErrorActionPreference = 'Stop'
function IsJava21([string]$HomePath) {
    if (!$HomePath -or !(Test-Path "$HomePath\bin\java.exe") -or !(Test-Path "$HomePath\release")) { return $false }
    return [bool]((Get-Content "$HomePath\release") -match '^JAVA_VERSION="21[.\"]')
}
if ($JdkHome -and !(IsJava21 $JdkHome)) { throw 'The specified JDK must be Java 21.' }
if (!$JdkHome -and (IsJava21 $env:JAVA_HOME)) { $JdkHome = $env:JAVA_HOME }
if (!$JdkHome) {
    $tools = Join-Path $PSScriptRoot '.tools\jdk21-zulu'
    if (Test-Path $tools) {
        $JdkHome = Get-ChildItem $tools -Directory | Where-Object { IsJava21 $_.FullName } | Select-Object -First 1 -ExpandProperty FullName
    }
}
if (!$JdkHome) { throw 'Java 21 not found. Run: .\build-local.ps1 -JdkHome C:\path\	o\jdk-21' }
if (!$GradleUserHome) {
    if ($env:GRADLE_USER_HOME) { $GradleUserHome = $env:GRADLE_USER_HOME }
    elseif (Test-Path 'F:\.gradle') { $GradleUserHome = 'F:\.gradle' }
    else { $GradleUserHome = Join-Path $env:USERPROFILE '.gradle' }
}
$oldJava = $env:JAVA_HOME; $oldPath = $env:Path; $oldGradle = $env:GRADLE_USER_HOME
$code = 1
Push-Location $PSScriptRoot
try {
    $env:JAVA_HOME = $JdkHome
    $env:Path = "$JdkHome\bin;$oldPath"
    $env:GRADLE_USER_HOME = $GradleUserHome
    Write-Host "Java 21: $JdkHome"
    Write-Host "Gradle cache: $GradleUserHome"
    & .\gradlew.bat --no-daemon --console=plain @Tasks
    $code = $LASTEXITCODE
}
finally {
    Pop-Location
    $env:JAVA_HOME = $oldJava; $env:Path = $oldPath; $env:GRADLE_USER_HOME = $oldGradle
}
exit $code