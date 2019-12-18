
<!-- MarkdownTOC -->

- [Idempotence](#idempotence)
	- [Idempotence Methods](#idempotence-methods)
		- [Safe vs Idempotent Methods](#safe-vs-idempotent-methods)
		- [Why PUT Idempotent and PATCH not](#why-put-idempotent-and-patch-not)
		- [Why DELETE is Idempotent](#why-delete-is-idempotent)

<!-- /MarkdownTOC -->

# Idempotence
## Idempotence Methods
### Safe vs Idempotent Methods
* Safe methods: HTTP methods that do not modify resources. 
* Idempotent methods: HTTP methods that can be called many times without different outcomes. 

| HTTP METHOD  |  IDEMPOTENCE  |  SAFETY  |
|---|---|---|
| GET  | YES  | YES  |
| HEAD  | YES   | YES  |
| OPTIONS  | YES  | YES  |
| TRACE  | YES  | YES  |
| POST  | NO  | NO  |
| PATCH  | NO  | NO  |
| PUT  |  YES | NO  |
| DELETE  | YES  | NO |

### Why PUT Idempotent and PATCH not
* It's because it matters how you apply your changes. If you'd like to change the name property of a resource, you might send something like {"name": "foo"} as a payload and that would indeed be idempotent since executing this request any number of times would yield the same result: The resources name attribute is now "foo".
* But PATCH is much more general in how you can change a resource (check this definition on how to apply a JSON patch). It could also, for example, mean to move the resource and would look something like this: { "op": "move", "from": "/a/b/c", "path": "/a/b/d" }. This operation is obviously not idempotent since calling at a second time would result in an error.
* So while most PATCH operations might be idempotent, there are some that aren't.

### Why DELETE is Idempotent
* "Methods can also have the property of "idempotence" in that (aside from error or expiration issues) the side-effects of N > 0 identical requests is the same as for a single request. The methods GET, HEAD, PUT and DELETE share this property. Also, the methods OPTIONS and TRACE SHOULD NOT have side effects, and so are inherently idempotent. "
* The key bit there is the side-effects of N > 0 identical requests is the same as for a single request.
* You would be correct to expect that the status code would be different but this does not affect the core concept of idempotency - you can send the request more than once without additional changes to the state of the server.