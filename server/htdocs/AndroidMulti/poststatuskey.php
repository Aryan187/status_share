<?php
$servername = "localhost";
$username = "root";
$password = "";
$database = "ssmulti_main1";

$conn = new mysqli($servername, $username, $password, $database);

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$response = array(); 

if($_SERVER['REQUEST_METHOD']=='POST'){
	
	$_POST = json_decode(file_get_contents('php://input'), true);
	
	$userid = $_POST['id'];
	$status = $_POST['status'];
		
	$stmt = $conn->prepare("INSERT INTO status (status,user_id_fk) VALUES (?,?);");
	$stmt->bind_param("ss",$status,$userid);
		
	//if data inserts successfully
	if($stmt->execute()){
		$response['error'] = false; 
		$response['message'] = 'Posted Successfully'; 
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