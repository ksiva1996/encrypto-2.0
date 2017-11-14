<?php
  
    $response = array();
    $file_path = "uploads/";
    $file_path = $file_path . basename( $_FILES['uploaded_file']['name']);
    if(move_uploaded_file($_FILES['uploaded_file']['tmp_name'], $file_path) ){
         $response["success"] = 1;
         $response["message"] = "signup successfull";
    }
    else
    {
        echo "fail";
    }
    
 ?>