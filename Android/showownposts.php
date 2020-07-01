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
	
	$userid = $_POST['id'];
		
	$stmt = $conn->prepare("SELECT status, timestamp from status WHERE user_id_fk = ? ORDER BY timestamp DESC;");
	$stmt->bind_param("s",$userid);
		
	//if data inserts successfully
	if($stmt->execute()){
		$response['error'] = false; 
		$response['message'] = 'Query Successful';
		$stmt->bind_result($status,$time);
		while ($stmt->fetch()){
			$temp = ['status'=>$status,'time'=>$time];
			array_push($response,$temp);
		}
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