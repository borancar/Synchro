<?php  
	class MySQLMonitor {
		protected $lock_timeout;
		protected $userID;

		function __construct($mysql, $lock_timeout, $userID) {
			$this->lock_timeout = $lock_timeout;
			$this->userID = $userID;
		}

		private static function acquire($userID) {
			mysql_query("UPDATE sync SET `UserID`=" . mysql_real_escape_string($userID));
			mysql_query("UPDATE sync SET `lock`=1");
			mysql_query("UPDATE sync SET `time`=NOW()");
		}

		public function is_acquired() {
			mysql_query("LOCK TABLES sync READ");

			$result = mysql_query("SELECT `UserID`=" . mysql_real_escape_string($this->userID) . " AND `lock`=1 AND `time`+ " . $this->lock_timeout . ">=NOW() as `locked` from sync");
			$value = mysql_fetch_array($result);

			mysql_query("UNLOCK TABLES");

			return $value['locked'];
		}

		public function acquire_monitor() {
			mysql_query("LOCK TABLES sync WRITE, users READ LOCAL");

			$result = mysql_query("SELECT * FROM sync");
			$value = mysql_fetch_array($result);

			if($value['lock'] === "0") {
				$this->acquire($this->userID);
			} else {
				$result = mysql_query("SELECT sync.`UserID` AS `userid`, `Username` AS `user`, `time`, `time`<=NOW()-" . mysql_real_escape_string($this->lock_timeout) . " AS `expired` FROM sync, users WHERE sync.UserID = users.UserID");

				$value = mysql_fetch_array($result);

				if($value["expired"] === "1") {
					$this->acquire($this->userID);
				} else if($this->userID === $value["userid"]) {
					$this->acquire($this->userID);
				} else {
					mysql_query("UNLOCK TABLES");
				throw new Exception("Monitor already acquired by " . $value["user"] . " on " . $value["time"] . "!");
				}
			}

			mysql_query("UNLOCK TABLES");
		}

		public function release_monitor() {
			mysql_query("LOCK TABLES sync WRITE");
			mysql_query("UPDATE sync SET `lock`=0");
			mysql_query("UNLOCK TABLES");
		}
	}
?>