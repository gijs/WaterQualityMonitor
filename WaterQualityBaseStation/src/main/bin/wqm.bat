set START_DIR=%cd%
set BASE_DIR=%cd%\..
set LIB_DIR=%BASE_DIR%\libs
set NATIVE_LIB_DIR=%BASE_DIR%\native\Windows\i368-mingw32\
set CONFIG_DIR=%BASE_DIR%\config
cd %BASE_DIR%
java -Djava.library.path=%NATIVE_LIB_DIR% -cp "%LIB_DIR%\*" wqm.Launch --config-dir %CONFIG_DIR% %*
cd %START_DIR%
