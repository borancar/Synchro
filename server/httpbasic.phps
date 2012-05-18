<?php
  class HTTPBasic {
    function send() {
      header("WWW-Authenticate: Basic realm=\"Synchro\"");
      header("HTTP/1.0 401 Unauthorized");
      echo("Wrong username or password!\r\n");
      exit;
    }

    function authenticate($mysql) {
      $result = mysql_query("SELECT * FROM users WHERE `Username`='" . mysql_real_escape_string($_SERVER["PHP_AUTH_USER"]) . "' AND `Password`=PASSWORD('" . mysql_real_escape_string($_SERVER["PHP_AUTH_PW"]) . "')");

      if(mysql_num_rows($result) === 0) {
	throw new Exception("Wrong username or password!");
      } else {
	$value = mysql_fetch_array($result);
	return $value['UserID'];
      }
    }
  }
?>
