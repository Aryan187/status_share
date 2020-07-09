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
		
	$stmt = $conn->prepare("SELECT DISTINCT status.status_id, userdata.user_id, userdata.user_name, 
	status.status, status.timestamp FROM userdata, status, friends WHERE userdata.user_id = status.user_id_fk AND 
	(userdata.user_id = ? OR (CASE WHEN friends.friend_one = ? THEN friends.friend_two = userdata.user_id WHEN 
	friends.friend_two = ? THEN friends.friend_one = userdata.user_id END)) ORDER BY status.timestamp DESC;");
	$stmt->bind_param("iii",$userid,$userid,$userid);
		
	//if data inserts successfully
	if($stmt->execute()){
		$response['error'] = false; 
		$response['message'] = 'Query Successful';
		$stmt->bind_result($statusid,$uid,$uname,$status,$time);
		while ($stmt->fetch()){
			$temp = ['statusid'=>$statusid,'uid'=>$uid,'uname'=>$uname,'status'=>$status,'time'=>$time];
			array_push($response,$temp);
		}
		$database = "ssmulti_main2";
		$conn = new mysqli($servername, $username, $password, $database);
		if ($conn->connect_error) {
			die("Connection failed: " . $conn->connect_error);
		}
		$stmt = $conn->prepare("SELECT DISTINCT status.status_id, userdata.user_id, userdata.user_name, 
		status.status, status.timestamp FROM userdata, status, friends WHERE userdata.user_id = status.user_id_fk AND 
		(userdata.user_id = ? OR (CASE WHEN friends.friend_one = ? THEN friends.friend_two = userdata.user_id WHEN 
		friends.friend_two = ? THEN friends.friend_one = userdata.user_id END)) ORDER BY status.timestamp DESC;");
		$stmt->bind_param("iii",$userid,$userid,$userid);
		$stmt->execute();
		$stmt->bind_result($statusid,$uid,$uname,$status,$time);
		while ($stmt->fetch()){
			$temp = ['status'=>$status];
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