$ErrorActionPreference = "Stop"

$projectRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$appImageDir = Join-Path $projectRoot "dist\HanyarNgopi"
$outputDir = Join-Path $projectRoot "dist-installer"
$workDir = Join-Path $projectRoot "target\sfx-installer"
$archivePath = Join-Path $workDir "HanyarNgopi-portable.zip"
$stubSource = Join-Path $PSScriptRoot "InstallerStub.cs"
$stubExe = Join-Path $workDir "installer-stub.exe"
$outputExe = Join-Path $outputDir "Manajemen HanyarNgopi-1.0.0.exe"
$iconPath = Join-Path $projectRoot "src\main\resources\com\pos\view\image\hanyarngopi.ico"
$csc = "C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe"
$marker = [System.Text.Encoding]::ASCII.GetBytes("__HANYAR_NGOPI_PAYLOAD_V1__")

if (!(Test-Path $appImageDir)) {
    throw "App image tidak ditemukan di $appImageDir"
}

if (!(Test-Path $csc)) {
    throw "Compiler C# tidak ditemukan di $csc"
}

if (Test-Path $workDir) {
    Remove-Item -LiteralPath $workDir -Recurse -Force
}

New-Item -ItemType Directory -Path $workDir | Out-Null

if (!(Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir | Out-Null
}

if (Test-Path $outputExe) {
    Remove-Item -LiteralPath $outputExe -Force
}

Compress-Archive -Path (Join-Path $appImageDir "*") -DestinationPath $archivePath -Force

$compileArgs = @(
    "/nologo",
    "/target:winexe",
    "/out:$stubExe",
    "/reference:System.Windows.Forms.dll",
    "/reference:System.IO.Compression.dll",
    "/reference:System.IO.Compression.FileSystem.dll",
    $stubSource
)

if (Test-Path $iconPath) {
    $compileArgs += "/win32icon:$iconPath"
}

& $csc @compileArgs
if ($LASTEXITCODE -ne 0) {
    throw "Compile installer stub gagal dengan exit code $LASTEXITCODE"
}

$stubBytes = [System.IO.File]::ReadAllBytes($stubExe)
$archiveBytes = [System.IO.File]::ReadAllBytes($archivePath)

$stream = [System.IO.File]::Open($outputExe, [System.IO.FileMode]::CreateNew, [System.IO.FileAccess]::Write)
try {
    $stream.Write($stubBytes, 0, $stubBytes.Length)
    $stream.Write($marker, 0, $marker.Length)
    $stream.Write($archiveBytes, 0, $archiveBytes.Length)
}
finally {
    $stream.Dispose()
}

if (!(Test-Path $outputExe)) {
    throw "Installer SFX tidak ditemukan di $outputExe"
}

Write-Host "Installer SFX berhasil dibuat di:"
Write-Host $outputExe
