# status_share
A toy implementation of a simplified facebook clone aimed at being as secure as possible.
This project was done under the supervision of Dr. Alptekin Küpçü of Koç University.

## Installation
* Install XAMPP (https://www.apachefriends.org/index.html)
* Copy the contents of the "server" folder into your XAMPP installation directory (Default is C:/xampp) and start/restart the server (more specifically, the Apache and MySQL services)
* It is assumed that the password of the root account is set to "". If not, change the password field in line 4 of every .php file.
* Run localhost/Android/initialize.php and localhost/AndroidMulti/initialize.php in your browser to create all the required databases.
* Import all the other files of the repository into Android Studio.
* Rename ActivityMainMulti.java to ActivityMain.java if you plan to use the multiple server version.
* Change the "private static string host" variable in ActivityMain.java to the IP Address of your WiFi connection.
* Make sure that the platform you are testing on (emulator/phone) is connected to the same wifi as your PC. You can test it by typing the IP Address of your PC Wifi in your phone/emulator browser and checking if the XAMPP dashboard comes up.
* Build the app in Android Studio and run.
