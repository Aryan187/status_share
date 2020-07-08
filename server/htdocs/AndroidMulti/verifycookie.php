<?php
$servername = "localhost";
$username = "root";
$password = "";
$database = "ssmulti_auth";

$conn = new mysqli($servername, $username, $password, $database);

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$response = array(); 

if($_SERVER['REQUEST_METHOD']=='POST'){
	
	$_POST = json_decode(file_get_contents('php://input'), true);
	
	$username2 = $_POST['username'];
	
		$stmt2 = $conn->prepare("SELECT SHA2(?,512);");
		$stmt2->bind_param("s",$_COOKIE[$username2]);
		$stmt2->execute();
		$stmt2->bind_result($tmp2);
		$c;
		while ($stmt2->fetch()){
			$c = $tmp2;
		}
		
		$stmt = $conn->prepare("SELECT session_id FROM user WHERE username = ?");
		$stmt->bind_param("s",$username2);
		
		//if data inserts successfully
		if($stmt->execute()){
			$stmt->bind_result($tmp);
			while ($stmt->fetch()){
				if ($tmp == $c){
					$response['error'] = false; 
					$response['message'] = 'Authenticated';
				}
				else {
					$response['error'] = true; 
					$response['message'] = 'Session Expired';
				}
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
 
//displaying the data in json format 
echo json_encode($response);

?>