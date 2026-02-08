import org.w3c.dom.Element
import kotlin.js.Promise

@JsModule("html2canvas")
@JsNonModule
external fun html2canvas(element: Element): Promise<Element>