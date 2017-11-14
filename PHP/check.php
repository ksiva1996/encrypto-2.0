<?php
 
$response = array();
if (isset($_POST['username']))
 {
 
    $username = $_POST['username'];
    require_once __DIR__ . '/db_config.php';
    $status = 0;
    $mysqli = new mysqli(DB_SERVER,DB_USERNAME, DB_PASSWORD,DB_DATABASE);
    $sql = "SELECT * FROM files WHERE touser = '$username' AND status = '$status'";
    $result = $mysqli->query($sql);
    if ($result->num_rows===0) {
            $response["sync"] = 0;
            $response["message"] = "unsuccessful please retry";
            echo json_encode($response);
    } 
    else
    {
    	$status = 1;	
    	$ans = array();
     	while($row = mysqli_fetch_array($result)){
	array_push($ans,
	array('databaseID'=>$row[0],'filename'=>$row[1],'from'=>$row[2],'key'=>$row[4],'fileaddress'=>$row[6]));
	$s="UPDATE files SET `status` = '$status' WHERE id = '$row[0]'";
	$x = $mysqli->query($s);
        }
        $response["sync"] = 1;
        $response["files"] = $ans;
        echo json_encode($response);
    }
} 
else {
    // required field is missing
    $response["success"] = 0;
    $response["message"] = "Required field(s) is missing";
 
    // echoing JSON response
    echo json_encode($response);
}
?>