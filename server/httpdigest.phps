<?php

/*
Copyright (c) 2005 Paul James
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:
1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
3. Neither the name of the Paul James nor the names of its contributors
   may be used to endorse or promote products derived from this software
   without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.
*/

	/** HTTP Digest authentication class */
	class HTTPDigest
	{
		/** The Digest opaque value (any string will do, never sent in plain text over the wire).
		 * @var str
		 */
		var $opaque = 'opaque';

		/** The authentication realm name.
		 * @var str
		 */    
		var $realm = 'Synchro';

		/** The base URL of the application, auth data will be used for all resources under this URL.
		 * @var str
		 */
		var $baseURL = '/Synchro';

		/** The private key.
		 * @var str
		 */
		var $privateKey = 'privatekey';

		/** The life of the nonce value in seconds
		 * @var int
		 */
		var $nonceLife = 300;

		/** Send HTTP Auth header */
		function send($error_message)
		{
			header('WWW-Authenticate: Digest '.
				'realm="'.$this->realm.'", '.
				'domain="'.$this->baseURL.'", '.
				'qop=auth, '.
				'algorithm=MD5, '.
				'nonce="'.$this->getNonce().'", '.
				'opaque="'.$this->getOpaque().'"'
			);
			header('HTTP/1.0 401 Unauthorized');

			echo $error_message;
			exit;
		}
		
		/** Get the HTTP Auth header
		 * @return str
		 */
		private function getAuthHeader()
		{
			if (isset($_SERVER['Authorization'])) {
				return $_SERVER['Authorization'];
			} elseif (function_exists('apache_request_headers')) {
				$headers = apache_request_headers();
				if (isset($headers['Authorization'])) {
					return $headers['Authorization'];
				}
			}

			throw new Exception("No authorization data!");
		}

		/** Parse HTTP digest message
		 * @return keyed array (name => value)
		 */
		private function parseDigestMessage($message) {
			$to_parse = array("username", "nonce", "response", "opaque", "uri", "cnonce");
			$data = array();

			foreach($to_parse as $name) {
				preg_match('/' . $name . '="([^"]+)"/', $message, $tmp);
				$data[$name] = $tmp[1];
			}

			preg_match('/qop="?([^,\s"]+)/', $message, $tmp); $data["qop"] = $tmp[1];
			preg_match('/nc=([^,\s]+)/', $message, $tmp); $data["nc"] = $tmp[1];

			return $data;
		}

		/** Authenticate the user and return username on success.
		 * @param str[] users Array of username/password pairs
		 * @return str
		 */
		function authenticate($mysql)
		{
			$authorization = $this->getAuthHeader();

			if (substr($authorization, 0, 5) == "Basic") {
				throw new Exception("Basic authentication not allowed!");
			}

			$data = $this->parseDigestMessage($authorization);

			if (!($data["opaque"] == $this->getOpaque() && $data["uri"] == $this->getURI() && $data["nonce"] == $this->getNonce())) {
				throw new Exception("Wrong session data!");
			}
			
			$result = mysql_query("SELECT UserID, Username, Password FROM users WHERE Username='" . mysql_real_escape_string($data["username"]) . "'");

			$value = mysql_fetch_array($result);

			$a1 = $value["Password"];
			$a2 = md5($_SERVER['REQUEST_METHOD'].':'.$this->getURI());

			if ($data["qop"] == "auth") {
				$expectedResponse = md5($a1.':'.$data["nonce"].':'.$data["nc"].':'.$data["cnonce"].':'.$data["qop"].':'.$a2);
			} else {
				$expectedResponse = md5($a1.':'.$data["nonce"].':'.$a2);   
			}

			if ($data["response"] == $expectedResponse) {
				return $value["UserID"];
			} else {
				throw new Exception("Wrong username or password!");
			}
		}

		/** Get nonce value for HTTP Digest.
		 * @return str
		 */
		function getNonce() {
			$time = ceil(time() / $this->nonceLife) * $this->nonceLife;
			return md5(date('Y-m-d H:i', $time).':'.$_SERVER['REMOTE_ADDR'].':'.$this->privateKey);
		}

		/** Get URI value for HTTP Digest.
		 * @return str
		 */
		function getURI() {
			$requestURI = $_SERVER['REQUEST_URI'];

			if (strpos($requestURI, '?') !== FALSE) { // hack for IE which does not pass querystring in URI element of Digest string or in response hash
				$requestURI = substr($requestURI, 0, strlen($uri[1]));
			}
			
			return $requestURI;
		}

		/** Get opaque value for HTTP Digest.
		 * @return str
		 */
		function getOpaque()
		{
			return md5($this->opaque);
		}

		/** Get realm for HTTP Digest taking PHP safe mode into account.
		 * @return str
		 */
		function getRealm()
		{
			if (ini_get('safe_mode')) {
				return $this->realm.'-'.getmyuid();
			} else {
				return $this->realm;
			}    
		}
	}

/* Example usage
$HTTPDigest =& new HTTPDigest();
if ($username = $HTTPDigest->authenticate(array(
    'username' => md5('username:'.$HTTPDigest->getRealm().':password')
))) {
    echo sprintf('Logged in as "%s"', $username);      
} else {
    $HTTPDigest->send();
    echo 'Not logged in';
}
//*/

?>
