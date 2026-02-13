@echo off
echo Algonix - Local sozlash
echo.

if exist src\main\resources\application.properties (
    echo application.properties allaqachon mavjud.
    echo.
    choice /C YN /M "Qayta yaratilsinmi"
    if errorlevel 2 goto :end
)

echo application.properties yaratilmoqda...
copy src\main\resources\application.properties.example src\main\resources\application.properties

echo.
echo Tayyor! Endi application.properties faylida parolni o'zgartiring:
echo   src\main\resources\application.properties
echo.
echo Quyidagi qatorni o'zgartiring:
echo   spring.datasource.password=sizning_parolingiz
echo.

:end
pause
