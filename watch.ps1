# ChainedClimber2D - Live Development Script
# Watches files and auto-compiles on save

Write-Host ""
Write-Host "========================================"  -ForegroundColor Cyan
Write-Host "  ChainedClimber2D - LIVE DEV MODE" -ForegroundColor Yellow
Write-Host "========================================"  -ForegroundColor Cyan
Write-Host ""
Write-Host "Features:" -ForegroundColor Green
Write-Host "  ✓ Watches .java files for changes"
Write-Host "  ✓ Auto-compiles when you save"
Write-Host "  ✓ Fast incremental builds"
Write-Host ""
Write-Host "Usage:" -ForegroundColor Green
Write-Host "  1. This window watches and compiles"
Write-Host "  2. Run 'gradlew desktop:run' in another terminal"
Write-Host "  3. Save code -> Auto compile -> Restart game"
Write-Host ""
Write-Host "========================================"  -ForegroundColor Cyan
Write-Host ""
Write-Host "Starting file watcher..." -ForegroundColor Yellow
Write-Host ""

# Start gradle in continuous mode
& .\gradlew.bat -t classes --console=plain

Write-Host ""
Write-Host "File watcher stopped." -ForegroundColor Red
Read-Host "Press Enter to exit"
