<?php  
	require "monitor.phps";
	require "configuration.phps";
	require "httpdigest.phps";

	function do_work($filename) {
		if(is_file($filename)) {
			$fp = fopen($filename, "rb");
			header("Content-Type: application/octet-stream");
			header("Content-Disposition: attachment; filename=synced.file");
			header("Content-Length: " . filesize($filename));
			header("Expires: 0");
			header("Cache-Control: must-revalidate, post-check=0, pre-check=0");
			header("Content-MD5: " . base64_encode(md5_file($filename)));
			readfile($filename);
		} else {
			echo "No file under synchronization!\r\nLock acquired. You may now submit files!\r\n";
		}

		exit;
	}

	$mysql = mysql_connect($mysql_server, $mysql_user, $mysql_pass);

	if(!$mysql)
		die("Could not connect: " . mysql_error() . "\r\n");

	mysql_select_db($db_name);

	$digest = new HTTPDigest();

	try {
		$userID = $digest->authenticate($mysql);
	} catch (Exception $e) {
		$digest->send($e->getMessage());
	}

	$monitor = new MySQLMonitor($mysql, $lock_timeout, $userID);

	try {
		$monitor->acquire_monitor();
	} catch (Exception $e) {
		die($e->getMessage() . "\r\n");
	}

	do_work($synced_filename);

	mysql_close($mysql);
?>