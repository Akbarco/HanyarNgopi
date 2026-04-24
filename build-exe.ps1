$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

$jdkPath = "C:\Program Files\Java\jdk-22"
$javaHomeBin = Join-Path $jdkPath "bin"
$jpackageExe = Join-Path $javaHomeBin "jpackage.exe"
$iconPath = "src\main\resources\com\pos\view\image\hanyarngopi.ico"
$localWixPath = "tools\wix"
$packageInput = "target\package-input"
$distDir = "dist"
$installerDir = "dist-installer"
$mainJar = "MyApp-1.0-SNAPSHOT.jar"
$appName = "HanyarNgopi"
$displayName = "Manajemen HanyarNgopi"
$appVersion = "1.0.0"
$appDescription = "Manajemen HanyarNgopi"

if (!(Test-Path $jdkPath)) {
    throw "JDK 22 tidak ditemukan di $jdkPath"
}

if (!(Test-Path $jpackageExe)) {
    throw "jpackage tidak ditemukan di $jpackageExe"
}

if (!(Test-Path $iconPath)) {
    throw "Icon tidak ditemukan di $iconPath"
}

$env:JAVA_HOME = $jdkPath
$env:Path = "$javaHomeBin;$env:Path"

if (Test-Path (Join-Path $localWixPath "candle.exe")) {
    $env:Path = "$((Resolve-Path $localWixPath).Path);$env:Path"
}

Write-Host "==> Build Maven package dengan JDK 22"
& .\mvnw.cmd "-q" "-Dmaven.test.skip=true" "package"

Write-Host "==> Salin dependency runtime"
if (Test-Path $packageInput) {
    Remove-Item -LiteralPath $packageInput -Recurse -Force
}
New-Item -ItemType Directory -Path $packageInput | Out-Null

& .\mvnw.cmd "-q" "dependency:copy-dependencies" "-DincludeScope=runtime" "-DoutputDirectory=$packageInput\lib"

Copy-Item -LiteralPath "target\$mainJar" -Destination $packageInput -Force
Get-ChildItem -Path "$packageInput\lib\*.jar" | ForEach-Object {
    Copy-Item -LiteralPath $_.FullName -Destination $packageInput -Force
}

Write-Host "==> Generate Windows app image"
if (Test-Path $distDir) {
    Remove-Item -LiteralPath $distDir -Recurse -Force
}

& $jpackageExe `
    --type app-image `
    --input $packageInput `
    --name $appName `
    --main-jar $mainJar `
    --main-class com.pos.MainApp `
    --dest $distDir `
    --module-path "$jdkPath\jmods;$packageInput" `
    --add-modules javafx.controls,javafx.fxml,java.sql `
    --icon $iconPath `
    --vendor Akbarco `
    --app-version $appVersion `
    --description $appDescription

$appExe = Join-Path $distDir "$appName\$appName.exe"
if (Test-Path $appExe) {
    Write-Host ""
    Write-Host "Launcher EXE berhasil dibuat:"
    Write-Host $appExe
} else {
    throw "Launcher EXE tidak ditemukan setelah jpackage app-image."
}

$hasWix = $null -ne (Get-Command candle.exe -ErrorAction SilentlyContinue) -and
          $null -ne (Get-Command light.exe -ErrorAction SilentlyContinue)

if ($hasWix) {
    Write-Host "==> Generate installer EXE"
    if (Test-Path $installerDir) {
        Remove-Item -LiteralPath $installerDir -Recurse -Force
    }

    & $jpackageExe `
        --type exe `
        --name $appName `
        --app-image (Join-Path $distDir $appName) `
        --dest $installerDir `
        --vendor Akbarco `
        --app-version $appVersion `
        --icon $iconPath `
        --description $appDescription `
        --win-shortcut `
        --win-menu `
        --win-dir-chooser

    $defaultInstaller = Join-Path $installerDir "$appName-$appVersion.exe"
    $renamedInstaller = Join-Path $installerDir "$displayName-$appVersion.exe"
    if (Test-Path $defaultInstaller) {
        if (Test-Path $renamedInstaller) {
            Remove-Item -LiteralPath $renamedInstaller -Force
        }
        Rename-Item -LiteralPath $defaultInstaller -NewName "$displayName-$appVersion.exe"
    }

    Write-Host ""
    Write-Host "Installer EXE berhasil dibuat di:"
    Write-Host $renamedInstaller
} else {
    Write-Host ""
    Write-Warning "WiX Toolset belum terpasang. Launcher EXE sudah jadi, tapi installer EXE satu file belum bisa dibuat."
    Write-Host "Pasang WiX Toolset 3.x/4.x, lalu jalankan ulang script ini untuk menghasilkan installer."
}
