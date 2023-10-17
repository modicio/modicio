# modicio Runtime API Documentation

## GET

### `model/metadata/variants` @MetadataController

* URL Params
  * `delimiter?=INT`
  * `closed_only?=BOOLEAN = true`
* Returns
  * JSON Body

Get the metadata of all known variants. The result size can be delimited to the k most recently created variants.
If closed_only, then only closed fragments will be regarded.

---

### `model/metadata/variant` @MetadataController

* URL Params
  * `variant_timestamp?=STRING`
  * `variant_UUID?=String`
  * `variant_name?=String`
  * `closed_only?=BOOLEAN = true`
* Returns
  * JSON Body

Get the complete metadata of a variant. At least one of the URL params must be provided.
If the URL params are not sufficient to determine a variant, an error code is thrown.
If closed_only, then only closed fragments will be regarded.

---

### `model/metadata/variant/versions` @MetadataController

* URL Params
  * `variant_timestamp=STRING`
  * `variant_UUID=String`
  * `delimiter?=INT`
  * `closed_only?=BOOLEAN = true`
* Returns
  * JSON Body

Get the metadata of all known running versions of the specified variant. The response size can be delimited to 
the k most recent versions.
If closed_only, then only closed fragments will be regarded.

---

### `/model/reference` @ModelController

* URL Params
* Returns
  * XML Body

Get the active reference model (fragment) closed model.

---

### `/model/variant/reference` @ModelController

* URL Params
  * `variant_timestamp_from=STRING`
  * `variant_UUID_from=String`

Activate the specified variant as reference model.

---

### `/model/reference/subspace` @ModelController TODO

* URL Params
  * `variant_UUID=String`
  * `root=STRING`
  * `depth=INT`
* Returns
  * XML Body

Get a subspace of the reference model (fragment) of a certain variant as open model. The subspace is constructed 
starting 
from `root` with a specified recursion `depth`. Select a depth of 0 for the minimal subspace.

---

### `/model/reconstructed/` @ModelController TODO

* URL Params
  * `version_UUID?=String`
  * `variant_UUID?=String`
* Returns
  * XML Body

Get a past model reconstruction (fragment) as open model.

---

### `instance`

TODO


## PUT

### `/model` @ModelController TODO

* URL Params
  * `variant_UUID?=String`
  * `variant_name=String`
  * `as_version=Boolean = false`
* Body
  * XML Body (closed fragment)
* Checks
  * XML fragment verification

Create a new variant with a full model.
- If an existing variant is specified, the new variant will be initialized as successor of it.
  - if as_version is set to true, the follow-up will be degraded to a subsequent version only
- If no existing variant is specified, a new empty variant with the given name is constructed.


## POST

### `/model/variant/` @ModelController

* URL Params
  * `variant_UUID_from?=String`
  * `variant_name=String`
 

Create a new variant. 
- If an existing variant is specified, the new variant will be initialized as successor of it.
- If no existing variant is specified, a new empty variant with the given name is constructed.

---

### `model/evolve`

TODO


---

### `instance/create`

TODO

## DELETE