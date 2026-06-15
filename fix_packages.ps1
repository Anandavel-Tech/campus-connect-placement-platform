$Utf8NoBomEncoding = New-Object System.Text.UTF8Encoding $False

$dirsToFix = @(
    @{ Path = "src\service"; Pkg = "package service;" },
    @{ Path = "src\server"; Pkg = "package server;" },
    @{ Path = "src\storage"; Pkg = "package storage;" },
    @{ Path = "src\interfaces"; Pkg = "package interfaces;" }
)

foreach ($item in $dirsToFix) {
    if (Test-Path $item.Path) {
        $files = Get-ChildItem -Path $item.Path -Filter "*.java"
        foreach ($file in $files) {
            $content = Get-Content $file.FullName -Raw
            if ($content -notmatch "package ") {
                $content = $item.Pkg + "`r`n`r`n" + $content
            }
            if ($file.Name -eq "CampusServer.java" -and $content -notmatch "import com.sun.net.httpserver.HttpServer;") {
                $content = $content -replace "import com.sun.net.httpserver.HttpHandler;", "import com.sun.net.httpserver.HttpServer;`r`nimport com.sun.net.httpserver.HttpHandler;"
            }
            if ($file.Name -eq "FileStorageManager.java" -and $content -notmatch "import java.io.\*;") {
                $content = $content -replace "import java.util.\*;", "import java.util.*;`r`nimport java.io.*;"
            }
            [System.IO.File]::WriteAllText($file.FullName, $content, $Utf8NoBomEncoding)
        }
    }
}

Write-Host "Added packages and missing imports."
