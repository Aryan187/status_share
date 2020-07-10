# Status Share
A toy implementation of a simplified facebook clone aimed at being as secure as possible.
This project was done under the supervision of Dr. Alptekin Küpçü of Koç University.

## Installation
* Install XAMPP (https://www.apachefriends.org/index.html)
* Install AndroidStudio 4.0 (https://developer.android.com/studio) and install APK 28/29/30 in Android Studio.
* Copy the contents of the "server" folder into your XAMPP installation directory (Default is `C:/xampp`) and start/restart the server (more specifically, the Apache and MySQL services). 
  * If you have not installed XAMPP in the default directory, then change the directory accordingly in the `server/mysql/bin/my.ini` file.
  * It is assumed that the credentials of the default admin account of phpMyAdmin are not changed. If they are, change the username and password field in line 3-4 of every `.php` file (in `server/htdocs/Android` and `server/htdocs/AndroidMulti`).
* Run `localhost/Android/initialize.php` (for single server version) and `localhost/AndroidMulti/initialize.php` (for multi-server version) in your browser to create all the required databases.
* Import all the files except the `server` folder into Android Studio.
* Rename `MainActivityMulti` to `MainActivity.java` in `app/src/main/java/com/aryan/statusshare` if you plan to use the multiple server version.
* Change the `private static string host` variable in `MainActivity.java` to the IP Address of your WiFi connection. You can find it by typing `ipconfig` in command prompt. 
* Make sure that the platform you are testing on (emulator/phone) is connected to the same wifi as your PC. You can test this by typing the IP Address of your PC Wifi in your phone/emulator browser and checking if the XAMPP dashboard comes up.
* Build the app in Android Studio and run on your emulator/phone.
