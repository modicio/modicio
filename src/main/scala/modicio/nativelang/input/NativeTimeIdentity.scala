package modicio.nativelang.input

import io.circe.generic.JsonCodec

@JsonCodec
case class NativeTimeIdentity(variantTime: Long, runningTime: Long, versionTime: Long, variantId: String, runningId: String, versionId: String)
