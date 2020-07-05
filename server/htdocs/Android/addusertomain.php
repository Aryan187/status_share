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
		
		$stmt = $conn->prepare("INSERT INTO userdata (user_name) VALUES (?);");
		$stmt->bind_param("s",$username2);
		
		//if data inserts successfully
		if($stmt->execute()){
			$response['error'] = false; 
			$response['message'] = 'User Added Successfully';
		}else{
			//if not making failure response 
			$response['error'] = true; 
			$response['message'] = 'Please try later';
		}
}
else{
		$response['error'] = true; 
		$response['message'] = "Invalid request"; 
	}
 
//displaying the data in json format 
echo json_encode($response);

?>