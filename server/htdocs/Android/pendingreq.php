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
	
	$id = $_POST['id'];
		
	$stmt = $conn->prepare("SELECT friend_two FROM friends WHERE friend_one = ? AND friend_status = 0;");
	$stmt->bind_param("s",$id);
		
	//if data inserts successfully
	if($stmt->execute()){
		$response['error'] = false; 
		$response['message'] = 'Query Successful';
		$stmt->bind_result($fid);
		while ($stmt->fetch()){
			$temp = array();
			$temp['id'] = $fid;
			$conn2 = new mysqli($servername, $username, $password, $database);
			$stmt2 = $conn2->prepare("SELECT user_name FROM userdata WHERE user_id = ?;");
			$stmt2->bind_param("s",$fid);
			$stmt2->execute();
			$stmt2->bind_result($tmp);
			while ($stmt2->fetch()){
				$temp['name'] = $tmp;
			}
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