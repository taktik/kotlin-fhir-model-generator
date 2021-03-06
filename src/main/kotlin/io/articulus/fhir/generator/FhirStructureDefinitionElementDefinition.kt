package io.articulus.fhir.generator

import com.google.gson.JsonElement
import com.google.gson.JsonObject


class FhirStructureDefinitionElementDefinition(val element: FhirStructureDefinitionElement, dict: JsonObject) {
    val log by logger()

    val id: String = if (dict.has("id")) dict["id"].asString else throw Exception("ID not found in JsonObject")
    val types = mutableListOf<FhirElementType>()
    val short: String
    val name: String?
    var propName: String? = null // todo huh?
    val contentReference: String?
    var contentReferenced: FhirStructureDefinitionElementDefinition? = null
    val formal: String
    val comment: String?
    val binding: FhirElementBinding?
    val slicing: JsonElement?
    val representation: JsonElement?

    init {
        if (dict.has("type")) {
            dict.getAsJsonArray("type").forEach { e ->
                types.add(FhirElementType(e as JsonObject))
            }
        }

        name = dict.getStringOrNull("name")
        contentReference = if (dict.has("contentReference")) dict["contentReference"].asString else null
        short = dict.getStringOrEmpty("short")
        formal = dict.getStringOrEmpty("definition")
        comment = dict.getStringOrNull("comments")

        binding = if (dict.has("binding")) FhirElementBinding(dict["binding"] as JsonObject) else null
        slicing = dict["slicing"]
        representation = dict["representation"]
    }


    fun resolveDependencies() {
        // update the definition from a reference, if there is one
        if (contentReference != null) {
            if (!contentReference.startsWith("#")) {
                throw Exception("Only relative 'contentReference' dict definitions are supported right now")
            }
            val elem = element.profile.elementWithId(contentReference.substring(1))

            if (elem == null) {
                throw Exception("There is no element definiton with id $contentReference")
            } else {
                contentReferenced = elem.definition
            }
        }
    }


    /**
     * Determines the class-name that the element would have if it was
     * defining a class. This means it uses "name", if present, and the last
     * "path" component otherwise.
     */
    fun nameIfClass(): String {
        if (contentReferenced != null) {
            return contentReferenced!!.nameIfClass()
        }

        val withName = name ?: propName
        val parentName = element.parent?.nameIfClass()

        var className = if (withName != null) element.profile.fhirSpec.classNameForType(withName, parentName) else null
        if (parentName != null) { // && and self.element.profile.spec.settings.backbone_class_adds_parent:
            className = parentName + className
        }
        return className!!.replace("[x]","")

    }
}

