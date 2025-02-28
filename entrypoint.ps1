# entrypoint.ps1
Write-Host "Aguardando PostgreSQL..."

do {
    $isReady = (Test-NetConnection -ComputerName postgres -Port 5432).TcpTestSucceeded
    Start-Sleep -Seconds 2
} until ($isReady)

Write-Host "PostgreSQL está pronto. Iniciando aplicação..."
java -jar app.jar

