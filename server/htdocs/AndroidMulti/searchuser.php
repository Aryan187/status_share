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
	
	$key = $_POST['key'];
	$id = $_POST['id'];
		
	$stmt = $conn->prepare("SELECT user_id, user_name from userdata WHERE user_name LIKE (?) AND user_id != ?;");
	$stmt->bind_param("si",$key,$id);
		
	//if data inserts successfully
	if($stmt->execute()){
		$response['error'] = false; 
		$response['message'] = 'Query Successful';
		$stmt->bind_result($id,$name);
		while ($stmt->fetch()){
			$temp = ['id'=>$id,'name'=>$name];
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