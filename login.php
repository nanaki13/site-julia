<?php 
if(isLogged()){
	require 'logout.php';
?>

<?php 
}else{
	?>
<form method="post" action="/login">
	<label for="login">login</label><input type="text" name="login"/>
	<label for="password">password</label><input type="password" name="password"/>
	<input type="submit" value="OK"/>
</form>	
	<?php 	
}
?>

