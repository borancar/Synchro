Synchro
=======

Simple file synchronization using PHP and MySQL using the client-server model. It allows multiple users to
share one synchronized file.

Technical notes
---------------

* The server side is done in PHP/MySQL so should work with most hosting providers.
* Entering critical section is done via MySQL primitives with the mutual exclusion done by the server
* Client side in Java
* Uses HTML requests
* MD5 hashing is performed on files for both upload and download
