<?php
$servername = "localhost";
$username = "root";
$password = "";
$database = "android";

$conn = new mysqli($servername, $username, $password, $database);

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$users = array(); 

$sql = "SELECT id, name, SEX FROM userdetails;";

$stmt = $conn->prepare($sql);

$stmt->execute();

$stmt->bind_result($id, $name, $sex);

while($stmt->fetch()){
	
	$temp = [
		'id'=>$id,
		'name'=>$name,
		'sex'=>$sex
	];
	
	array_push($users, $temp);
}
 
echo json_encode($users);