<?php
if(isset($_POST['filename'])&&isset($_POST['from'])&&isset($_POST['toUser'])&&isset($_POST['key']))
    {
    require_once __DIR__ . '/db_config.php';
    $mysqli = new mysqli(DB_SERVER,DB_USERNAME, DB_PASSWORD,DB_DATABASE);
    $name = $_POST['filename'];
    $from = $_POST['from'];
    $toUser = $_POST['toUser'];
    $k =  $_POST['key'];
    $download = 0;
    $address = "http://araniisansthan.com/Encrypto/uploads/".$name;
    $sql = "INSERT INTO files (name,fromuser,touser,secretkey,`status`,address) VALUES ('$name','$from','$toUser','$k','$download','$address')";
         $result = $mysqli->query($sql);
         if($result)
         {
         $response["success"] = 1;
         $response["message"] = "signup successfull";
         echo json_encode($response);
         } 
    }
    else
    {
    	 $response["success"] = 0;
         $response["message"] = "signup successfull";
         echo json_encode($response);
    }
 ?>