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
		
	$stmt = $conn->prepare("SELECT COUNT(friend_status) FROM friends WHERE (friend_one = ? OR friend_two = ?) AND (friend_one = ? OR friend_two = ?);");
	$stmt->bind_param("iiii",$id,$id,$fid,$fid);
	$stmt->execute();
	$stmt->bind_result($tmp);
	$c;
	while ($stmt->fetch()){
		$c = $tmp;
	}
	if ($c == 0){
		$response['error'] = false; 
		$response['message'] = "Request Not Sent";
		$response['ans'] = 0;
	}
	else {
		$stmt2 = $conn->prepare("SELECT friend_status FROM friends WHERE (friend_one = ? OR friend_two = ?) AND (friend_one = ? OR friend_two = ?);");
		$stmt2->bind_param("iiii",$id,$id,$fid,$fid);
		$stmt2->execute();
		$stmt2->bind_result($tmp2);
		$c2;
		while ($stmt2->fetch()){
			$c2 = $tmp2;
		}
		if ($c2 == 0){
			$response['error'] = false; 
			$response['message'] = "Request has been received/sent. Please accept/wait for it to be accepted.";
			$response['ans'] = 1;
		}
		else {
			$response['error'] = false; 
			$response['message'] = "Friends";
			$response['ans'] = 2;
		}
	}
	//if data inserts successfully
}
else{
		$response['error'] = true; 
		$response['message'] = "Invalid request"; 
	}

$response['size'] = count($response);
 
//displaying the data in json format 
echo json_encode($response);

?>