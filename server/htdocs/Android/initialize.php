<?php
$servername = "localhost";
$username = "root";
$password = "";

// Create connection
$conn = mysqli_connect($servername, $username, $password);
// Check connection
if (!$conn) {
  die("Connection failed: " . mysqli_connect_error());
}

// Create database
$stmt = $conn->prepare("CREATE DATABASE IF NOT EXISTS ss_auth;");
$stmt->execute();
$stmt = $conn->prepare("CREATE DATABASE IF NOT EXISTS ss_main;");
$stmt->execute();
$database="ss_auth";
$conn = mysqli_connect($servername, $username, $password, $database);
$stmt = $conn->prepare("CREATE TABLE IF NOT EXISTS user (username VARCHAR(20) PRIMARY KEY, hash CHAR(128), salt CHAR(10), session_id CHAR(128) DEFAULT NULL) ENCRYPTED=YES ENCRYPTION_KEY_ID=4;");
$stmt->execute();
$database="ss_main";
$conn = mysqli_connect($servername, $username, $password, $database);
$stmt = $conn->prepare("CREATE TABLE IF NOT EXISTS userdata (user_id INT PRIMARY KEY AUTO_INCREMENT, user_name VARCHAR(20)) ENCRYPTED=YES ENCRYPTION_KEY_ID=1;");
$stmt->execute();
$stmt = $conn->prepare("CREATE TABLE IF NOT EXISTS status (status_id INT PRIMARY KEY AUTO_INCREMENT, status VARCHAR(200), user_id_fk INT, timestamp timestamp DEFAULT current_timestamp, FOREIGN KEY (user_id_fk) REFERENCES userdata(user_id)) ENCRYPTED=YES ENCRYPTION_KEY_ID=3;");
$stmt->execute();
$stmt = $conn->prepare("CREATE TABLE IF NOT EXISTS friends (friend_one INT, friend_two INT, friend_status INT DEFAULT 0, PRIMARY KEY (friend_one,friend_two), FOREIGN KEY (friend_one) REFERENCES userdata(user_id), FOREIGN KEY (friend_two) REFERENCES userdata(user_id)) ENCRYPTED=YES ENCRYPTION_KEY_ID=2;");
$stmt->execute();

mysqli_close($conn);
echo("true");
?>
