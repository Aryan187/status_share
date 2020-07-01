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
	
	$fid = $_POST['fid'];
	$id = $_POST['id'];
		
	$stmt = $conn->prepare("UPDATE friends SET friend_status = 1 WHERE friend_one = ? AND friend_two = ?;");
	$stmt->bind_param("ii",$id,$fid);
		
	//if data inserts successfully
	if($stmt->execute()){
		$response['error'] = false; 
		$response['message'] = 'Request Accepted';
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

$response['size'] = count($response);
 
//displaying the data in json format 
echo json_encode($response);

?>