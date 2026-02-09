package components

import org.w3c.dom.Element
import kotlin.js.Promise

@JsModule("components.html2canvas")
@JsNonModule
external fun html2canvas(element: Element, options: dynamic = definedExternally): Promise<Element>