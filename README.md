# MITAI-Extensions
MIT AppInventor extensions stuff

* Crowdcrafting - the folks at https://scifabric.com/ provide a free Pybossa service at http://crowdcrafting.org/. This allows you to run crowd-sourcing project without having to set up your own infrastructure.  This extension provides basic interaction with those services, including login, get-next-task, and post-answer (to a task).

* JsonFileDB - while MITAI/Thunkable allow for lists to be interpreted as sets of key-value pairs (a.k.a. a hash or dictionary), dereferencing pairs-within-pairs-within-... gets cumbersome.  This was an attempt to provide N-level deep dereferencing.  Since that assumes you know the structure of the hash, it reads the original hash from a JSON-text file.

* EncodeText - simple extension to add base-64, Hex, MD5, SHA1, SHA256, and SHA512 encodings for strings.  The original idea was to allow MITAI/Thunkable to perform HTTP-Basic authentication (and maybe HTTP-Digest too).  Generally speaking, this works, except that most modern sites send back a session cookie ... which you can't get access to in MITAI/Thunkable.

* WebV2 - NOT WORKING! an attempt to add Response-Header functionality to the standard Web component ... this would let you see any session tokens that get sent back (in a Set-Cookie header).
