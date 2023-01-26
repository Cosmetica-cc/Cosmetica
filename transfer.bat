git checkout 1.19
git merge master
pause
REM ==============================
git checkout 1.19.1
git merge 1.19
pause
REM ==============================
git checkout 1.19.3
git merge 1.19.1
pause
REM ==============================
REM BACKPORTS
git checkout 1.18.1
git merge master
pause
REM ==============================
git checkout 1.17
git merge 1.18.1
pause
REM ==============================
git checkout 1.16.5
git merge 1.17
pause
REM ==============================
git checkout master