<?php  
	require "monitor.phps";
	require "configuration.phps";
	require "httpdigest.phps";

	function do_work($filename) {
		if(!isset($_FILES['userfile'])) {
?>
	<form enctype="multipart/form-data" action="submit.php" method="POST">
	<input type="hidden" name="MAX_FILE_SIZE" value="300000" />
	File: <input name="userfile" type="file" />
	<input type="submit" value="Send File" />
	</form>
<?php
			exit;
		} else {
			if(!isset($_POST['md5'])) {
				echo "MD5 hash not sent from client!";
				move_uploaded_file($_FILES['userfile']['tmp_name'], $filename);
				exit;
			} else {
				$actualMD5 = md5_file($_FILES['userfile']['tmp_name']);
				$receivedMD5 = $_POST['md5'];
				
				if($actualMD5 != $receivedMD5) {
					echo "File corrupt! MD5 hashes do not match!\r\nExpected: " . $receivedMD5 . "\r\nActual: " . $actualMD5;
					exit;
				} else {
					move_uploaded_file($_FILES['userfile']['tmp_name'], $filename);
					echo "File sent successfully!";
				}
			}
		}
	}

	$mysql = mysql_connect($mysql_server, $mysql_user, $mysql_pass);

	if(!$mysql)
		die("Could not connect: " . mysql_error() . "\r\n");

	mysql_select_db($db_name);

	$digest = new HTTPDigest();

	try {
		$userID = $digest->authenticate($mysql);
	} catch (Exception $e) {
		$digest->send($e->getMessage() . "\r\n");
	}

	$monitor = new MySQLMonitor($mysql, $lock_timeout, $userID);

	if(!$monitor->is_acquired()) {
		die("You do not own the lock currently! Request file first to obtain the lock!\r\n");
	}

	try {
		$monitor->acquire_monitor();
	} catch (Exception $e) {
		die($e->getMessage() . "\r\n");
	}

	do_work($synced_filename);

	$monitor->release_monitor();

	mysql_close($mysql);
?>
