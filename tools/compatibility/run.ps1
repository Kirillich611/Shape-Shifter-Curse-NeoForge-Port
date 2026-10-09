param(
    [string]$ServerRoot = '',
    [string]$JavaHome = 'C:/Program Files/Java/jdk-21.0.12.1',
    [string]$ClientJar = 'C:/Apps/Modrinth App/meta/libraries/net/neoforged/neoforge/21.1.248/neoforge-21.1.248-client.jar'
)

$ErrorActionPreference = 'Stop'
$portRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
if (!$ServerRoot) { $ServerRoot = Join-Path $portRoot 'run/original-parity-neoforge2' }
$serverPath = (Resolve-Path -LiteralPath $ServerRoot).Path
$runPath = [IO.Path]::GetFullPath((Join-Path $portRoot 'run')) + [IO.Path]::DirectorySeparatorChar
if (!$serverPath.StartsWith($runPath, [StringComparison]::OrdinalIgnoreCase)) {
    throw 'Use an isolated server inside this repository''s run directory.'
}
$properties = Get-Content -LiteralPath (Join-Path $serverPath 'server.properties') -Raw
if ($properties -notmatch '(?m)^server-ip=127\.0\.0\.1\r?$' -or
    $properties -notmatch '(?m)^level-name=parity-world\r?$') {
    throw 'This harness must use the localhost parity-world test server.'
}
$launchArgs = 'libraries/net/neoforged/neoforge/21.1.248/win_args.txt'
foreach ($required in @($ClientJar, (Join-Path $serverPath $launchArgs), (Join-Path $JavaHome 'bin/javac.exe'))) {
    if (!(Test-Path -LiteralPath $required)) { throw "Missing prerequisite: $required" }
}

$utf8 = [Text.UTF8Encoding]::new($false)
$resources = Join-Path $serverPath 'test-resources'
$classes = Join-Path $serverPath 'test-classes'
New-Item -ItemType Directory -Force $resources, $classes | Out-Null
Copy-Item (Join-Path $PSScriptRoot 'resources/*') $resources -Recurse -Force
$data = Join-Path $portRoot 'src/main/resources/data/shape-shifter-curse'
$powers = @(Get-ChildItem (Join-Path $data 'powers') -Filter '*.json' -File -Recurse | ForEach-Object {
    'shape-shifter-curse:' + $_.FullName.Substring((Join-Path $data 'powers').Length + 1).Replace('\', '/').Replace('.json', '')
})
[IO.File]::WriteAllText((Join-Path $resources 'expected-powers.txt'), ($powers -join "`n"), $utf8)
Copy-Item (Join-Path $data 'origins/form_bat_2.json') (Join-Path $resources 'bat2.json') -Force
foreach ($mob in @('spider', 'wolf')) {
    Copy-Item (Join-Path $data "loot_table/entities/t_$mob.json") (Join-Path $resources "loot-$mob.json") -Force
}
$classpath = @($ClientJar) + @(Get-ChildItem (Join-Path $serverPath 'libraries') -Filter '*.jar' -Recurse | ForEach-Object { $_.FullName })
$sources = @(Get-ChildItem (Join-Path $PSScriptRoot 'src') -Filter '*.java' -Recurse | ForEach-Object { $_.FullName })
$compilerArgs = @('-proc:none', '-encoding', 'UTF-8', '-classpath',
    ('"' + (($classpath -join ';').Replace('\', '/')) + '"'), '-d',
    ('"' + $classes.Replace('\', '/') + '"')) + @($sources | ForEach-Object { '"' + $_.Replace('\', '/') + '"' })
$argsFile = Join-Path $serverPath 'compile-args.txt'
[IO.File]::WriteAllLines($argsFile, $compilerArgs, $utf8)
$compileLog = Join-Path $serverPath 'test-compile.log'
# PowerShell 5.1 turns successful javac stderr notes into NativeCommandError.
$ErrorActionPreference = 'Continue'
try {
    & (Join-Path $JavaHome 'bin/javac.exe') ('@' + $argsFile) *> $compileLog
    $compileExit = $LASTEXITCODE
} finally { $ErrorActionPreference = 'Stop' }
if ($compileExit -ne 0) { throw "Test harness compilation failed; inspect $compileLog" }
& (Join-Path $JavaHome 'bin/jar.exe') --create --file (Join-Path $serverPath 'mods/compatibility-smoke.jar') -C $classes . -C $resources .
if ($LASTEXITCODE -ne 0) { throw 'Test harness packaging failed.' }

$modVersion = ((Get-Content (Join-Path $portRoot 'gradle.properties') | Select-String '^mod_version=').Line -split '=', 2)[1].Trim()
$artifact = Join-Path $portRoot "build/libs/shape-shifter-curse-connector-$modVersion+1.21.1.jar"
if (!(Test-Path -LiteralPath $artifact)) { throw "Build first: $artifact" }
$existing = @(Get-ChildItem (Join-Path $serverPath 'mods') -Filter 'shape-shifter-curse-connector-*.jar')
if ($existing.Count -gt 1 -or ($existing.Count -eq 1 -and $existing[0].Name -ne [IO.Path]::GetFileName($artifact))) {
    throw 'Move previous mod versions outside the isolated mods directory before testing.'
}
Copy-Item -LiteralPath $artifact (Join-Path $serverPath 'mods') -Force
$log = Join-Path $serverPath 'smoke-console.log'
Push-Location $serverPath
$ErrorActionPreference = 'Continue'
try {
    & (Join-Path $JavaHome 'bin/java.exe') -Xmx2G ('@' + $launchArgs) nogui *> $log
    $serverExit = $LASTEXITCODE
} finally { Pop-Location; $ErrorActionPreference = 'Stop' }
$result = Get-Content -LiteralPath $log -Raw
if ($serverExit -ne 0 -or $result -match 'COMPATIBILITY_SMOKE_FAILED' -or $result -notmatch 'COMPATIBILITY_SMOKE_PASSED: [^\r\n]+') {
    throw "Native compatibility checks failed; inspect $log"
}
$Matches[0]
exit 0
