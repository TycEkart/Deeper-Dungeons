package com.deeperdungeons.styles

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
        marginBottom(10.px)
        padding(0.px, 5.px)
        fontFamily("sans-serif")
        fontSize(12.px)
        color(Color("#666"))
    }

    val sheetContainer by style {
        fontFamily("Book Antiqua", "Palatino Linotype", "Palatino", "serif")
        backgroundColor(Color("#fdf1dc")) // Parchment color
        color(Color("#58180d")) // Dark reddish brown
        padding(20.px)
        property("box-shadow", "0px 0px 15px rgba(0,0,0,0.2)")
        
        // D&D 5e Stat Block Border Style
        border(1.px, LineStyle.Solid, Color("#d4d4d4"))
        property("border-top", "5px solid #58180d")
        property("border-bottom", "5px solid #58180d")
    }

    val header by style {
        marginBottom(10.px)
    }
    
    val monsterName by style {
        fontFamily("MrJeeves", "Book Antiqua", "serif")
        fontSize(28.px)
        fontWeight("bold")
        property("font-variant", "small-caps")
        color(Color("#58180d"))
        margin(0.px)
        lineHeight(1.em)
    }

    val subHeader by style {
        fontStyle("italic")
        fontSize(14.px)
        color(Color.black)
    }

    val taperedRule by style {
        height(2.px)
        property("background", "#58180d")
        property("background", "linear-gradient(90deg, transparent 0%, #58180d 50%, transparent 100%)")
        property("border", "none")
        margin(10.px, 0.px)
        display(DisplayStyle.Block)
    }

    val statsGrid by style {
        display(DisplayStyle.Block)
        color(Color("#58180d"))
        fontSize(14.px)
        lineHeight(1.5.em)
    }

    val abilityScoreContainer by style {
        display(DisplayStyle.Flex)
        justifyContent(JustifyContent.SpaceAround)
        textAlign("center")
        padding(10.px, 0.px)
        color(Color("#58180d"))
    }

    val abilityScore by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        alignItems(AlignItems.Center)
        width(120.px) // Increased width significantly to accommodate input fields
    }
    
    val abilityScoreLabel by style {
        fontWeight("bold")
        fontSize(12.px)
    }

    val section by style {
        marginTop(10.px)
        marginBottom(10.px)
        fontSize(14.px)
        lineHeight(1.4.em)
    }

    val sectionHeader by style {
        color(Color("#58180d"))
        fontSize(18.px)
        fontWeight("normal")
        property("font-variant", "small-caps")
        property("border-bottom", "1px solid #58180d")
        marginTop(15.px)
        marginBottom(5.px)
    }

    val propertyLine by style {
        marginBottom(2.px)
        display(DisplayStyle.Flex)
        gap(5.px)
    }
    
    val propertyLabel by style {
        fontWeight("bold")
        color(Color("#58180d"))
        whiteSpace("nowrap")
    }

    val traitBlock by style {
        marginBottom(10.px)
        lineHeight(1.4.em)
    }
    
    val traitName by style {
        fontWeight("bold")
        fontStyle("italic")
        color(Color("#58180d"))
        marginRight(5.px)
    }
    
    val inputField by style {
        width(100.percent)
        padding(5.px)
        marginTop(2.px)
        marginBottom(2.px)
        border(1.px, LineStyle.Solid, Color("#ccc"))
        borderRadius(4.px)
        fontFamily("inherit")
    }
}