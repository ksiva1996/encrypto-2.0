<?php
 
$response = array();

if (isset($_POST['name']) && isset($_POST['username']) && isset($_POST['password'])) 
{
 
    $name = $_POST['name'];
    $username = $_POST['username'];
    $password = $_POST['password'];
    require_once __DIR__ . '/db_config.php';
    $mysqli = new mysqli(DB_SERVER,DB_USERNAME, DB_PASSWORD,DB_DATABASE);
    $sql = "SELECT * FROM users WHERE username = '$username'";
    $result = $mysqli->query($sql);
    if ($result->num_rows===0) {
         $sql = "INSERT INTO users (name,username,password) VALUES ('$name','$username','$password')";
         $result = $mysqli->query($sql);
         if($result)
         {
         $response["success"] = 1;
         $response["message"] = "signup successfull";
         } 
         else
         {
            $response["success"] = 0;
            $response["message"] = "unsuccessful please retry";
            
          }        
        echo json_encode($response);
    } 
    else
    {
     $response["success"] = 0;
     $response["message"] = "username already exists";         
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