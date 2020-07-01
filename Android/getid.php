<?php
$servername = "localhost";
$username = "root";
$password = "";
$database = "ss_main";

$conn = new mysqli($servername, $username, $password, $database);

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$response = array(); 

if($_SERVER['REQUEST_METHOD']=='POST'){
	
	$_POST = json_decode(file_get_contents('php://input'), true);
	
	$username2 = $_POST['username'];
	
	if (!isset($_COOKIE[$username2])){
		$response['error'] = true;
		$response['message'] = "Session Expired";
		$response['id'] = -1;
	}
	else{
		$stmt = $conn->prepare("SELECT user_id FROM userdata WHERE user_name = ?;");
		$stmt->bind_param("s",$username2);

		//if data inserts successfully
		if($stmt->execute()){
			$stmt->bind_result($tmp);
			while ($stmt->fetch()){
				$response['id'] = strval($tmp);
			}
			$response['error'] = false; 
			$response['message'] = 'User Added Successfully';
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