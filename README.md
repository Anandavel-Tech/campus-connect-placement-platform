# Campus Connect

Campus Connect is a plain Java HTTP server with a static HTML/CSS/JavaScript frontend.

## Requirements

- JDK 17 or newer
- PowerShell

## Run

From the project folder:

```powershell
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

Then open:

```text
http://localhost:8080
```

Stop the server with `Ctrl+C`.

## Default Admin Login

- Email: `admin@campusconnect.com`
- Password: `admin123`

## Demo Logins

- Student: `student@campusconnect.com` / `student123`
- Recruiter: `recruiter@campusconnect.com` / `recruiter123`

## Manual Build And Run

```powershell
$files = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -cp "lib\gson-2.10.1.jar" -d bin $files
java -cp "bin;lib\gson-2.10.1.jar" Main
```
