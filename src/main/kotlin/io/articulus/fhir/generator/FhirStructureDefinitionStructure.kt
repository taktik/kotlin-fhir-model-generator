package io.articulus.fhir.generator

import com.google.gson.JsonElement
import com.google.gson.JsonObject

open class FhirStructureDefinitionStructure(val fhirSpec: FhirSpec, val profile: FhirStructureDefinition) {
    var name: String? = null
    var base: String? = null
    var kind: String? = null
    var subClassOf: String? = null
    var snapshot: JsonElement? = null
    var differential: JsonElement? = null



    fun parseFrom(profile: JsonObject) {
        val profileName = profile.get("name").asString

        name = fhirSpec.classNameForProfile(profileName)

        if (profile.has("baseDefinition")) {
            base = profile.get("baseDefinition").asString
        }
        kind = profile.get("kind").asString

        if (base != null) {
            subClassOf = fhirSpec.classNameForProfile(profile.get("baseDefinition").asString)
        }

        snapshot = if (profile.has("snapshot")) profile["snapshot"].asJsonObject["element"] else null
        differential = if (profile.has("differential")) profile["differential"].asJsonObject["element"] else null

    }
}
