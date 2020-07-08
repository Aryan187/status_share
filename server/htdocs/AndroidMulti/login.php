<?php
$servername = "localhost";
$username = "root";
$password = "";
$database = "ssmulti_auth";
session_start();

$conn = new mysqli($servername, $username, $password, $database);

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$response = array(); 

if($_SERVER['REQUEST_METHOD']=='POST'){
	
	$_POST = json_decode(file_get_contents('php://input'), true);
	
	$username2 = $_POST['username'];
	$password2 = $_POST['password'];
	
	$stmt4 = $conn->prepare("SELECT COUNT(username) FROM user WHERE username = ?;");
	$stmt4->bind_param("s",$username2);
	$stmt4->execute();
	$stmt4->bind_result($t1);
	$c;
	while ($stmt4->fetch()){
		$c = $t1;
	}
	$stmt4->close();
	
	if ($c == 0){
		$response['error'] = true; 
		$response['message'] = "User does not exist"; 
	}
		
	else {
	
		$stmt3 = $conn->prepare("SELECT SALT FROM user WHERE username = ?;");
		$stmt3->bind_param("s",$username2);
		$stmt3->execute();
		$stmt3->bind_result($sal);
		$res;
		while ($stmt3->fetch()){
			$res = $sal;
		}
		$stmt3->close();
		
		$stmt2 = $conn->prepare("SELECT SHA2(CONCAT(?,?),512);");
		$stmt2->bind_param("ss",$res,$password2);
		$stmt2->execute();
		$stmt2->bind_result($hash);
		$res2;
		while ($stmt2->fetch()){
			$res2 = $hash;
		}
		$stmt2->close();
		
		$stmt = $conn->prepare("SELECT HASH FROM user WHERE username = ?;");
		$stmt->bind_param("s",$username2);
		$stmt->execute();
		$stmt->bind_result($pw);
		$res3;
		while ($stmt->fetch()){
			$res3 = $pw;
		}
		$stmt->close();
		if($res2 == $res3){
			setcookie($username2,session_create_id(),time()+3600);
			$response['error'] = false; 
			$response['message'] = 'Login successful';
		}else{
			//if not making failure response 
			$response['error'] = true; 
			$response['message'] = 'Wrong Password';
		}
		
	}
}
else{
		$response['error'] = true; 
		$response['message'] = "Invalid request"; 
	}
 
//displaying the data in json format
session_destroy();
echo json_encode($response);

?>