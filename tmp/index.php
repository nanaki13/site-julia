<?php 

	
// Afficher les erreurs à l'écran
ini_set('display_errors', 1);
// Enregistrer les erreurs dans un fichier de log
//ini_set('log_errors', 1);
// Nom du fichier qui enregistre les logs (attention aux droits à l'écriture)
//ini_set('error_log', dirname(__file__) . '/log_error_php.txt');
// Afficher les erreurs et les avertissements
// error_reporting(e_all);
include 'php/autoload.php';
include 'php/function.php';

//

try{
  session_start();	
 
  
  $conf = new Config(getPage(),getSubPage());
  if(getPage()=='admin'  && ! isLogged()){
  	throw new Exception('401');
  }else{
  	$request = new Request($_SERVER);
  	$request->process($conf->getConfig());
  }
  
  
}catch(Exception $e){
  if(preg_match('/[0-9]*/',$e->getMessage() )){
      echo $e->getMessage();
      http_response_code($e->getMessage());
  }else{
    throw $e;
  }
}

//print_r($_SERVER);
//echo "page = ". $_GET['page']."</br>";
//echo "subPage = ".$_GET['subPage']."</br>";



?>





