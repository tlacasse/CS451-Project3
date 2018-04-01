Param(
	[string]$env = "dev"
)

$base = [string]$pwd
$path = $base + '\_Build'
if (-not (Test-Path ($path))){
	New-Item -Path $path -ItemType directory
}
if (-not (Test-Path ($path + '\Scripts'))){
	New-Item -Path ($path + '\Scripts') -ItemType directory
}
if (-not (Test-Path ($path + '\Stuff'))){
	New-Item -Path ($path + '\Stuff') -ItemType directory
}
if (-not (Test-Path ($path + '\favicon.ico'))){
	Copy-Item ($base + '\favicon.ico') $path
}

Write-Host '-----------------------------' -ForegroundColor Green
Write-Host 'Removing old files.' -ForegroundColor Green
Write-Host '-----------------------------' -ForegroundColor Green
$files = Get-ChildItem -Path $path -Recurse -exclude favicon.ico
$fileToDelete = @()
foreach($file in $files){
	$ignore = $false
	$ignore = $ignore -or ($file.FullName -like ($path + '\Stuff*'))
	$ignore = $ignore -or ($file.FullName -like ($path + '\Scripts'))
	$ignore = $ignore -or ($file.FullName -like ($path + '\bin*'))
	$ignore = $ignore -or ($file.FullName -like ($path + '*.config'))
	$ignore = $ignore -or ($file.FullName -like ($path + '*.asax*'))
	if (-not $ignore){
		$fileToDelete += $file
	}
}
$fileToDelete | Select -ExpandProperty FullName | sort length -Descending | Remove-Item -Verbose

Write-Host '-----------------------------' -ForegroundColor Green
Write-Host 'Building CSproject.' -ForegroundColor Green
Write-Host '-----------------------------' -ForegroundColor Green
$project = $base + '\TicTacToe.csproj'
$msBuildExe = 'C:\Program Files (x86)\MSBuild\14.0\Bin\msbuild.exe'
& "$($msBuildExe)" "$($project)" /t:Build /m

Write-Host '-----------------------------' -ForegroundColor Green
Write-Host 'Gulp build.' -ForegroundColor Green
Write-Host '-----------------------------' -ForegroundColor Green

$gulppath = 'C:\Users\' + $env:UserName + '\AppData\Roaming\npm\gulp.cmd'
& $gulppath --env $env

Write-Host '-----------------------------' -ForegroundColor Green
Write-Host '===FINISHED BUILD===' -ForegroundColor Green

$readVersion = [IO.File]::ReadAllText($base + '\_Build\Stuff\version.txt')
$split = $readVersion.split('.')
$writeVersion = $split[0] + '.' + (([int]$split[1])+1)
$writeVersion | Out-File ($base + '\_Build\Stuff\version.txt')
Write-Host ('= ' + $writeVersion) -ForegroundColor Green
