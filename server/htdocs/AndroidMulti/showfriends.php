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
	
	$id = $_POST['id'];
		
	$stmt = $conn->prepare("SELECT friend_one , friend_two FROM friends WHERE (friend_one = ? OR friend_two = ?) AND friend_status = 1;");
	$stmt->bind_param("ss",$id,$id);
		
	//if data inserts successfully
	if($stmt->execute()){
		$response['error'] = false; 
		$response['message'] = 'Query Successful';
		$stmt->bind_result($fid,$fid2);
		while ($stmt->fetch()){
			$temp = array();
			$c;
			if ($fid == $id)
				$c = $fid2;
			else
				$c = $fid;
			$temp['id'] = $c;
			$conn2 = new mysqli($servername, $username, $password, $database);
			$stmt2 = $conn2->prepare("SELECT user_name FROM userdata WHERE user_id = ?;");
			$stmt2->bind_param("s",$c);
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