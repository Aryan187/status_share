<?php
$servername = "localhost";
$username = "root";
$password = "";
$database = "ss_auth";

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
	
	if ($c == 1){
		$response['error'] = true; 
		$response['message'] = "Username is already taken";
	}
	
	else {
	
		$stmt3 = $conn->prepare("SELECT SUBSTRING(SHA1(RAND()),1,10);");
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
		
		$stmt = $conn->prepare("INSERT INTO user (username,salt,hash) VALUES (?,?,?);");
		$stmt->bind_param("sss",$username2,$res,$res2);
		
		//if data inserts successfully
		if($stmt->execute()){
			$response['error'] = false; 
			$response['message'] = 'Registered successfully'; 
		}else{
			//if not making failure response 
			$response['error'] = true; 
			$response['message'] = 'Please try later';
		}
		
	}
}
else{
		$response['error'] = true; 
		$response['message'] = "Invalid request"; 
	}
 
//displaying the data in json format 
echo json_encode($response);

?>