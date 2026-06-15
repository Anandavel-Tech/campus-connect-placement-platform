$Utf8NoBomEncoding = New-Object System.Text.UTF8Encoding $False

$files = Get-ChildItem -Path "src" -Recurse -Filter "*.java"
foreach ($file in $files) {
    # Read without assuming specific BOM, it will read content
    $content = Get-Content $file.FullName -Raw
    
    # Manually remove BOM if it exists
    if ($content.Length -gt 0 -and $content[0] -eq [char]0xFEFF) {
        $content = $content.Substring(1)
    }
    
    # Count braces
    $openBraces = ($content.ToCharArray() | Where-Object { $_ -eq '{' }).Count
    $closeBraces = ($content.ToCharArray() | Where-Object { $_ -eq '}' }).Count
    
    # Add missing braces
    if ($openBraces -gt $closeBraces) {
        $missing = $openBraces - $closeBraces
        for ($i = 0; $i -lt $missing; $i++) {
            $content += "`r`n}"
        }
    }
    
    # Write back without BOM
    [System.IO.File]::WriteAllText($file.FullName, $content, $Utf8NoBomEncoding)
}

Write-Host "Fixed syntax and BOM."
