@echo off
setlocal
set "username=%1"
set "password=%2"
set "db_username=%3"
set "db_password=%4"
call :strlen password_len password

FOR /F "tokens=*" %%a in ('curl --request POST --data "password=%password%&cost=10" https://bcrypt.org/api/generate-hash.json') do SET response=%%a
echo %response%
SET /A before=71+%password_len%
call set password=%%response:~%before%,60%%
echo %password%

call mysql --user=%db_username% --password=%db_password% -h localhost -D dcount -e "insert into users (username, password) value ('%username%', '%password%')"

goto :eof


:strlen <resultVar> <stringVar>
(   
    setlocal EnableDelayedExpansion
    (set^ tmp=!%~2!)
    if defined tmp (
        set "len=1"
        for %%P in (4096 2048 1024 512 256 128 64 32 16 8 4 2 1) do (
            if "!tmp:~%%P,1!" NEQ "" ( 
                set /a "len+=%%P"
                set "tmp=!tmp:~%%P!"
            )
        )
    ) ELSE (
        set len=0
    )
)
( 
    endlocal
    set "%~1=%len%"
    exit /b
)