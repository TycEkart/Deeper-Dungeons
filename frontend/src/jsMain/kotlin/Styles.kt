import org.jetbrains.compose.web.css.*

object MonsterSheetStyle : StyleSheet() {
    val mainContainer by style {
        maxWidth(800.px)
        property("margin", "0 auto") // Center horizontally
        position(Position.Relative)
    }

    val controlsContainer by style {
        display(DisplayStyle.Flex)
        justifyContent(JustifyContent.SpaceBetween)
        alignItems(AlignItems.Center)
        marginBottom(5.px)
        padding(0.px, 5.px)
        fontFamily("monospace")
        fontSize(12.px)
        color(Color("#666"))
    }

    val sheetContainer by style {
        fontFamily("Helvetica", "Arial", "sans-serif")
        backgroundColor(Color("#fdf1dc")) // Parchment color
        color(Color("#58180d")) // Dark reddish brown
        padding(20.px)
        border(2.px, LineStyle.Solid, Color("#58180d"))
        property("box-shadow", "0px 0px 15px gray")
    }

    val header by style {
        marginBottom(10.px)
    }

    val subHeader by style {
        fontStyle("italic")
        fontSize(14.px)
    }

    val statsGrid by style {
        display(DisplayStyle.Grid)
        gridTemplateColumns("1fr")
        gap(5.px)
        color(Color("#7a200d"))
    }

    val abilityScoreContainer by style {
        display(DisplayStyle.Flex)
        justifyContent(JustifyContent.SpaceBetween)
        textAlign("center")
        padding(10.px, 0.px)
        color(Color("#7a200d"))
    }

    val abilityScore by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        alignItems(AlignItems.Center)
    }

    val section by style {
        marginTop(10.px)
        marginBottom(10.px)
    }

    val sectionHeader by style {
        property("border-bottom", "1px solid #58180d")
        color(Color("#58180d"))
        fontSize(20.px)
        marginTop(20.px)
        marginBottom(10.px)
    }

    val propertyLine by style {
        marginBottom(5.px)
    }

    val traitBlock by style {
        marginBottom(10.px)
    }
    
    val inputField by style {
        width(100.percent)
        padding(5.px)
        marginTop(2.px)
        marginBottom(2.px)
        border(1.px, LineStyle.Solid, Color("#ccc"))
        borderRadius(4.px)
    }
}