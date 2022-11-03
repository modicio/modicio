package modicio.nativelang.input.api

import modicio.core.api.RegistryJ
import modicio.core.api.TransformerJ
import modicio.verification.api.DefinitionVerifierJ
import modicio.verification.api.ModelVerifierJ
import modicio.api.JavaAPIConversions._
import modicio.nativelang.input.{NativeCompartment, NativeDSL, NativeDSLTransformer}

import java.util.concurrent.CompletableFuture


class NativeDSLTransformerJ(registry:RegistryJ,
                                   definitionVerifier:DefinitionVerifierJ,
                                   modelVerifier:ModelVerifierJ)
        extends TransformerJ[NativeDSL, NativeCompartment](registry, definitionVerifier, modelVerifier) {

  private val transformer = new NativeDSLTransformer(registry, definitionVerifier, modelVerifier)

  override def extendJ(input: NativeDSL): CompletableFuture[Any] = transformer.extend(input)

  override def decomposeInstanceJ(input: String): CompletableFuture[NativeCompartment] = transformer.decomposeInstance(input)

  override def decomposeModelJ(): CompletableFuture[NativeDSL] = transformer.decomposeModel()
}
