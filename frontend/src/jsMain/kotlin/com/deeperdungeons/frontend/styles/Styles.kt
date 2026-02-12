package com.deeperdungeons.frontend.styles

import org.jetbrains.compose.web.css.*

object MonsterSheetStyle : StyleSheet() {
    init {
        "body" style {
            backgroundColor(Color("#1a1a1a")) // Dark background for the whole page
            margin(0.px)
            padding(20.px) // Add some padding so the sheet doesn't touch edges
        }
    }

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
        color(Color("#ccc")) // Lighter text for controls on dark background
    }

    val dndButton by style {
        backgroundColor(Color("#fdf1dc")) // Same as sheet background
        color(Color("#58180d"))
        border(2.px, LineStyle.Solid, Color("#58180d"))
        borderRadius(5.px)
        padding(5.px, 10.px)
        fontFamily("Book Antiqua", "Palatino Linotype", "Palatino", "serif")
        fontWeight("bold")
        fontSize(12.px)
        property("font-variant", "small-caps")
        cursor("pointer")
        property("transition", "all 0.2s ease")

        hover {
            backgroundColor(Color("#58180d"))
            color(Color("#fdf1dc"))
        }
    }

    val sheetContainer by style {
        fontFamily("Book Antiqua", "Palatino Linotype", "Palatino", "serif")
        backgroundColor(Color("#fdf1dc")) // Parchment color
        color(Color("#58180d")) // Dark reddish brown
        padding(20.px)
        property("box-shadow", "0px 0px 15px rgba(0,0,0,0.5)") // Darker shadow
        
        // D&D 5e Stat Block Border Style
        border(1.px, LineStyle.Solid, Color("#d4d4d4"))
        property("border-top", "5px solid #58180d")
        property("border-bottom", "5px solid #58180d")
    }

    val listContainer by style {
        maxWidth(500.px) // Narrower list
        property("margin", "0 auto") // Center horizontally
        fontFamily("Book Antiqua", "Palatino Linotype", "Palatino", "serif")
        backgroundColor(Color("#fdf1dc")) // Parchment color
        color(Color("#58180d")) // Dark reddish brown
        padding(20.px)
        property("box-shadow", "0px 0px 15px rgba(0,0,0,0.5)") // Darker shadow
        border(1.px, LineStyle.Solid, Color("#d4d4d4"))
        property("border-top", "5px solid #58180d")
        property("border-bottom", "5px solid #58180d")
        
        // Add a subtle texture or gradient to make it look more like paper
        property("background-image", "url('https://www.transparenttextures.com/patterns/aged-paper.png')")
    }

    val listItem by style {
        padding(10.px)
        border(1.px, LineStyle.Solid, Color("#d4d4d4"))
        borderRadius(5.px)
        cursor("pointer")
        backgroundColor(Color.white)
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        gap(10.px)
        marginBottom(10.px)
        property("transition", "all 0.2s ease")
        
        // Make list items slightly transparent to blend with parchment
        backgroundColor(Color("rgba(255, 255, 255, 0.6)"))
        border(1.px, LineStyle.Solid, Color("#58180d"))

        hover {
            backgroundColor(Color("rgba(255, 255, 255, 0.9)"))
            property("transform", "translateX(5px)")
            property("box-shadow", "2px 2px 5px rgba(0,0,0,0.1)")
        }
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
