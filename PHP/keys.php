<?php

$response = array();

if (isset($_POST['privateKey']) && isset($_POST['username']) && isset($_POST['publicKey'])) {
 
    $publicKey = $_POST['publicKey'];
    $username = $_POST['username'];
    $privateKey = $_POST['privateKey'];
    require_once __DIR__ . '/db_config.php';
    $mysqli = new mysqli(DB_SERVER,DB_USERNAME, DB_PASSWORD,DB_DATABASE);
    
    $sql = "UPDATE users SET publicKey = '$publicKey' , privateKey = '$privateKey' WHERE username = '$username'";
         $result = $mysqli->query($sql);
         if($result)
         {
         $response["success"] = 1;
         $response["message"] = "successfull";
         } 
         else
         {
            $response["success"] = 0;
            $response["message"] = "unsuccessful please retry";
            
          }        
        echo json_encode($response);
} 
else {
    // required field is missing
    $response["success"] = 0;
    $response["message"] = "Required field(s) is missing";
 
    // echoing JSON response
    echo json_encode($response);
}
?>