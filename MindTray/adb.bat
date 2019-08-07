cd /d C:\Users\Win7\AppData\Local\Android\android-sdk\platform-tools\

REM adb.exe -s FANVVSPRH6SGKFA6 shell

adb -s FANVVSPRH6SGKFA6 shell "run-as moonlightflower.com.mindtray chmod 666 /data/data/moonlightflower.com.mindtray/databases/memos.db

adb pull /data/data/moonlightflower.com.mindtray/databases/memos.db .

adb shell "run-as moonlightflower.com.mindtray chmod 600 /data/data/moonlightflower.com.mindtray/databases/memos.db

pause