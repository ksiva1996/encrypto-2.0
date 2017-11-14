<?php
include __DIR__ . '/db_config.php'; 
$response = array();

 if (isset($_POST['username']))
 { 
    $username = $_POST['username'];
    $mysqli = new mysqli(DB_SERVER,DB_USERNAME, DB_PASSWORD,DB_DATABASE);
    $sql = "SELECT * FROM users WHERE username = '$username' ";
    $result = $mysqli->query($sql);
    if ($result->num_rows===0) 
    {
         $response["success"] = 0;
         $response["message"] = "user does not exist";         
         echo json_encode($response);
    } 
    else
    {
     $response["success"] = 1;
    
    while( $row = mysqli_fetch_array($result))
    {
     $response["publicKey"] = $row[4];
    }
     echo json_encode($response);
    }
} 
else
{
    $response["success"] = 0;
    $response["message"] = "Required field(s) is missing";
    echo json_encode($response);
}
?>