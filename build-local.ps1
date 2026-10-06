param(
    [string[]]$Tasks = @('clean', 'build', 'genIntellijRuns'),
    [string]$JdkHome = '',
    [string]$GradleUserHome = ''
)
$ErrorActionPreference = 'Stop'
function IsJava17([string]$HomePath) {
    if (!$HomePath -or !(Test-Path "$HomePath\bin\java.exe") -or !(Test-Path "$HomePath\release")) { return $false }
    return [bool]((Get-Content "$HomePath\release") -match '^JAVA_VERSION="17[.\"]')
}
if ($JdkHome -and !(IsJava17 $JdkHome)) { throw 'The specified JDK must be Java 17.' }
if (!$JdkHome -and (IsJava17 $env:JAVA_HOME)) { $JdkHome = $env:JAVA_HOME }
if (!$JdkHome) {
    $tools = Join-Path $PSScriptRoot '.tools\jdk17-zulu'
    if (Test-Path $tools) {
        $JdkHome = Get-ChildItem $tools -Directory | Where-Object { IsJava17 $_.FullName } | Select-Object -First 1 -ExpandProperty FullName
    }
}
if (!$JdkHome) { throw 'Java 17 not found. Run: .\build-local.ps1 -JdkHome C:\path\to\jdk-17' }
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
    Write-Host "Java 17: $JdkHome"
    Write-Host "Gradle cache: $GradleUserHome"
    & .\gradlew.bat --no-daemon --console=plain @Tasks
    $code = $LASTEXITCODE
}
finally {
    Pop-Location
    $env:JAVA_HOME = $oldJava; $env:Path = $oldPath; $env:GRADLE_USER_HOME = $oldGradle
}
exit $code
