git checkout 1.19
git merge master
pause
git push
@echo off
echo ==============================
@echo on
git checkout 1.19.1
git merge 1.19
pause
git push
@echo off
echo ==============================
@echo on
git checkout 1.19.3
git merge 1.19.1
pause
git push
@echo off
echo ==============================
@echo on
git checkout 1.19.3
git merge 1.19.1
pause
git push
@echo off
echo ==============================
@echo on
git checkout 1.19.4
git merge 1.19.3
pause
git push
@echo off
echo ==============================
echo BACKPORTS
@echo on
git checkout 1.18.1
git merge master
pause
git push
@echo off
echo ==============================
@echo on
git checkout 1.17
git merge 1.18.1
pause
git push
@echo off
echo ==============================
@echo on
git checkout 1.16.5
git merge 1.17
pause
git push
@echo off
echo ==============================
@echo on
git checkout master